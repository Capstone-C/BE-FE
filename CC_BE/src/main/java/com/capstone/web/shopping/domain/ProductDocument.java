package com.capstone.web.shopping.domain;

import lombok.*;

import java.time.LocalDateTime;

/**
 * 쇼핑 상품 데이터 모델
 * Naver Shopping API에서 수집한 상품 데이터
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class ProductDocument {

    /**
     * 상품 ID
     * 형식: {mallType}_{externalProductId}
     */
    private String id;

    /**
     * 상품명
     */
    private String name;

    /**
     * 현재 판매가격
     */
    private Integer price;

    /**
     * 원가 (정상가)
     */
    private Integer originalPrice;

    /**
     * 할인율
     */
    private Integer discountRate;

    /**
     * 상품 이미지 URL
     */
    private String imageUrl;

    /**
     * 상품 상세 페이지 URL
     */
    private String productUrl;

    /**
     * 쇼핑몰 타입
     */
    private String mallType;

    /**
     * 상품 카테고리
     */
    private String category;

    /**
     * 상품 설명
     */
    private String description;

    /**
     * 배송 정보
     */
    private String deliveryInfo;

    /**
     * 평점
     */
    private Double rating;

    /**
     * 리뷰 수
     */
    private Integer reviewCount;

    /**
     * 상품 등록일시
     */
    private LocalDateTime createdAt;

    /**
     * 상품 수정일시
     */
    private LocalDateTime updatedAt;

    /**
     * 외부 쇼핑몰의 상품 ID
     */
    private String externalProductId;

    /**
     * 할인율 계산
     */
    public Integer getDiscountRate() {
        if (originalPrice == null || originalPrice <= 0 || price >= originalPrice) {
            return 0;
        }
        return (int) Math.round((double) (originalPrice - price) / originalPrice * 100);
    }

    /**
     * Document ID 생성 헬퍼 메서드
     * 형식: {mallType}_{externalProductId}
     */
    public static String generateId(String mallType, String externalProductId) {
        return mallType + "_" + externalProductId;
    }

    /**
     * 상품 정보 업데이트 (Elasticsearch 문서 갱신용)
     */
    public ProductDocument updateInfo(String name, Integer price, Integer originalPrice,
                                      String imageUrl, String description,
                                      String deliveryInfo, Double rating, Integer reviewCount) {
        return ProductDocument.builder()
                .id(this.id)
                .name(name)
                .price(price)
                .originalPrice(originalPrice)
                .imageUrl(imageUrl)
                .productUrl(this.productUrl)
                .mallType(this.mallType)
                .category(this.category)
                .description(description)
                .deliveryInfo(deliveryInfo)
                .rating(rating)
                .reviewCount(reviewCount)
                .externalProductId(this.externalProductId)
                .createdAt(this.createdAt)
                .updatedAt(LocalDateTime.now())
                .build();
    }
}
