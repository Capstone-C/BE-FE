package com.capstone.web.refrigerator.service;

import com.capstone.web.refrigerator.config.GeminiConfig;
import com.capstone.web.refrigerator.dto.RefrigeratorDto.ScanPurchaseHistoryResponse;
import com.capstone.web.refrigerator.dto.RefrigeratorDto.ScanPurchaseHistoryResponse.PurchasedItem;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Google Gemini API를 사용한 영수증 파싱 서비스
 * (Vision generateContent 엔드포인트 v1beta 사용)
 * 공식 Quickstart는 SDK 사용을 권장하지만 여기서는 REST 호출을 사용합니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GeminiService {

    private static final String SYSTEM_INSTRUCTION = "You are a Korean receipt ingredient extractor. Return ONLY JSON with keys: items (array). Each item has: name (string), quantity (int, default 1), unit (string, from ['개','팩','봉','병','캔','컵','박스','g','kg','ml','L','포','묶음'] or null). Focus on edible food items only. Ignore prices, totals, discounts, barcodes, points, payment info. Output example: {\"items\":[{\"name\":\"우유\",\"quantity\":1,\"unit\":\"개\"},{\"name\":\"사과\",\"quantity\":4,\"unit\":\"개\"}]}. No extra text.";
    private final GeminiConfig config;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    /**
     * 이미지(영수증 사진) 자체를 Gemini Vision 모델로 파싱하여 구조화된 구매 이력 반환
     * - 외부 OCR(CLOVA) 단계를 생략하고 Gemini의 이미지 이해 기능 사용
     */
    public ScanPurchaseHistoryResponse parseReceiptImage(MultipartFile image) {
        resolveApiKeyFromEnv();
        validateConfig();
        try {
            String url = buildEndpointUrl();

            String mimeType = image.getContentType() != null ? image.getContentType() : "image/jpeg";
            byte[] bytes = image.getBytes();
            String base64 = Base64.getEncoder().encodeToString(bytes);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("x-goog-api-key", config.getApiKey());

            Map<String, Object> generationConfig = new HashMap<>();
            if (config.getMaxTokens() != null) {
                generationConfig.put("max_output_tokens", config.getMaxTokens());
            }
            generationConfig.put("response_mime_type", "application/json");

            List<Map<String, Object>> contents = new ArrayList<>();
            Map<String, Object> content = new HashMap<>();
            content.put("role", "user");
            List<Map<String, Object>> parts = new ArrayList<>();
            parts.add(Map.of("text", SYSTEM_INSTRUCTION));
            Map<String, Object> inlineData = new HashMap<>();
            inlineData.put("mime_type", mimeType);
            inlineData.put("data", base64);
            parts.add(Map.of("inline_data", inlineData));
            content.put("parts", parts);
            contents.add(content);

            Map<String, Object> body = new HashMap<>();
            body.put("contents", contents);
            body.put("generationConfig", generationConfig);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
            log.info("Gemini Vision API 호출 시작 (image) model={}, url={}, size={} bytes", config.getModel(), url, bytes.length);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);

            return parseGeminiResponse(response.getBody());
        } catch (Exception e) {
            log.error("Gemini 이미지 파싱 실패: {}", e.getMessage(), e);
            throw new RuntimeException("Gemini 이미지 파싱 실패: " + e.getMessage());
        }
    }

    private void resolveApiKeyFromEnv() {
        // Per docs: if both set, GOOGLE_API_KEY takes precedence
        String google = System.getenv("GOOGLE_API_KEY");
        String gemini = System.getenv("GEMINI_API_KEY");
        if (!isBlank(google)) {
            config.setApiKey(google);
        } else if (isBlank(config.getApiKey()) && !isBlank(gemini)) {
            config.setApiKey(gemini);
        }
    }

    private void validateConfig() {
        if (isBlank(config.getApiKey())) {
            throw new IllegalStateException("[Gemini] API key 미설정: 환경변수 GEMINI_API_KEY 또는 gemini.api-key 설정 필요");
        }
        if (isBlank(config.getModel())) {
            throw new IllegalStateException("[Gemini] 모델 미설정: gemini.model 설정 필요");
        }
        if (isBlank(config.getApiUrl())) {
            throw new IllegalStateException("[Gemini] api-url 미설정: gemini.api-url 설정 필요");
        }
    }

    private boolean isBlank(String s) {
        return s == null || s.isBlank();
    }

    private String buildEndpointUrl() {
        String base = config.getApiUrl(); // e.g. https://generativelanguage.googleapis.com/v1beta/models
        if (base.endsWith("/")) base = base.substring(0, base.length() - 1);
        return base + "/" + config.getModel() + ":generateContent"; // no query key; header used
    }

    private ScanPurchaseHistoryResponse parseGeminiResponse(String body) throws Exception {
        if (isBlank(body)) {
            log.warn("Gemini 응답 본문이 비어있어 빈 항목 반환");
            return emptyResponse();
        }
        JsonNode root = objectMapper.readTree(body);

        // API 에러 응답 처리
        if (root.has("error")) {
            String code = root.path("error").path("code").asText("");
            String message = root.path("error").path("message").asText("");
            log.error("Gemini API error: code={}, message={}, raw={}", code, message, abbreviate(body, 500));
            throw new RuntimeException("Gemini API 오류: " + message);
        }

        JsonNode candidates = root.path("candidates");
        if (candidates.isMissingNode() || !candidates.isArray() || candidates.isEmpty()) {
            log.warn("Gemini 응답에 candidates 없음. raw={}", abbreviate(body, 500));
            return emptyResponse();
        }
        JsonNode first = candidates.get(0);
        // promptFeedback 처리 (차단 등)
        if (first.has("finishReason")) {
            String reason = first.path("finishReason").asText("");
            if ("SAFETY".equalsIgnoreCase(reason)) {
                log.warn("Gemini 응답이 SAFETY로 차단됨. raw={}", abbreviate(body, 500));
            }
        }
        JsonNode firstContent = first.path("content").path("parts");
        if (!firstContent.isArray() || firstContent.isEmpty()) {
            log.warn("Gemini 응답 parts 비어있음. raw={}", abbreviate(body, 500));
            return emptyResponse();
        }
        String text = firstContent.get(0).path("text").asText();
        if (isBlank(text)) {
            log.warn("Gemini 텍스트 응답 비어있음. raw={}", abbreviate(body, 500));
            return emptyResponse();
        }

        // JSON 파싱 시도
        JsonNode data;
        try {
            data = objectMapper.readTree(text);
        } catch (Exception e) {
            log.warn("응답을 JSON으로 파싱하지 못해 items 비어있는 기본 구조 반환: raw={}", abbreviate(text, 300));
            return emptyResponse();
        }

        // 제거된 필드 (store, purchaseDate, totalAmount) 더 이상 사용하지 않음
        // List<PurchasedItem> 구성
        List<PurchasedItem> items = new ArrayList<>();
        JsonNode itemsNode = data.path("items");
        if (itemsNode.isArray()) {
            for (JsonNode itemNode : itemsNode) {
                String name = itemNode.path("name").asText(null);
                if (name == null || name.isBlank()) continue;
                int quantity = itemNode.path("quantity").asInt(1);
                quantity = inferQuantityFromName(name, quantity);
                String unit = itemNode.path("unit").asText(null);
                if (unit == null || unit.isBlank()) {
                    unit = inferUnitFromName(name);
                }
                items.add(PurchasedItem.builder()
                        .name(name.trim())
                        .quantity(Math.max(1, quantity))
                        .unit(unit)
                        .build());
            }
        }
        log.info("Gemini 파싱 완료: items={}", items.size());
        return ScanPurchaseHistoryResponse.builder()
                .items(items)
                .build();
    }

    private ScanPurchaseHistoryResponse emptyResponse() {
        return ScanPurchaseHistoryResponse.builder()
                .items(Collections.emptyList())
                .build();
    }

    private String abbreviate(String s, int max) {
        if (s == null) return "";
        return s.length() <= max ? s : s.substring(0, max) + "...";
    }

    private String inferUnitFromName(String name) {
        if (name == null) return null;
        String lower = name.toLowerCase();
        // 단위 패턴 (그램, 밀리리터 등 다양한 표기 허용)
        Pattern numUnit = Pattern.compile("(\\d+(?:[.,]\\d+)?)\\s*(kg|g|ml|l|ℓ|㎖|그램|킬로그램|리터|밀리리터)", Pattern.CASE_INSENSITIVE);
        Matcher m = numUnit.matcher(lower);
        if (m.find()) {
            String u = m.group(2).toLowerCase();
            switch (u) {
                case "kg":
                case "킬로그램":
                    return "kg";
                case "g":
                case "그램":
                    return "g";
                case "ml":
                case "㎖":
                case "밀리리터":
                    return "ml";
                case "l":
                case "ℓ":
                case "리터":
                    return "L";
                default:
                    break; // fallthrough to keyword inference
            }
        }

        // 2) 키워드 기반 추론 (포장/형태)
        String n = name.toLowerCase().replaceAll("\\s+", "");
        if (n.contains("캔")) return "캔";
        if (n.contains("병")) return "병";
        if (n.contains("팩")) return "팩";
        if (n.contains("봉")) return "봉";
        if (n.contains("박스")) return "박스";
        if (n.contains("컵")) return "컵";
        if (n.contains("포") || n.contains("파우치")) return "포";
        if (n.contains("묶음") || n.contains("단")) return "묶음";

        // 3) 식재료 기본 단위 추론
        String[] pieceFoods = {"사과", "배", "바나나", "오이", "호박", "양파", "파", "쪽파", "마늘", "감자", "고구마", "당근", "파프리카", "토마토", "두부", "달걀", "계란", "빵", "꺄르로", "라면", "김밥", "참치"};
        for (String k : pieceFoods) {
            if (n.contains(k)) return "개";
        }
        String[] beverages = {"물", "생수", "음료", "주스", "콜라", "사이다", "탄산수", "차", "커피", "우유", "요구르트"};
        for (String b : beverages) {
            if (n.contains(b)) return "병"; // 음료는 병 기본
        }
        // 4) 기본값
        return "개";
    }

    private int inferQuantityFromName(String name, int fallback) {
        if (name == null) return fallback;
        String n = name.toLowerCase();
        Matcher mx = Pattern.compile("x(\\d+)").matcher(n);
        if (mx.find()) {
            return parsePositive(mx.group(1), fallback);
        }
        Matcher mUnits = Pattern.compile("(\\d+)\\s*(입|개입|개|팩|봉|병|박스|캔|묶음)").matcher(n);
        if (mUnits.find()) {
            return parsePositive(mUnits.group(1), fallback);
        }
        Matcher plusPattern = Pattern.compile("(\\d+)[+](\\d+)").matcher(n);
        if (plusPattern.find()) {
            return parsePositive(plusPattern.group(1), fallback);
        }
        return fallback;
    }

    private int parsePositive(String s, int fallback) {
        try {
            int v = Integer.parseInt(s);
            return v > 0 ? v : fallback;
        } catch (NumberFormatException e) {
            return fallback;
        }
    }
}
