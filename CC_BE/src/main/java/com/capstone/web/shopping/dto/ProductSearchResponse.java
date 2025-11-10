package com.capstone.web.shopping.dto;

import com.capstone.web.shopping.domain.ProductDocument;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 상품 검색 응답 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "상품 검색 응답")
public class ProductSearchResponse {

    @Schema(description = "검색된 상품 목록")
    private List<ProductDto> products;

    @Schema(description = "전체 상품 개수", example = "178")
    private Long totalCount;

    @Schema(description = "현재 페이지 번호", example = "0")
    private Integer currentPage;

    @Schema(description = "전체 페이지 수", example = "9")
    private Integer totalPages;

    @Schema(description = "페이지 크기", example = "20")
    private Integer pageSize;

    @Schema(description = "필터링 집계 정보 (쇼핑몰별 개수 등)")
    private Map<String, Object> aggregations;

    /**
     * 상품 DTO
     */
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "상품 정보")
    public static class ProductDto {

        @Schema(description = "상품 ID", example = "NAVER_12345")
        private String id;

        @Schema(description = "상품명", example = "유기농 계란 30구")
        private String name;

        @Schema(description = "가격", example = "12000")
        private Integer price;

        @Schema(description = "원가", example = "15000")
        private Integer originalPrice;

        @Schema(description = "할인율", example = "20")
        private Integer discountRate;

        @Schema(description = "이미지 URL")
        private String imageUrl;

        @Schema(description = "상품 상세 URL")
        private String productUrl;

        @Schema(description = "쇼핑몰 타입", example = "NAVER")
        private String mallType;

        @Schema(description = "카테고리", example = "BEVERAGES")
        private String category;

        @Schema(description = "평점", example = "4.5")
        private Double rating;

        @Schema(description = "리뷰 수", example = "128")
        private Integer reviewCount;

        @Schema(description = "배송 정보", example = "무료배송")
        private String deliveryInfo;

        /**
         * ProductDocument를 ProductDto로 변환
         */
        public static ProductDto from(ProductDocument document) {
            return ProductDto.builder()
                    .id(document.getId())
                    .name(document.getName())
                    .price(document.getPrice())
                    .originalPrice(document.getOriginalPrice())
                    .discountRate(document.getDiscountRate())
                    .imageUrl(document.getImageUrl())
                    .productUrl(document.getProductUrl())
                    .mallType(document.getMallType())
                    .category(document.getCategory())
                    .rating(document.getRating())
                    .reviewCount(document.getReviewCount())
                    .deliveryInfo(document.getDeliveryInfo())
                    .build();
        }
    }

    /**
     * Page<ProductDocument>를 ProductSearchResponse로 변환
     */
    public static ProductSearchResponse from(org.springframework.data.domain.Page<ProductDocument> page) {
        return ProductSearchResponse.builder()
                .products(page.getContent().stream()
                        .map(ProductDto::from)
                        .collect(Collectors.toList()))
                .totalCount(page.getTotalElements())
                .currentPage(page.getNumber())
                .totalPages(page.getTotalPages())
                .pageSize(page.getSize())
                .build();
    }
}
