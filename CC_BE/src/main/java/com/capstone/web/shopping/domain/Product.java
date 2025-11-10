package com.capstone.web.shopping.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * 상품 엔티티
 * 외부 쇼핑몰 API에서 수집한 상품 정보를 저장
 */
@Entity
@Table(name = "products", indexes = {
    @Index(name = "idx_mall_type", columnList = "mall_type"),
    @Index(name = "idx_category", columnList = "category"),
    @Index(name = "idx_created_at", columnList = "created_at")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 상품명
     */
    @Column(nullable = false, length = 500)
    private String name;

    /**
     * 현재 판매가격
     */
    @Column(nullable = false)
    private Integer price;

    /**
     * 원가 (정상가)
     */
    @Column
    private Integer originalPrice;

    /**
     * 상품 이미지 URL
     */
    @Column(length = 1000)
    private String imageUrl;

    /**
     * 상품 상세 페이지 URL
     */
    @Column(nullable = false, length = 1000)
    private String productUrl;

    /**
     * 쇼핑몰 타입
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "mall_type", nullable = false, length = 20)
    private ShoppingMallType mallType;

    /**
     * 상품 카테고리
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ProductCategory category;

    /**
     * 상품 설명
     */
    @Column(columnDefinition = "TEXT")
    private String description;

    /**
     * 배송 정보
     */
    @Column(length = 500)
    private String deliveryInfo;

    /**
     * 평점
     */
    @Column
    private Double rating;

    /**
     * 리뷰 수
     */
    @Column
    private Integer reviewCount;

    /**
     * 외부 쇼핑몰의 상품 ID
     */
    @Column(length = 100)
    private String externalProductId;

    /**
     * 상품 등록일시
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 상품 수정일시
     */
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

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
     * 상품 정보 업데이트
     */
    public void updateProductInfo(String name, Integer price, Integer originalPrice, 
                                   String imageUrl, String description, 
                                   String deliveryInfo, Double rating, Integer reviewCount) {
        this.name = name;
        this.price = price;
        this.originalPrice = originalPrice;
        this.imageUrl = imageUrl;
        this.description = description;
        this.deliveryInfo = deliveryInfo;
        this.rating = rating;
        this.reviewCount = reviewCount;
    }
}
