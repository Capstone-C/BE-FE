package com.capstone.web.shopping.repository;

import com.capstone.web.shopping.domain.ProductDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * ProductDocument Elasticsearch Repository
 * 쇼핑몰 상품 데이터는 Elasticsearch에만 저장되며 RDB를 사용하지 않음
 */
@Repository
public interface ProductSearchRepository extends ElasticsearchRepository<ProductDocument, String> {

    /**
     * 외부 상품 ID로 검색 (중복 방지용)
     */
    Optional<ProductDocument> findByExternalProductId(String externalProductId);

    /**
     * 쇼핑몰 타입과 외부 상품 ID로 검색
     */
    Optional<ProductDocument> findByMallTypeAndExternalProductId(String mallType, String externalProductId);

    /**
     * 카테고리별 검색 (페이징)
     */
    Page<ProductDocument> findByCategory(String category, Pageable pageable);

    /**
     * 쇼핑몰 타입별 검색 (페이징)
     */
    Page<ProductDocument> findByMallType(String mallType, Pageable pageable);

    /**
     * 카테고리와 쇼핑몰 타입으로 검색 (페이징)
     */
    Page<ProductDocument> findByCategoryAndMallType(String category, String mallType, Pageable pageable);

    /**
     * 상품명으로 검색 (Full-text search with Nori analyzer)
     * Nori 형태소 분석기가 자연스럽게 처리:
     * - "고기" → 고기 포함 제품
     * - "만두" → 만두 포함 제품
     * - "김치만두" → 김치 관련 제품 (Nori가 "김치" 토큰 추출)
     * - "김치 만두" → 김치와 만두 모두 포함된 제품
     */
    @Query("{\"match\": {\"name\": \"?0\"}}")
    Page<ProductDocument> findByNameContaining(String name, Pageable pageable);

    /**
     * 가격 범위로 검색
     */
    Page<ProductDocument> findByPriceBetween(Integer minPrice, Integer maxPrice, Pageable pageable);

    /**
     * 쇼핑몰 타입별로 삭제 (벌크 삭제용)
     */
    void deleteByMallType(String mallType);

    /**
     * 복합 검색: 키워드 + 카테고리
     */
    @Query("{\"bool\": {\"must\": [{\"term\": {\"category\": \"?1\"}}, {\"match\": {\"name\": \"?0\"}}]}}")
    Page<ProductDocument> findByNameContainingAndCategory(String name, String category, Pageable pageable);

    /**
     * 복합 검색: 키워드 + 쇼핑몰 타입
     */
    @Query("{\"bool\": {\"must\": [{\"term\": {\"mallType\": \"?1\"}}, {\"match\": {\"name\": \"?0\"}}]}}")
    Page<ProductDocument> findByNameContainingAndMallType(String name, String mallType, Pageable pageable);

    /**
     * 복합 검색: 키워드 + 카테고리 + 쇼핑몰 타입
     */
    @Query("{\"bool\": {\"must\": [{\"term\": {\"category\": \"?1\"}}, {\"term\": {\"mallType\": \"?2\"}}, {\"match\": {\"name\": \"?0\"}}]}}")
    Page<ProductDocument> findByNameContainingAndCategoryAndMallType(
            String name, String category, String mallType, Pageable pageable);

    /**
     * 복합 검색: 키워드 + 가격 범위
     */
    @Query("{\"bool\": {\"must\": [{\"range\": {\"price\": {\"gte\": ?1, \"lte\": ?2}}}, {\"match\": {\"name\": \"?0\"}}]}}")
    Page<ProductDocument> findByNameContainingAndPriceBetween(
            String name, Integer minPrice, Integer maxPrice, Pageable pageable);

    /**
     * 복합 검색: 카테고리 + 가격 범위
     */
    Page<ProductDocument> findByCategoryAndPriceBetween(
            String category, Integer minPrice, Integer maxPrice, Pageable pageable);

    /**
     * 복합 검색: 키워드 + 카테고리 + 가격 범위
     */
    @Query("{\"bool\": {\"must\": [{\"term\": {\"category\": \"?1\"}}, {\"range\": {\"price\": {\"gte\": ?2, \"lte\": ?3}}}, {\"match\": {\"name\": \"?0\"}}]}}")
    Page<ProductDocument> findByNameContainingAndCategoryAndPriceBetween(
            String name, String category, Integer minPrice, Integer maxPrice, Pageable pageable);
}
