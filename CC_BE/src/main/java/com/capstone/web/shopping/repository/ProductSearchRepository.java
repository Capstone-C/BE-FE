package com.capstone.web.shopping.repository;

import com.capstone.web.shopping.domain.ProductDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * ProductDocument Elasticsearch Repository
 */
@Repository
public interface ProductSearchRepository extends ElasticsearchRepository<ProductDocument, String> {

    /**
     * 상품 ID로 검색
     */
    List<ProductDocument> findByProductId(Long productId);

    /**
     * 카테고리별 검색
     */
    List<ProductDocument> findByCategory(String category);

    /**
     * 쇼핑몰 타입별 검색
     */
    List<ProductDocument> findByMallType(String mallType);

    /**
     * 카테고리와 쇼핑몰 타입으로 검색
     */
    List<ProductDocument> findByCategoryAndMallType(String category, String mallType);
}
