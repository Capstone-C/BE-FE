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

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.*;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.imageio.ImageIO;

/**
 * Google Gemini API를 사용한 영수증/상품 스크린샷 파싱 서비스
 * (Vision generateContent 엔드포인트 v1beta 사용)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GeminiService {

    private static final String SYSTEM_INSTRUCTION = String.join(" ",
            "You are a Korean receipt OCR and item extractor.",
            "The image will mainly be a paper receipt.",
            "Return ONLY JSON with keys: items (array).",
            "Each item has: name (string), quantity (int, default 1), unit (string or null from ['개','팩','봉','병','캔','컵','박스','g','kg','ml','L','포','묶음']).",
            "For each line item on the receipt, set name to the FULL product name including volume or weight as it appears, e.g. '라라스윗 바닐라파인트 474ml', '서울 저지방우유 1L', '삼겹살 500g'.",
            "Do NOT simplify, shorten, or normalize the name. Never trim brand names, flavors, or numeric volume/weight information.",
            "Use quantity ONLY for counts (예: '2개', '3팩', 'x2') and NOT for milliliter/gram values.",
            "When you see patterns like '474ml', '500ml', '1.5L', '130g' in a line item, keep them as part of the name string instead of mapping them to quantity.",
            "Unit can still be set to a simple piece unit (개/팩/봉/캔 등) or ml/g/L/kg if clearly indicated, but the numeric value should remain inside the name.",
            "For example, the receipt item '라라스윗)바닐라파인트474' should be represented in JSON as: { name: '라라스윗 바닐라파인트 474ml', quantity: 1, unit: 'ml' }.",
            "Ignore dates, order statuses, links, totals, discounts, barcodes, and payment info.",
            "Output example: {\"items\":[{\"name\":\"피코크 초마짬뽕 4개입\",\"quantity\":1,\"unit\":\"개\"},{\"name\":\"라라스윗 바닐라파인트 474ml\",\"quantity\":1,\"unit\":\"ml\"}] }.",
            "No extra text.");

    private final GeminiConfig config;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    /**
     * 이미지(영수증/상품 스크린샷)를 Gemini Vision 모델로 파싱하여 구조화된 구매 이력 반환
     */
    public ScanPurchaseHistoryResponse parseReceiptImage(MultipartFile image) {
        resolveApiKeyFromEnv();
        validateConfig();
        try {
            String url = buildEndpointUrl();

            String mimeType = image.getContentType() != null ? image.getContentType() : "image/jpeg";
            byte[] originalBytes = image.getBytes();
            byte[] bytesForModel = maybeUpscaleSmallImage(originalBytes, mimeType);
            String base64 = Base64.getEncoder().encodeToString(bytesForModel);

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
            log.info("Gemini Vision API 호출 시작 (image) model={}, url={}, size={} bytes", config.getModel(), url, bytesForModel.length);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);

            return parseGeminiResponse(response.getBody());
        } catch (Exception e) {
            log.error("Gemini 이미지 파싱 실패: {}", e.getMessage(), e);
            throw new RuntimeException("Gemini 이미지 파싱 실패: " + e.getMessage());
        }
    }

    private void resolveApiKeyFromEnv() {
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
        String base = config.getApiUrl();
        if (base.endsWith("/")) base = base.substring(0, base.length() - 1);
        return base + "/" + config.getModel() + ":generateContent";
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

        // 1차: JSON 직접 파싱
        try {
            JsonNode data = objectMapper.readTree(text);
            List<PurchasedItem> items = new ArrayList<>();
            JsonNode itemsNode = data.path("items");
            if (itemsNode.isArray()) {
                for (JsonNode itemNode : itemsNode) {
                    String name = itemNode.path("name").asText(null);
                    if (name == null || name.isBlank()) continue;
                    int quantity = itemNode.path("quantity").asInt(1);
                    quantity = inferQuantityFromName(name, quantity);
                    String unit = itemNode.path("unit").asText(null);
                    if (unit == null || unit.isBlank()) unit = inferUnitFromName(name);
                    items.add(PurchasedItem.builder()
                            // 더 이상 cleanName으로 축약하지 않고 전체 이름 그대로 사용
                            .name(name.trim())
                            .quantity(Math.max(1, quantity))
                            .unit(unit)
                            // weight intentionally ignored in DTO for compatibility
                            .build());
                }
            }
            log.info("Gemini 파싱 완료(JSON): items={}", items.size());
            return ScanPurchaseHistoryResponse.builder().items(items).build();
        } catch (Exception jsonFail) {
            // 2차: fallback – 자유 텍스트에서 식재료 힌트를 추출
            List<PurchasedItem> items = fallbackFromText(text);
            log.info("Gemini 파싱 완료(fallback): items={}", items.size());
            return ScanPurchaseHistoryResponse.builder().items(items).build();
        }
    }

    private ScanPurchaseHistoryResponse emptyResponse() {
        return ScanPurchaseHistoryResponse.builder().items(Collections.emptyList()).build();
    }

    private String abbreviate(String s, int max) {
        if (s == null) return "";
        return s.length() <= max ? s : s.substring(0, max) + "...";
    }

    // --- 이미지 업스케일 (작은 스크린샷 보정) ---
    private byte[] maybeUpscaleSmallImage(byte[] bytes, String mimeType) {
        try {
            BufferedImage src = ImageIO.read(new ByteArrayInputStream(bytes));
            if (src == null) return bytes;
            int w = src.getWidth();
            int h = src.getHeight();
            int min = Math.min(w, h);
            if (min >= 400) return bytes; // 충분히 큼
            double scale = 400.0 / min; // 최소 변 400으로 스케일업
            int nw = (int) Math.round(w * scale);
            int nh = (int) Math.round(h * scale);
            BufferedImage dst = new BufferedImage(nw, nh, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = dst.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.drawImage(src, 0, 0, nw, nh, null);
            g.dispose();
            String format = mimeType != null && mimeType.toLowerCase().contains("png") ? "png" : "jpg";
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ImageIO.write(dst, format, bos);
            return bos.toByteArray();
        } catch (Exception e) {
            return bytes; // 실패 시 원본 유지
        }
    }

    // --- 이름 정리: 마케팅 수식어 제거 ---
    private String cleanName(String name) {
        if (name == null) return null;
        return name.trim();
    }

    private String inferUnitFromName(String name) {
        if (name == null) return null;
        String lower = name.toLowerCase();
        Pattern numUnit = Pattern.compile("(\\d+(?:[.,]\\d+)?)\\s*(kg|g|ml|l|ℓ|그램|킬로그램|리터|밀리리터)", Pattern.CASE_INSENSITIVE);
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
                    break;
            }
        }
        String n = name.toLowerCase().replaceAll("\\s+", "");
        if (n.contains("캔")) return "캔";
        if (n.contains("병")) return "병";
        if (n.contains("팩")) return "팩";
        if (n.contains("봉")) return "봉";
        if (n.contains("박스")) return "박스";
        if (n.contains("컵")) return "컵";
        if (n.contains("포") || n.contains("파우치")) return "포";
        if (n.contains("묶음") || n.contains("단")) return "묶음";
        String[] pieceFoods = {"사과", "배", "바나나", "오이", "호박", "양파", "파", "쪽파", "마늘", "감자", "고구마", "당근", "파프리카", "토마토", "두부", "달걀", "계란", "빵", "라면", "김밥", "참치", "참외", "수박", "귤", "오렌지", "딸기", "포도"};
        for (String k : pieceFoods) if (n.contains(k)) return "개";
        String[] beverages = {"물", "생수", "음료", "주스", "콜라", "사이다", "탄산수", "차", "커피", "우유", "요구르트"};
        for (String b : beverages) if (n.contains(b)) return "병";
        return "개";
    }

    private int inferQuantityFromName(String name, int fallback) {
        if (name == null) return fallback;
        String n = name.toLowerCase();
        Matcher mx = Pattern.compile("x(\\d+)").matcher(n);
        if (mx.find()) return parsePositive(mx.group(1), fallback);
        Matcher mUnits = Pattern.compile("(\\d+)\\s*(입|개입|개|팩|봉|병|박스|캔|묶음)").matcher(n);
        if (mUnits.find()) return parsePositive(mUnits.group(1), fallback);
        Matcher plusPattern = Pattern.compile("(\\d+)[+](\\d+)").matcher(n);
        if (plusPattern.find()) return parsePositive(plusPattern.group(1), fallback);
        // 소수 무게가 포함된 경우는 quantity=1 유지 (무게는 별도 weight에서 처리)
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

    // --- 텍스트 기반 Fallback 파싱 ---
    private List<PurchasedItem> fallbackFromText(String text) {
        if (isBlank(text)) return Collections.emptyList();
        String raw = text.replace('\r', '\n');
        String cleaned = raw
                .replaceAll("https?://\\S+", " ")
                .replaceAll("[|▶▷•·●■◆◇☆★]+", " ")
                .replace("\\u00a0", " ")
                .trim();

        // 수량: N 패턴 우선 추출
        Integer qty = null;
        Matcher qMatcher = Pattern.compile("수량\\s*[:=]\\s*(\\d+)").matcher(cleaned);
        if (qMatcher.find()) {
            qty = parsePositive(qMatcher.group(1), 1);
        }

        // 무게/용량 추출 (예: 4.5kg, 500g, 1L, 250ml)
        Double weight = null;
        String unit = null;
        Matcher wMatcher = Pattern.compile("(\\d+(?:[.,]\\d+)?)\\s*(kg|g|ml|l|ℓ)", Pattern.CASE_INSENSITIVE).matcher(cleaned);
        if (wMatcher.find()) {
            try {
                weight = Double.parseDouble(wMatcher.group(1).replace(',', '.'));
            } catch (Exception ignore) {
            }
            String u = wMatcher.group(2).toLowerCase();
            unit = ("kg".equals(u) || "킬로그램".equals(u)) ? "kg" :
                    ("g".equals(u) || "그램".equals(u)) ? "g" :
                            ("ml".equals(u) || "㎖".equals(u) || "밀리리터".equals(u)) ? "ml" :
                                    ("l".equals(u) || "ℓ".equals(u) || "리터".equals(u)) ? "L" : null;
        }

        // 식재료 후보 추출: 사전 + 한글 명사성 토큰 중 흔한 식재료 키워드
        String[] candidates = cleaned.split("[\n,/]|\\s{2,}");
        Set<String> foods = new LinkedHashSet<>(Arrays.asList(
                "사과", "배", "바나나", "오렌지", "귤", "포도", "딸기", "블루베리", "참외", "수박", "키위", "레몬", "라임",
                "양파", "파", "쪽파", "마늘", "감자", "고구마", "당근", "호박", "오이", "토마토", "파프리카", "브로콜리", "시금치",
                "우유", "요거트", "요구르트", "치즈", "두부", "계란", "달걀", "빵", "햄", "소세지", "베이컨", "라면", "김밥", "참치"
        ));
        String[] ban = {"배송", "도착", "예정", "준비", "주문", "결제", "옵션", "색상", "사이즈", "가격", "할인", "쿠폰", "포인트", "적립", "링크", "리뷰", "평점", "문의", "상품", "구매", "장바구니"};

        String name = null;
        outer:
        for (String token : candidates) {
            String t = token.trim();
            if (t.isEmpty()) continue;
            boolean banned = false;
            for (String b : ban) {
                if (t.contains(b)) {
                    banned = true;
                    break;
                }
            }
            if (banned) continue;
            for (String f : foods) {
                if (t.contains(f)) {
                    name = f;
                    break outer;
                }
            }
        }

        if (name == null) {
            // 가벼운 휴리스틱: 한글 2~6자 단어들 중 마지막 유용 토큰 시도
            Matcher mKo = Pattern.compile("[가-힣]{2,6}").matcher(cleaned.replaceAll("\\s+", ""));
            String last = null;
            while (mKo.find()) last = mKo.group();
            if (last != null) name = last;
        }

        if (name == null) return Collections.emptyList();

        // fallback에서도 이제는 cleanName으로 축약하지 않고, 추출된 이름 전체를 사용
        String finalName = name.trim();
        int quantity = qty != null ? Math.max(1, qty) : 1;
        String finalUnit = unit != null ? unit : inferUnitFromName(finalName);

        PurchasedItem item = PurchasedItem.builder()
                .name(finalName)
                .quantity(quantity)
                .unit(finalUnit)
                // .weight(weight) // optional: not used to keep builder compatibility
                .build();
        return Collections.singletonList(item);
    }
}
