package com.capstone.web.shopping.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 상품 검색 요청 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "상품 검색 요청")
public class ProductSearchRequest {

    @Schema(description = "검색 키워드", example = "유기농 계란")
    private String keyword;

    @Schema(description = "쇼핑몰 타입 필터", example = "NAVER")
    private String mallType;

    @Schema(description = "카테고리 필터", example = "BEVERAGES")
    private String category;

    @Schema(description = "최소 가격", example = "10000")
    private Integer minPrice;

    @Schema(description = "최대 가격", example = "50000")
    private Integer maxPrice;

    @Schema(description = "정렬 기준 (relevance, price_asc, price_desc, latest)", example = "price_asc")
    @Builder.Default
    private String sortBy = "relevance";

    @Schema(description = "페이지 번호 (0부터 시작)", example = "0")
    @Builder.Default
    private Integer page = 0;

    @Schema(description = "페이지 크기", example = "20")
    @Builder.Default
    private Integer size = 20;
}
