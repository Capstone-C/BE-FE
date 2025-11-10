package com.capstone.web.shopping.scheduler;

import com.capstone.web.shopping.client.NaverShoppingApiClient;
import com.capstone.web.shopping.domain.ProductCategory;
import com.capstone.web.shopping.domain.ProductDocument;
import com.capstone.web.shopping.service.ProductIndexingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 상품 데이터 수집 스케줄러
 * SHOP-01: 매일 새벽 3시에 네이버 쇼핑 API로부터 상품 데이터를 수집하여 Elasticsearch에 저장
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ProductCollectorScheduler {

    private final NaverShoppingApiClient naverShoppingApiClient;
    private final ProductIndexingService productIndexingService;

    /**
     * 매일 새벽 3시에 상품 데이터 수집
     * 모든 카테고리에 대해 상품을 수집하고 Elasticsearch에 저장
     */
    @Scheduled(cron = "0 0 3 * * *")
    public void collectProducts() {
        log.info("====== Starting scheduled product collection ======");
        long startTime = System.currentTimeMillis();

        try {
            List<ProductDocument> allProducts = new ArrayList<>();

            // 모든 카테고리에 대해 상품 수집
            for (ProductCategory category : ProductCategory.values()) {
                try {
                    log.info("Collecting products for category: {}", category);
                    List<ProductDocument> products = naverShoppingApiClient.searchProducts(
                            category.getDisplayName(), // "채소", "과일" 등
                            100 // 카테고리당 100개 수집
                    );
                    allProducts.addAll(products);
                    log.info("Collected {} products for category: {}", products.size(), category);

                    // API 호출 제한을 고려하여 각 카테고리 수집 사이에 1초 대기
                    Thread.sleep(1000);
                } catch (Exception e) {
                    log.error("Failed to collect products for category: {}. Error: {}", 
                            category, e.getMessage(), e);
                    // 하나의 카테고리 실패가 전체 수집을 중단하지 않도록 계속 진행
                }
            }

            // 수집한 모든 상품을 Elasticsearch에 일괄 저장
            if (!allProducts.isEmpty()) {
                log.info("Starting bulk indexing of {} products", allProducts.size());
                productIndexingService.bulkIndexProducts(allProducts);
                log.info("Bulk indexing completed successfully");
            } else {
                log.warn("No products collected");
            }

            long duration = System.currentTimeMillis() - startTime;
            log.info("====== Product collection completed in {} ms. Total products: {} ======", 
                    duration, allProducts.size());

        } catch (Exception e) {
            log.error("Fatal error during product collection: {}", e.getMessage(), e);
        }
    }

    /**
     * 수동으로 상품 수집을 트리거할 수 있는 메서드 (테스트 및 수동 실행용)
     * Spring Boot Actuator를 통해 호출 가능
     */
    public void collectProductsManually() {
        log.info("Manual product collection triggered");
        collectProducts();
    }
}
