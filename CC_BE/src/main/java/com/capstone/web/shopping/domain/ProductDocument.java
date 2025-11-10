package com.capstone.web.shopping.domain;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.*;

import java.time.LocalDateTime;

/**
 * Elasticsearch 상품 Document
 * 외부 쇼핑몰 API에서 수집한 상품 데이터를 Elasticsearch에 직접 저장
 * RDB를 사용하지 않고 Elasticsearch만 사용
 */
@Document(indexName = "products")
@Setting(settingPath = "elasticsearch/product-settings.json")
@Mapping(mappingPath = "elasticsearch/product-mappings.json")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class ProductDocument {

    /**
     * Elasticsearch 문서 ID
     * 형식: {mallType}_{externalProductId}
     */
    @Id
    private String id;

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
     * 외부 쇼핑몰의 상품 ID
     */
    @Field(type = FieldType.Keyword)
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
