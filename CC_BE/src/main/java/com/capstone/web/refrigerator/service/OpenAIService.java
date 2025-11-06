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
            당신은 영수증 텍스트를 JSON으로 파싱하는 전문가입니다.
            
            다음 규칙에 따라 영수증 정보를 추출하세요:
            1. store: 매장명 (예: "CU 강남점", "이마트")
            2. date: 구매 날짜 (YYYY-MM-DD 형식, 예: "2025-01-15")
            3. items: 구매 항목 배열
               - name: 상품명
               - price: 가격 (숫자만, 쉼표 제거)
               - quantity: 수량 (기본값 1)
            4. total: 총 금액 (숫자만, 쉼표 제거)
            
            주의사항:
            - 광고, 쿠폰, 바코드, "감사합니다" 같은 불필요한 정보는 무시
            - 가격이 없는 항목은 제외
            - 날짜 형식은 반드시 YYYY-MM-DD
            - 모든 금액은 정수형 숫자만
            
            응답은 반드시 다음 JSON 형식으로:
            {
              "store": "매장명",
              "date": "2025-01-15",
              "items": [
                {"name": "상품명", "price": 3000, "quantity": 1}
              ],
              "total": 3000
            }
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
            requestBody.put("max_tokens", config.getMaxTokens());

            // 시스템 프롬프트 + 사용자 입력 (OCR 텍스트)
            List<Map<String, String>> messages = new ArrayList<>();
            messages.add(Map.of("role", "system", "content", SYSTEM_PROMPT));
            messages.add(Map.of("role", "user", "content", ocrText));
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
