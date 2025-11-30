package com.capstone.web.shopping.service;

import com.capstone.web.shopping.client.NaverShoppingApiClient;
import com.capstone.web.shopping.domain.ProductDocument;
import com.capstone.web.shopping.dto.ProductSearchRequest;
import com.capstone.web.shopping.repository.ProductSearchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 상품 검색 서비스
 * SHOP-01: 통합 쇼핑몰 상품 검색 기능
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ProductSearchService {

    private final ProductSearchRepository productSearchRepository;
    private final NaverShoppingApiClient naverShoppingApiClient;
    private final ProductIndexingService productIndexingService;

    /**
     * 상품 검색
     * 키워드, 가격 범위, 쇼핑몰 타입, 카테고리로 필터링 및 정렬
     */
    public Page<ProductDocument> searchProducts(ProductSearchRequest request) {
        log.info("Searching products with keyword: {}, mallType: {}, category: {}, priceRange: {}-{}",
                request.getKeyword(), request.getMallType(), request.getCategory(),
                request.getMinPrice(), request.getMaxPrice());

        Pageable pageable = createPageable(request);

        // 1. 키워드 검색
        if (request.getKeyword() != null && !request.getKeyword().isBlank()) {
            return searchByKeyword(request, pageable);
        }

        // 2. 카테고리 필터
        if (request.getCategory() != null && !request.getCategory().isBlank()) {
            return productSearchRepository.findByCategory(request.getCategory(), pageable);
        }

        // 3. 쇼핑몰 타입 필터
        if (request.getMallType() != null && !request.getMallType().isBlank()) {
            return productSearchRepository.findByMallType(request.getMallType(), pageable);
        }

        // 4. 전체 조회
        return productSearchRepository.findAll(pageable);
    }

    /**
     * 키워드 검색 (가격 범위, 쇼핑몰 타입, 카테고리 필터 적용)
     * 
     * 각 조합별로 적절한 Repository 메서드를 사용하여 정확한 검색 수행
     */
    private Page<ProductDocument> searchByKeyword(ProductSearchRequest request, Pageable pageable) {
        String keyword = request.getKeyword().trim();
        String category = request.getCategory();
        String mallType = request.getMallType();
        Integer minPrice = request.getMinPrice();
        Integer maxPrice = request.getMaxPrice();
        
        Page<ProductDocument> results;

        // 복합 조건 검색 - 키워드 + 카테고리 + 가격
        if (category != null && !category.isBlank() && (minPrice != null || maxPrice != null)) {
            int min = minPrice != null ? minPrice : 0;
            int max = maxPrice != null ? maxPrice : Integer.MAX_VALUE;
            log.debug("Search: keyword + category + price range");
            results = productSearchRepository.findByNameContainingAndCategoryAndPriceBetween(
                    keyword, category, min, max, pageable);
        }
        // 복합 조건 검색 - 키워드 + 카테고리 + 쇼핑몰
        else if (category != null && !category.isBlank() && mallType != null && !mallType.isBlank()) {
            log.debug("Search: keyword + category + mallType");
            results = productSearchRepository.findByNameContainingAndCategoryAndMallType(
                    keyword, category, mallType, pageable);
        }
        // 복합 조건 검색 - 키워드 + 가격
        else if (minPrice != null || maxPrice != null) {
            int min = minPrice != null ? minPrice : 0;
            int max = maxPrice != null ? maxPrice : Integer.MAX_VALUE;
            log.debug("Search: keyword + price range");
            results = productSearchRepository.findByNameContainingAndPriceBetween(
                    keyword, min, max, pageable);
        }
        // 복합 조건 검색 - 키워드 + 쇼핑몰
        else if (mallType != null && !mallType.isBlank()) {
            log.debug("Search: keyword + mallType");
            results = productSearchRepository.findByNameContainingAndMallType(keyword, mallType, pageable);
        }
        // 복합 조건 검색 - 키워드 + 카테고리
        else if (category != null && !category.isBlank()) {
            log.debug("Search: keyword + category");
            results = productSearchRepository.findByNameContainingAndCategory(keyword, category, pageable);
        }
        // 키워드만
        else {
            log.debug("Search: keyword only");
            results = productSearchRepository.findByNameContaining(keyword, pageable);
        }

        // 검색 결과가 없고 첫 페이지인 경우, 외부 API에서 실시간 검색 시도 (Fallback)
        if (results.isEmpty() && pageable.getPageNumber() == 0) {
            log.info("No products found in Elasticsearch. Trying on-demand fetch from mall APIs (keyword: {})", keyword);
            
            if (naverShoppingApiClient.isHealthy()) {
                List<ProductDocument> fetchedProducts = naverShoppingApiClient.searchProducts(keyword, 100);
                
                if (!fetchedProducts.isEmpty()) {
                    // Elasticsearch에 저장
                    productIndexingService.bulkIndexProducts(fetchedProducts);
                    
                    // 현재 페이지에 맞는 결과 반환
                    int start = (int) pageable.getOffset();
                    int end = Math.min((start + pageable.getPageSize()), fetchedProducts.size());
                    
                    if (start <= fetchedProducts.size()) {
                        List<ProductDocument> pagedList = fetchedProducts.subList(start, end);
                        return new org.springframework.data.domain.PageImpl<>(pagedList, pageable, fetchedProducts.size());
                    }
                }
            } else {
                log.warn("No healthy shopping mall clients available. Check API credentials.");
            }
        }

        log.info("Found {} products for keyword: '{}'", results.getTotalElements(), keyword);
        return results;
    }

    /**
     * 정렬 및 페이징 설정 생성
     */
    private Pageable createPageable(ProductSearchRequest request) {
        Sort sort = createSort(request.getSortBy());
        int page = request.getPage() != null ? request.getPage() : 0;
        int size = request.getSize() != null ? request.getSize() : 20;
        
        return PageRequest.of(page, size, sort);
    }

    /**
     * 정렬 기준 생성
     */
    private Sort createSort(String sortBy) {
        if (sortBy == null || sortBy.isBlank()) {
            return Sort.unsorted(); // 기본: score 정렬 (Elasticsearch _score)
        }

        return switch (sortBy.toLowerCase()) {
            case "relevance" -> Sort.unsorted(); // score 정렬
            case "price_asc" -> Sort.by(Sort.Direction.ASC, "price");
            case "price_desc" -> Sort.by(Sort.Direction.DESC, "price");
            case "latest" -> Sort.by(Sort.Direction.DESC, "createdAt");
            case "rating" -> Sort.by(Sort.Direction.DESC, "rating");
            default -> Sort.unsorted(); // 기본 score 정렬
        };
    }
}
