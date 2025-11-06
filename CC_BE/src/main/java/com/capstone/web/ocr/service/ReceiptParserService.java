package com.capstone.web.ocr.service;

import com.capstone.web.ocr.dto.OcrDto.ParsedItem;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 영수증 파서 서비스
 * OCR로 추출된 텍스트에서 식재료 정보를 파싱합니다.
 */
@Slf4j
@Service
public class ReceiptParserService {

    // 가격 패턴: 1,000원, 1000원, 1.000원, ₩1000, 3 000원 등
    // 숫자, 쉼표, 마침표, 공백의 조합을 모두 캡처
    // ₩ 기호나 '원' 문자가 있어야 가격으로 인식
    private static final Pattern PRICE_PATTERN = Pattern.compile(
        "(?:₩\\s*([0-9][0-9,. ]*[0-9]|[0-9])|([0-9][0-9,. ]*[0-9]|[0-9])\\s*원)"
    );
    
    // 수량 패턴: 숫자 + 단위 (긴 단위부터 매칭, 선택적 공백 포함)
    // 예: 2개, 1.5kg, 500ml, 2 리터 등
    private static final Pattern QUANTITY_PATTERN = Pattern.compile(
        "([0-9]+(?:\\.[0-9]+)?)\\s*" +  // 정수 또는 소수 (예: 1, 1.5, 0.5)
        "(킬로그램|킬로|리터|그람|밀리리터|" +  // 긴 한글 단위
        "kg|ml|g|l|" +  // 영문 단위
        "개|봉|팩|병|캔|통|묶음|줄|장|마리)"  // 한글 단위
    );
    
    // 무시할 키워드 패턴 (영수증의 메타데이터)
    private static final String[] IGNORE_KEYWORDS = {
        // 결제 관련
        "합계", "총액", "소계", "카드", "현금", "받은금액", "거스름돈", "부가세", "과세",
        "할인", "쿠폰", "적립", "포인트", "결제", "승인", "거래",
        // 매장 정보
        "매장", "점", "영수증", "감사합니다", "tel", "전화", "주소", "사업자",
        "대표", "번호", "일시", "날짜", "시간", "상호", "사업자등록번호",
        // 기타
        "welcome", "thank", "you", "receipt", "total", "subtotal", "tax", "card"
    };

    /**
     * OCR로 추출된 텍스트에서 식재료 정보 파싱
     * 
     * @param extractedText OCR로 추출된 원본 텍스트
     * @return 파싱된 식재료 목록
     */
    public List<ParsedItem> parseReceipt(String extractedText) {
        log.info("Starting receipt parsing...");
        
        List<ParsedItem> items = new ArrayList<>();
        
        if (extractedText == null || extractedText.isEmpty()) {
            log.warn("Extracted text is empty");
            return items;
        }
        
        // 줄 단위로 분리
        String[] lines = extractedText.split("\\n");
        
        for (String line : lines) {
            line = line.trim();
            
            // 빈 줄이나 너무 짧은 줄 무시
            if (line.isEmpty() || line.length() < 2) {
                continue;
            }
            
            // 무시할 키워드 체크
            if (shouldIgnoreLine(line)) {
                continue;
            }
            
            // 식재료 정보 파싱
            ParsedItem item = parseLineAsItem(line);
            if (item != null) {
                items.add(item);
                log.debug("Parsed item: {}", item.getName());
            }
        }
        
        log.info("Parsing completed. Found {} items", items.size());
        return items;
    }

    /**
     * 줄을 무시해야 하는지 확인
     */
    private boolean shouldIgnoreLine(String line) {
        String lowerLine = line.toLowerCase();
        
        for (String keyword : IGNORE_KEYWORDS) {
            if (lowerLine.contains(keyword)) {
                return true;
            }
        }
        
        // 숫자만 있는 줄 무시 (가격만 있는 경우)
        if (line.matches("^[\\d,. ]+$")) {
            return true;
        }
        
        return false;
    }

    /**
     * 한 줄에서 식재료 정보 파싱
     */
    private ParsedItem parseLineAsItem(String line) {
        // 가격 제거 (식재료명 추출을 위해)
        String nameWithoutPrice = PRICE_PATTERN.matcher(line).replaceAll("").trim();
        
        // 수량 및 단위 추출
        Integer quantity = null;
        String unit = null;
        Matcher quantityMatcher = QUANTITY_PATTERN.matcher(line);
        if (quantityMatcher.find()) {
            try {
                String quantityStr = quantityMatcher.group(1);
                // 소수점이 있는 경우 반올림하여 정수로 변환
                if (quantityStr.contains(".")) {
                    quantity = (int) Math.round(Double.parseDouble(quantityStr));
                } else {
                    quantity = Integer.parseInt(quantityStr);
                }
                unit = normalizeUnit(quantityMatcher.group(2));
            } catch (NumberFormatException e) {
                log.debug("Failed to parse quantity: {}", quantityMatcher.group(1));
            }
        }
        
        // 가격 추출
        Integer price = null;
        Matcher priceMatcher = PRICE_PATTERN.matcher(line);
        if (priceMatcher.find()) {
            try {
                // 두 그룹 중 매칭된 그룹 선택 (₩ 기호가 있으면 group(1), 원이 있으면 group(2))
                String priceStr = priceMatcher.group(1) != null ? 
                    priceMatcher.group(1) : priceMatcher.group(2);
                priceStr = priceStr.replaceAll("[,. ]", "");
                price = Integer.parseInt(priceStr);
            } catch (NumberFormatException e) {
                log.debug("Failed to parse price: {}", priceMatcher.group(1));
            }
        }
        
        // 식재료명 정제
        String cleanName = cleanItemName(nameWithoutPrice);
        
        // 이름이 없거나 너무 짧으면 무시
        if (cleanName.isEmpty() || cleanName.length() < 2) {
            return null;
        }
        
        return ParsedItem.builder()
                .name(cleanName)
                .quantity(quantity)
                .unit(unit)
                .price(price)
                .build();
    }

    /**
     * 단위 정규화 (일관성을 위해)
     */
    private String normalizeUnit(String unit) {
        if (unit == null) return null;
        
        return switch (unit.toLowerCase()) {
            case "킬로그램", "킬로" -> "kg";
            case "그람" -> "g";
            case "리터" -> "l";
            case "밀리리터" -> "ml";
            default -> unit;
        };
    }

    /**
     * 식재료명 정제
     */
    private String cleanItemName(String name) {
        // 수량 패턴 제거
        name = QUANTITY_PATTERN.matcher(name).replaceAll("").trim();
        
        // 특수문자 제거 (단, 한글, 영문, 숫자, 공백은 유지)
        name = name.replaceAll("[^가-힣a-zA-Z0-9\\s]", "").trim();
        
        // 연속된 공백을 하나로
        name = name.replaceAll("\\s+", " ");
        
        return name;
    }
}
