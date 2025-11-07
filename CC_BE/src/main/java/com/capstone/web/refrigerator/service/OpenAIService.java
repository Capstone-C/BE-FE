package com.capstone.web.refrigerator.service;

import com.capstone.web.refrigerator.config.OpenAIConfig;
import com.capstone.web.refrigerator.dto.RefrigeratorDto.ScanPurchaseHistoryResponse;
import com.capstone.web.refrigerator.dto.RefrigeratorDto.ScanPurchaseHistoryResponse.PurchasedItem;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

/**
 * OpenAI GPT-5 Nano를 사용한 영수증 파싱 서비스
 * 참고: https://platform.openai.com/docs/models/gpt-5-nano
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OpenAIService {

    private final OpenAIConfig config;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    private static final String SYSTEM_PROMPT = """
            You are a receipt parser. Extract purchase information from Korean receipts.
            
            STRICT RULES:
            1. Extract store name (매장명) - Look for 농협, CU, 이마트, GS25, etc.
            2. Extract purchase date in YYYY-MM-DD format
            3. Extract ONLY food items with prices:
               - Look for lines with: [상품명] [단가] [수량] [금액]
               - Common patterns: "P상품명", "상품(코드)", "001 P상품명"
               - Skip: 바코드, 전화, 주소, 사업자번호, 포인트, 카드정보
            4. Extract total amount (판매총액, 받을금액, 합계)
            
            EXAMPLES OF VALID ITEMS:
            - "P굿모닝우유 900ML" price: 1350
            - "P양파" price: 3300
            - "P하선정 바로먹기좋은장" price: 1380
            
            REQUIRED JSON FORMAT (no additional text):
            {
              "store": "store name",
              "date": "YYYY-MM-DD",
              "items": [
                {"name": "item name", "price": 1350, "quantity": 1}
              ],
              "total": 8560
            }
            
            If you cannot find items, return empty array for items but still extract store, date, and total.
            """;

    /**
     * OCR 텍스트를 GPT-5 Nano로 파싱하여 구조화된 데이터 반환
     *
     * @param ocrText CLOVA OCR로 추출한 원문
     * @return 파싱된 구매 이력 데이터
     */
    public ScanPurchaseHistoryResponse parseReceipt(String ocrText) {
        try {
            log.info("GPT-5 Nano로 영수증 파싱 시작 (입력 길이: {} 글자)", ocrText.length());

            // OpenAI API 요청 구성
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + config.getApiKey());
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", config.getModel());
            requestBody.put("max_completion_tokens", config.getMaxTokens());  // GPT -5 Nano(GPT-4o)는 max_completion_tokens 사용

            // 시스템 프롬프트 + 사용자 입력 (OCR 텍스트)
            List<Map<String, String>> messages = new ArrayList<>();
            messages.add(Map.of("role", "system", "content", SYSTEM_PROMPT));
            
            // 사용자 메시지: OCR 텍스트 + 추출 지시
            String userMessage = String.format(
                "Extract purchase information from this receipt:\n\n%s\n\n" +
                "Remember: Extract items with pattern like 'P상품명', '001 P상품명' and their prices. " +
                "Return valid JSON only.",
                ocrText
            );
            messages.add(Map.of("role", "user", "content", userMessage));
            requestBody.put("messages", messages);

            // JSON 출력 강제
            requestBody.put("response_format", Map.of("type", "json_object"));

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            // API 호출
            log.debug("OpenAI API 호출: {}", config.getApiUrl());
            ResponseEntity<String> response;
            
            try {
                response = restTemplate.exchange(
                        config.getApiUrl(),
                        HttpMethod.POST,
                        entity,
                        String.class
                );
            } catch (org.springframework.web.client.HttpClientErrorException e) {
                log.error("OpenAI API 호출 실패 ({}): {}", e.getStatusCode(), e.getResponseBodyAsString());
                
                // 계정 비활성화 또는 결제 문제
                if (e.getStatusCode().value() == 429 || e.getStatusCode().value() == 401) {
                    throw new RuntimeException("OpenAI API 인증 또는 결제 문제가 발생했습니다. " +
                            "API 키와 계정 상태를 확인하세요: https://platform.openai.com/account/billing");
                }
                throw new RuntimeException("OpenAI API 호출 실패: " + e.getMessage());
            }

            // 응답 파싱
            return parseGptResponse(response.getBody());

        } catch (RuntimeException e) {
            // 이미 처리된 RuntimeException은 그대로 전달
            throw e;
        } catch (Exception e) {
            log.error("GPT-5 Nano 파싱 실패: {}", e.getMessage(), e);
            throw new RuntimeException("영수증 파싱에 실패했습니다: " + e.getMessage());
        }
    }

    /**
     * GPT-5 Nano 응답에서 영수증 데이터 추출
     */
    private ScanPurchaseHistoryResponse parseGptResponse(String responseBody) throws Exception {
        JsonNode root = objectMapper.readTree(responseBody);
        String content = root.path("choices").get(0).path("message").path("content").asText();

        log.debug("GPT-5 Nano 응답: {}", content);

        // JSON 파싱
        JsonNode data = objectMapper.readTree(content);

        // 토큰 사용량 로깅 (비용 모니터링)
        int inputTokens = root.path("usage").path("prompt_tokens").asInt();
        int outputTokens = root.path("usage").path("completion_tokens").asInt();
        log.info("토큰 사용량 - 입력: {}, 출력: {}, 총합: {}", inputTokens, outputTokens, inputTokens + outputTokens);

        // DTO 변환
        String store = data.path("store").asText("알 수 없는 매장");
        LocalDate purchaseDate = parseDate(data.path("date").asText());
        Integer totalAmount = data.path("total").asInt(0);

        List<PurchasedItem> items = new ArrayList<>();
        JsonNode itemsNode = data.path("items");
        for (JsonNode itemNode : itemsNode) {
            PurchasedItem item = PurchasedItem.builder()
                    .name(itemNode.path("name").asText())
                    .price(itemNode.path("price").asInt())
                    .quantity(itemNode.path("quantity").asInt(1))
                    .build();
            items.add(item);
        }

        log.info("파싱 완료: {} ({}), 항목 {}개, 총액 {}원", store, purchaseDate, items.size(), totalAmount);

        return ScanPurchaseHistoryResponse.builder()
                .store(store)
                .purchaseDate(purchaseDate)
                .items(items)
                .totalAmount(totalAmount)
                .build();
    }

    /**
     * 날짜 문자열을 LocalDate로 변환 (다양한 포맷 지원)
     */
    private LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) {
            log.warn("날짜 정보가 없어 오늘 날짜로 대체");
            return LocalDate.now();
        }

        // 다양한 날짜 포맷 시도
        List<DateTimeFormatter> formatters = Arrays.asList(
                DateTimeFormatter.ofPattern("yyyy-MM-dd"),
                DateTimeFormatter.ofPattern("yyyy.MM.dd"),
                DateTimeFormatter.ofPattern("yyyy/MM/dd"),
                DateTimeFormatter.ofPattern("yyyyMMdd")
        );

        for (DateTimeFormatter formatter : formatters) {
            try {
                return LocalDate.parse(dateStr, formatter);
            } catch (DateTimeParseException ignored) {
                // 다음 포맷 시도
            }
        }

        log.warn("날짜 파싱 실패: {} -> 오늘 날짜로 대체", dateStr);
        return LocalDate.now();
    }
}
