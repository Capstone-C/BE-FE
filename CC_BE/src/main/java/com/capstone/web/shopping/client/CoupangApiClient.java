package com.capstone.web.shopping.client;

import com.capstone.web.shopping.domain.ProductDocument;
import com.capstone.web.shopping.domain.ShoppingMallType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 쿠팡 API 클라이언트
 * 쿠팡 파트너스 API를 통해 상품 데이터 수집
 */
@Component
@Slf4j
public class CoupangApiClient implements ShoppingMallApiClient {

    private final String accessKey;
    private final String secretKey;

    public CoupangApiClient(
            @Value("${coupang.api.access-key:}") String accessKey,
            @Value("${coupang.api.secret-key:}") String secretKey) {
        this.accessKey = accessKey;
        this.secretKey = secretKey;
    }

    @Override
    public List<ProductDocument> searchProducts(String keyword, int maxResults) {
        log.info("Searching Coupang for keyword: {}", keyword);
        
        // TODO: 실제 쿠팡 API 호출 구현
        List<ProductDocument> mockProducts = new ArrayList<>();
        
        if (isConfigured()) {
            log.warn("Coupang API not fully implemented yet. Returning mock data.");
        } else {
            log.warn("Coupang API credentials not configured");
        }
        
        return mockProducts;
    }

    @Override
    public List<ProductDocument> getProductsByCategory(String category, int maxResults) {
        log.info("Getting Coupang products for category: {}", category);
        return new ArrayList<>();
    }

    @Override
    public String getMallType() {
        return ShoppingMallType.COUPANG.name();
    }

    @Override
    public boolean isHealthy() {
        return isConfigured();
    }

    private boolean isConfigured() {
        return accessKey != null && !accessKey.isEmpty() 
                && secretKey != null && !secretKey.isEmpty();
    }
}
