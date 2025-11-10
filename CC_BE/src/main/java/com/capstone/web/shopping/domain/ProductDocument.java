package com.capstone.web.shopping.domain;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.*;

import java.time.LocalDateTime;

/**
 * Elasticsearch 상품 검색용 Document
 * RDB의 Product 엔티티와 동기화되어 검색에 최적화된 구조로 저장
 */
@Document(indexName = "products")
@Setting(settingPath = "elasticsearch/product-settings.json")
@Mapping(mappingPath = "elasticsearch/product-mappings.json")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class ProductDocument {

    @Id
    private String id;

    /**
     * RDB의 Product 엔티티 ID
     */
    @Field(type = FieldType.Long)
    private Long productId;

    /**
     * 상품명 (검색 대상)
     */
    @Field(type = FieldType.Text, analyzer = "nori")
    private String name;

    /**
     * 현재 판매가격
     */
    @Field(type = FieldType.Integer)
    private Integer price;

    /**
     * 원가 (정상가)
     */
    @Field(type = FieldType.Integer)
    private Integer originalPrice;

    /**
     * 할인율
     */
    @Field(type = FieldType.Integer)
    private Integer discountRate;

    /**
     * 상품 이미지 URL
     */
    @Field(type = FieldType.Keyword, index = false)
    private String imageUrl;

    /**
     * 상품 상세 페이지 URL
     */
    @Field(type = FieldType.Keyword, index = false)
    private String productUrl;

    /**
     * 쇼핑몰 타입
     */
    @Field(type = FieldType.Keyword)
    private String mallType;

    /**
     * 상품 카테고리
     */
    @Field(type = FieldType.Keyword)
    private String category;

    /**
     * 상품 설명 (검색 대상)
     */
    @Field(type = FieldType.Text, analyzer = "nori")
    private String description;

    /**
     * 배송 정보
     */
    @Field(type = FieldType.Text)
    private String deliveryInfo;

    /**
     * 평점
     */
    @Field(type = FieldType.Double)
    private Double rating;

    /**
     * 리뷰 수
     */
    @Field(type = FieldType.Integer)
    private Integer reviewCount;

    /**
     * 상품 등록일시
     */
    @Field(type = FieldType.Date, format = DateFormat.date_hour_minute_second)
    private LocalDateTime createdAt;

    /**
     * 상품 수정일시
     */
    @Field(type = FieldType.Date, format = DateFormat.date_hour_minute_second)
    private LocalDateTime updatedAt;

    /**
     * Product 엔티티로부터 Document 생성
     */
    public static ProductDocument from(Product product) {
        return ProductDocument.builder()
                .productId(product.getId())
                .name(product.getName())
                .price(product.getPrice())
                .originalPrice(product.getOriginalPrice())
                .discountRate(product.getDiscountRate())
                .imageUrl(product.getImageUrl())
                .productUrl(product.getProductUrl())
                .mallType(product.getMallType().name())
                .category(product.getCategory().name())
                .description(product.getDescription())
                .deliveryInfo(product.getDeliveryInfo())
                .rating(product.getRating())
                .reviewCount(product.getReviewCount())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }
}
