package com.capstone.web.shopping.client;

import com.capstone.web.shopping.domain.ProductCategory;
import com.capstone.web.shopping.domain.ProductDocument;
import com.capstone.web.shopping.domain.ShoppingMallType;
import com.capstone.web.shopping.dto.NaverShoppingResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 네이버 쇼핑 API 클라이언트
 * 네이버 쇼핑 검색 API를 통해 상품 데이터 수집
 */
@Component
@Slf4j
public class NaverShoppingApiClient implements ShoppingMallApiClient {

    private static final String NAVER_SHOPPING_API_URL = "https://openapi.naver.com/v1/search/shop.json";
    
    private final RestTemplate restTemplate;
    private final String clientId;
    private final String clientSecret;

    public NaverShoppingApiClient(
            RestTemplate restTemplate,
            @Value("${naver.api.client-id:}") String clientId,
            @Value("${naver.api.client-secret:}") String clientSecret) {
        this.restTemplate = restTemplate;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
    }

    @Override
    public List<ProductDocument> searchProducts(String keyword, int maxResults) {
        if (!isConfigured()) {
            log.warn("Naver API credentials not configured. Returning empty list.");
            return new ArrayList<>();
        }

        log.info("Searching Naver Shopping API for keyword: '{}', maxResults: {}", keyword, maxResults);
        
        try {
            // API URL 구성
            String url = UriComponentsBuilder.fromUriString(NAVER_SHOPPING_API_URL)
                    .queryParam("query", keyword)
                    .queryParam("display", Math.min(maxResults, 100)) // 네이버 API 최대 100개
                    .queryParam("start", 1)
                    .queryParam("sort", "sim") // sim: 정확도순, date: 날짜순, asc/dsc: 가격순
                    .build()
                    .toUriString();

            // 헤더 설정
            HttpHeaders headers = new HttpHeaders();
            headers.set("X-Naver-Client-Id", clientId);
            headers.set("X-Naver-Client-Secret", clientSecret);
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<String> entity = new HttpEntity<>(headers);

            // API 호출
            ResponseEntity<NaverShoppingResponse> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    NaverShoppingResponse.class
            );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                NaverShoppingResponse body = response.getBody();
                log.info("Successfully fetched {} products from Naver (total: {})", 
                        body.getItems().size(), body.getTotal());
                
                return body.getItems().stream()
                        .map(this::convertToProductDocument)
                        .collect(Collectors.toList());
            } else {
                log.warn("Unexpected response from Naver API: {}", response.getStatusCode());
                return new ArrayList<>();
            }

        } catch (RestClientException e) {
            log.error("Error calling Naver Shopping API for keyword: {}", keyword, e);
            return new ArrayList<>();
        }
    }

    @Override
    public List<ProductDocument> getProductsByCategory(String category, int maxResults) {
        // 카테고리를 검색 키워드로 변환
        String keyword = mapCategoryToKeyword(category);
        log.info("Getting Naver products for category: {} (keyword: {})", category, keyword);
        return searchProducts(keyword, maxResults);
    }

    @Override
    public String getMallType() {
        return ShoppingMallType.NAVER.name();
    }

    @Override
    public boolean isHealthy() {
        if (!isConfigured()) {
            return false;
        }

        try {
            // 간단한 테스트 쿼리로 API 상태 확인
            searchProducts("테스트", 1);
            return true; // API 호출 성공
        } catch (Exception e) {
            log.error("Naver API health check failed", e);
            return false;
        }
    }

    private boolean isConfigured() {
        return clientId != null && !clientId.isEmpty() 
                && clientSecret != null && !clientSecret.isEmpty();
    }

    /**
     * 네이버 API 응답을 ProductDocument로 변환
     */
    private ProductDocument convertToProductDocument(NaverShoppingResponse.NaverProduct naverProduct) {
        // HTML 태그 제거 (title에 <b> 태그가 포함되어 있음)
        String cleanTitle = removeHtmlTags(naverProduct.getTitle());
        
        // 가격 파싱 (String -> Integer)
        Integer price = parsePrice(naverProduct.getLprice());
        Integer originalPrice = parsePrice(naverProduct.getHprice());
        
        // 카테고리 매핑
        String category = mapNaverCategoryToOurCategory(
                naverProduct.getCategory1(),
                naverProduct.getCategory2(),
                naverProduct.getCategory3()
        );

        // Document ID 생성
        String documentId = ProductDocument.generateId(
                ShoppingMallType.NAVER.name(),
                naverProduct.getProductId()
        );

        return ProductDocument.builder()
                .id(documentId)
                .name(cleanTitle)
                .price(price)
                .originalPrice(originalPrice)
                .imageUrl(naverProduct.getImage())
                .productUrl(naverProduct.getLink())
                .mallType(ShoppingMallType.NAVER.name())
                .category(category)
                .description(naverProduct.getBrand() + " / " + naverProduct.getMaker())
                .deliveryInfo(naverProduct.getMallName() + " 배송")
                .rating(null) // 네이버 API에서 제공하지 않음
                .reviewCount(null) // 네이버 API에서 제공하지 않음
                .externalProductId(naverProduct.getProductId())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    /**
     * HTML 태그 제거
     */
    private String removeHtmlTags(String text) {
        if (text == null) return "";
        return text.replaceAll("<[^>]*>", "");
    }

    /**
     * 가격 문자열 파싱
     */
    private Integer parsePrice(String priceStr) {
        if (priceStr == null || priceStr.isEmpty()) {
            return null;
        }
        try {
            return Integer.parseInt(priceStr);
        } catch (NumberFormatException e) {
            log.warn("Failed to parse price: {}", priceStr);
            return null;
        }
    }

    /**
     * 네이버 카테고리를 우리 시스템의 ProductCategory로 매핑
     */
    private String mapNaverCategoryToOurCategory(String cat1, String cat2, String cat3) {
        // 식품 관련 카테고리 매핑
        if ("식품".equals(cat1)) {
            if (cat2 != null) {
                if (cat2.contains("채소")) return ProductCategory.VEGETABLES.name();
                if (cat2.contains("과일")) return ProductCategory.FRUITS.name();
                if (cat2.contains("정육") || cat2.contains("고기")) return ProductCategory.MEAT.name();
                if (cat2.contains("수산") || cat2.contains("해산물")) return ProductCategory.SEAFOOD.name();
                if (cat2.contains("유제품") || cat2.contains("우유") || cat2.contains("치즈")) 
                    return ProductCategory.DAIRY.name();
                if (cat2.contains("쌀") || cat2.contains("곡물")) return ProductCategory.GRAINS.name();
                if (cat2.contains("음료")) return ProductCategory.BEVERAGES.name();
                if (cat2.contains("양념") || cat2.contains("조미료")) return ProductCategory.SEASONINGS.name();
                if (cat2.contains("과자") || cat2.contains("간식")) return ProductCategory.SNACKS.name();
            }
            return ProductCategory.PROCESSED.name(); // 기타 식품
        }
        
        return ProductCategory.ETC.name();
    }

    /**
     * ProductCategory를 검색 키워드로 변환
     */
    private String mapCategoryToKeyword(String category) {
        try {
            ProductCategory cat = ProductCategory.valueOf(category);
            return cat.getDisplayName();
        } catch (IllegalArgumentException e) {
            log.warn("Unknown category: {}", category);
            return category;
        }
    }
}

