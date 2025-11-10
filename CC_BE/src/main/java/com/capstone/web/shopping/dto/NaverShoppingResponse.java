package com.capstone.web.shopping.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * 네이버 쇼핑 API 응답 DTO
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NaverShoppingResponse {

    private String lastBuildDate;
    private Long total;
    private Integer start;
    private Integer display;
    private List<NaverProduct> items;

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NaverProduct {
        private String title;           // 상품명 (HTML 태그 포함 가능)
        private String link;            // 상품 상세 URL
        private String image;           // 상품 이미지 URL
        private String lprice;          // 최저가 (String)
        private String hprice;          // 최고가 (String, 비어있을 수 있음)
        private String mallName;        // 쇼핑몰명
        private String productId;       // 상품 ID
        private String productType;     // 상품 타입 (1: 카탈로그, 2: 개별상품)
        private String brand;           // 브랜드
        private String maker;           // 제조사
        private String category1;       // 카테고리 1단계
        private String category2;       // 카테고리 2단계
        private String category3;       // 카테고리 3단계
        private String category4;       // 카테고리 4단계
    }
}
