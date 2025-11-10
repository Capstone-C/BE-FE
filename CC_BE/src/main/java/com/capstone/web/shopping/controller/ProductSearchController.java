package com.capstone.web.shopping.controller;

import com.capstone.web.shopping.domain.ProductDocument;
import com.capstone.web.shopping.dto.ProductSearchRequest;
import com.capstone.web.shopping.dto.ProductSearchResponse;
import com.capstone.web.shopping.service.ProductSearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 상품 검색 컨트롤러
 * SHOP-01: 통합 쇼핑몰 상품 검색 API
 */
@RestController
@RequestMapping("/api/v1/shopping")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Shopping", description = "쇼핑몰 상품 검색 API")
public class ProductSearchController {

    private final ProductSearchService productSearchService;

    /**
     * 상품 검색 API
     * 키워드, 가격 범위, 쇼핑몰 타입, 카테고리로 필터링하여 상품을 검색
     * 
     * @param request 검색 조건 (keyword, mallType, category, minPrice, maxPrice, sortBy, page, size)
     * @return 검색 결과 (상품 목록, 총 개수, 페이지 정보)
     */
    @GetMapping("/search")
    @Operation(
        summary = "상품 검색",
        description = "키워드, 가격 범위, 쇼핑몰 타입, 카테고리로 상품을 검색합니다. " +
                      "페이징과 정렬을 지원합니다. " +
                      "인증 없이 접근 가능합니다."
    )
    public ResponseEntity<ProductSearchResponse> searchProducts(
            @ModelAttribute ProductSearchRequest request) {
        
        log.info("Search request received: keyword={}, mallType={}, category={}, " +
                "minPrice={}, maxPrice={}, sortBy={}, page={}, size={}",
                request.getKeyword(), request.getMallType(), request.getCategory(),
                request.getMinPrice(), request.getMaxPrice(), request.getSortBy(),
                request.getPage(), request.getSize());

        Page<ProductDocument> searchResults = productSearchService.searchProducts(request);
        ProductSearchResponse response = ProductSearchResponse.from(searchResults);

        log.info("Search completed: found {} products, page {}/{}",
                response.getTotalCount(), response.getCurrentPage() + 1, response.getTotalPages());

        return ResponseEntity.ok(response);
    }
}
