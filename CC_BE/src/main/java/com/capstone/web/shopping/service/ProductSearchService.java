package com.capstone.web.shopping.service;

import com.capstone.web.shopping.client.NaverShoppingApiClient;
import com.capstone.web.shopping.domain.ProductDocument;
import com.capstone.web.shopping.dto.ProductSearchRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 상품 검색 서비스
 * SHOP-01: Naver API 직접 호출 방식으로 상품 검색
 * Elasticsearch 제거 - 검색 결과 필터링은 메모리에서 처리
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ProductSearchService {

    private final NaverShoppingApiClient naverShoppingApiClient;

    /**
     * 상품 검색
     * Naver API를 직접 호출하여 실시간 검색 결과 반환
     */
    public Page<ProductDocument> searchProducts(ProductSearchRequest request) {
        log.info("Searching products with keyword: {}, mallType: {}, category: {}, priceRange: {}-{}",
                request.getKeyword(), request.getMallType(), request.getCategory(),
                request.getMinPrice(), request.getMaxPrice());

        String keyword = request.getKeyword();
        if (keyword == null || keyword.isBlank()) {
            log.warn("Keyword is required for product search");
            return Page.empty();
        }

        // Naver API 호출 (최대 100개)
        List<ProductDocument> allProducts = naverShoppingApiClient.searchProducts(keyword.trim(), 100);
        
        if (allProducts.isEmpty()) {
            log.info("No products found for keyword: '{}'", keyword);
            return Page.empty();
        }

        // 필터링 적용
        List<ProductDocument> filteredProducts = applyFilters(allProducts, request);
        
        // 정렬 적용
        List<ProductDocument> sortedProducts = applySorting(filteredProducts, request.getSortBy());
        
        // 페이징 적용
        Pageable pageable = createPageable(request);
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), sortedProducts.size());
        
        List<ProductDocument> pagedProducts = start < sortedProducts.size() 
                ? sortedProducts.subList(start, end) 
                : new ArrayList<>();

        log.info("Found {} products for keyword: '{}' (after filters: {}, page: {}/{})",
                allProducts.size(), keyword, sortedProducts.size(), 
                pageable.getPageNumber() + 1, (sortedProducts.size() + pageable.getPageSize() - 1) / pageable.getPageSize());

        return new PageImpl<>(pagedProducts, pageable, sortedProducts.size());
    }

    /**
     * 필터 적용: 카테고리, 가격 범위
     */
    private List<ProductDocument> applyFilters(List<ProductDocument> products, ProductSearchRequest request) {
        return products.stream()
                .filter(product -> matchesCategory(product, request.getCategory()))
                .filter(product -> matchesPriceRange(product, request.getMinPrice(), request.getMaxPrice()))
                .collect(Collectors.toList());
    }

    /**
     * 카테고리 필터
     */
    private boolean matchesCategory(ProductDocument product, String category) {
        if (category == null || category.isBlank()) {
            return true;
        }
        return category.equalsIgnoreCase(product.getCategory());
    }

    /**
     * 가격 범위 필터
     */
    private boolean matchesPriceRange(ProductDocument product, Integer minPrice, Integer maxPrice) {
        if (product.getPrice() == null) {
            return false;
        }
        
        if (minPrice != null && product.getPrice() < minPrice) {
            return false;
        }
        
        if (maxPrice != null && product.getPrice() > maxPrice) {
            return false;
        }
        
        return true;
    }

    /**
     * 정렬 적용
     */
    private List<ProductDocument> applySorting(List<ProductDocument> products, String sortBy) {
        if (sortBy == null || sortBy.isBlank() || "score".equalsIgnoreCase(sortBy)) {
            // 기본: Naver API의 정확도순 유지
            return products;
        }

        List<ProductDocument> sorted = new ArrayList<>(products);
        
        switch (sortBy.toLowerCase()) {
            case "price_asc":
                sorted.sort(Comparator.comparing(ProductDocument::getPrice, 
                        Comparator.nullsLast(Comparator.naturalOrder())));
                break;
            case "price_desc":
                sorted.sort(Comparator.comparing(ProductDocument::getPrice, 
                        Comparator.nullsFirst(Comparator.reverseOrder())));
                break;
            case "name":
                sorted.sort(Comparator.comparing(ProductDocument::getName));
                break;
            default:
                log.warn("Unknown sort type: {}, using default", sortBy);
        }
        
        return sorted;
    }

    /**
     * 페이징 설정 생성
     */
    private Pageable createPageable(ProductSearchRequest request) {
        int page = request.getPage() != null ? request.getPage() : 0;
        int size = request.getSize() != null ? request.getSize() : 20;
        
        return PageRequest.of(page, size);
    }
}
