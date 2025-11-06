package com.capstone.web.refrigerator.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * OCR 텍스트 전처리 유틸리티
 * GPT-5 Nano에 전달하기 전 불필요한 정보를 제거하여 토큰 비용 절감
 */
@Slf4j
@Component
public class ReceiptPreprocessor {

    // 제거할 불필요한 패턴들
    private static final List<String> NOISE_PATTERNS = Arrays.asList(
            // 광고/프로모션
            "바코드", "QR", "쿠폰", "적립", "포인트", "할인", "이벤트",
            // 감사 인사
            "감사합니다", "Thank you", "Come again", "방문", "이용",
            // 연락처/URL
            "http", "www.", "tel:", "Tel:", "전화", "문의",
            // 불필요한 정보
            "영수증", "receipt", "Receipt", "부가세", "VAT", "과세",
            // 기타
            "---", "===", "***", "◆", "■", "●"
    );

    /**
     * OCR 텍스트 정리
     * 
     * @param rawText CLOVA OCR 원문
     * @return 정리된 텍스트
     */
    public String clean(String rawText) {
        if (rawText == null || rawText.isEmpty()) {
            return "";
        }

        log.debug("전처리 전 텍스트 길이: {} 글자", rawText.length());

        String cleanedText = Arrays.stream(rawText.split("\n"))
                .map(String::trim)
                .filter(line -> !line.isEmpty()) // 빈 줄 제거
                .filter(this::isNotNoise) // 불필요한 줄 제거
                .filter(this::isNotBarcode) // 바코드/숫자만 있는 줄 제거
                .collect(Collectors.joining("\n"));

        log.debug("전처리 후 텍스트 길이: {} 글자 ({}% 감소)",
                cleanedText.length(),
                (rawText.length() - cleanedText.length()) * 100 / rawText.length());

        return cleanedText;
    }

    /**
     * 불필요한 패턴이 포함된 줄인지 확인
     */
    private boolean isNotNoise(String line) {
        String lowerLine = line.toLowerCase();
        return NOISE_PATTERNS.stream()
                .noneMatch(pattern -> lowerLine.contains(pattern.toLowerCase()));
    }

    /**
     * 바코드나 의미 없는 숫자만 있는 줄인지 확인
     */
    private boolean isNotBarcode(String line) {
        // 숫자와 특수문자만으로 구성되고 길이가 긴 줄은 바코드로 간주
        String digitsOnly = line.replaceAll("[^0-9]", "");
        if (digitsOnly.length() > 10 && digitsOnly.length() == line.replaceAll("\\s", "").length()) {
            return false; // 바코드
        }

        // 숫자만 있고 길이가 너무 긴 경우
        if (line.matches("^[0-9\\s-]+$") && line.length() > 15) {
            return false;
        }

        return true;
    }
}
