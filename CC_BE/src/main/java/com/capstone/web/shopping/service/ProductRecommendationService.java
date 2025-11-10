package com.capstone.web.shopping.service;

import com.capstone.web.refrigerator.domain.RefrigeratorItem;
import com.capstone.web.refrigerator.repository.RefrigeratorItemRepository;
import com.capstone.web.shopping.client.NaverShoppingApiClient;
import com.capstone.web.shopping.domain.ProductDocument;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * SHOP-01: 냉장고 재료 기반 상품 추천 서비스
 * 
 * 냉장고의 식재료가 떨어지거나 유통기한이 임박했을 때
 * 네이버 쇼핑에서 해당 상품을 검색하여 추천합니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProductRecommendationService {

    private final RefrigeratorItemRepository refrigeratorItemRepository;
    private final NaverShoppingApiClient naverShoppingApiClient;

    /**
     * 추천 트리거 기준을 설정할 수 있는 값들
     * application.yml 또는 환경변수에서 설정 가능
     */
    @Value("${shopping.recommendation.low-quantity-threshold:2}")
    private int lowQuantityThreshold; // 수량이 이 값 이하면 "떨어짐"으로 간주

    @Value("${shopping.recommendation.expiration-days-threshold:3}")
    private int expirationDaysThreshold; // 유통기한이 D-day 이 값 이하면 "임박"으로 간주

    @Value("${shopping.recommendation.max-products-per-item:10}")
    private int maxProductsPerItem; // 식재료당 추천할 최대 상품 개수

    /**
     * 특정 회원의 냉장고를 분석하여 구매가 필요한 식재료와 
     * 해당 식재료의 쇼핑 상품을 추천합니다.
     *
     * @param memberId 회원 ID
     * @return 추천 상품 목록 (식재료명과 상품 리스트)
     */
    public List<RecommendationResult> recommendProductsForMember(Long memberId) {
        log.info("상품 추천 시작 - 회원 ID: {}, 기준(수량: {}, 유통기한: D-{})", 
                memberId, lowQuantityThreshold, expirationDaysThreshold);

        // 1. 구매가 필요한 식재료 찾기
        List<RefrigeratorItem> needPurchase = findItemsNeedingPurchase(memberId);
        log.info("구매 필요 식재료 {}개 발견", needPurchase.size());

        if (needPurchase.isEmpty()) {
            log.info("구매가 필요한 식재료가 없습니다.");
            return List.of();
        }

        // 2. 각 식재료에 대해 네이버 쇼핑 검색
        List<RecommendationResult> results = new ArrayList<>();
        for (RefrigeratorItem item : needPurchase) {
            try {
                log.info("'{}'에 대한 상품 검색 중...", item.getName());
                List<ProductDocument> products = naverShoppingApiClient.searchProducts(
                        item.getName(), 
                        maxProductsPerItem
                );

                RecommendationResult result = RecommendationResult.builder()
                        .ingredientName(item.getName())
                        .reason(determineReason(item))
                        .currentQuantity(item.getQuantity())
                        .expirationDate(item.getExpirationDate())
                        .products(products)
                        .build();

                results.add(result);
                log.info("'{}': {}개 상품 추천", item.getName(), products.size());

                // API 호출 제한 고려
                Thread.sleep(500);
            } catch (Exception e) {
                log.error("'{}' 상품 검색 실패: {}", item.getName(), e.getMessage(), e);
            }
        }

        log.info("상품 추천 완료 - 총 {}개 식재료, {}개 추천 결과", 
                needPurchase.size(), results.size());
        return results;
    }

    /**
     * 구매가 필요한 식재료 찾기
     * - 수량이 임계값 이하
     * - 유통기한이 임박 (D-day가 임계값 이하)
     */
    private List<RefrigeratorItem> findItemsNeedingPurchase(Long memberId) {
        List<RefrigeratorItem> allItems = refrigeratorItemRepository.findByMemberId(memberId);
        LocalDate today = LocalDate.now();

        return allItems.stream()
                .filter(item -> {
                    // 수량이 적음
                    boolean lowQuantity = item.getQuantity() <= lowQuantityThreshold;

                    // 유통기한 임박 (유통기한이 있는 경우만)
                    boolean expirationNear = item.getExpirationDate() != null &&
                            !item.getExpirationDate().minusDays(expirationDaysThreshold).isAfter(today);

                    return lowQuantity || expirationNear;
                })
                .toList();
    }

    /**
     * 추천 사유 결정
     */
    private String determineReason(RefrigeratorItem item) {
        LocalDate today = LocalDate.now();
        boolean lowQuantity = item.getQuantity() <= lowQuantityThreshold;
        boolean expirationNear = item.getExpirationDate() != null &&
                !item.getExpirationDate().minusDays(expirationDaysThreshold).isAfter(today);

        if (lowQuantity && expirationNear) {
            return "수량 부족 & 유통기한 임박";
        } else if (lowQuantity) {
            return "수량 부족";
        } else {
            return "유통기한 임박";
        }
    }

    /**
     * 추천 결과 DTO
     */
    @lombok.Builder
    @lombok.Getter
    public static class RecommendationResult {
        private String ingredientName;      // 식재료명
        private String reason;              // 추천 사유
        private Integer currentQuantity;    // 현재 수량
        private LocalDate expirationDate;   // 유통기한
        private List<ProductDocument> products; // 추천 상품 목록
    }

    // Getter methods for threshold values (테스트 및 관리용)
    public int getLowQuantityThreshold() {
        return lowQuantityThreshold;
    }

    public int getExpirationDaysThreshold() {
        return expirationDaysThreshold;
    }

    public int getMaxProductsPerItem() {
        return maxProductsPerItem;
    }
}
