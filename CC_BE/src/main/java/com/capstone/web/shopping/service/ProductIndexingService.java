package com.capstone.web.shopping.service;

import com.capstone.web.shopping.domain.ProductDocument;
import com.capstone.web.shopping.repository.ProductSearchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.query.IndexQuery;
import org.springframework.data.elasticsearch.core.query.IndexQueryBuilder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Elasticsearch 인덱싱 서비스
 * 수집된 상품 데이터를 Elasticsearch에 Bulk Insert
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ProductIndexingService {

    private final ProductSearchRepository productSearchRepository;
    private final ElasticsearchOperations elasticsearchOperations;

    /**
     * 단건 상품 인덱싱
     */
    public ProductDocument indexProduct(ProductDocument product) {
        log.info("Indexing product: {} from {}", product.getName(), product.getMallType());
        return productSearchRepository.save(product);
    }

    /**
     * 벌크 상품 인덱싱
     * 대량의 상품 데이터를 한 번에 저장
     */
    public void bulkIndexProducts(List<ProductDocument> products) {
        if (products == null || products.isEmpty()) {
            log.warn("No products to index");
            return;
        }

        log.info("Bulk indexing {} products", products.size());

        List<IndexQuery> queries = products.stream()
                .map(product -> new IndexQueryBuilder()
                        .withId(product.getId())
                        .withObject(product)
                        .build())
                .collect(Collectors.toList());

        elasticsearchOperations.bulkIndex(queries, ProductDocument.class);
        log.info("Successfully indexed {} products", products.size());
    }

    /**
     * 상품 업데이트 또는 삽입 (Upsert)
     * 기존 문서가 있으면 업데이트, 없으면 새로 생성
     */
    public ProductDocument upsertProduct(ProductDocument product) {
        String id = product.getId();
        
        return productSearchRepository.findById(id)
                .map(existing -> {
                    log.info("Updating existing product: {}", id);
                    ProductDocument updated = existing.updateInfo(
                            product.getName(),
                            product.getPrice(),
                            product.getOriginalPrice(),
                            product.getImageUrl(),
                            product.getDescription(),
                            product.getDeliveryInfo(),
                            product.getRating(),
                            product.getReviewCount()
                    );
                    return productSearchRepository.save(updated);
                })
                .orElseGet(() -> {
                    log.info("Creating new product: {}", id);
                    return productSearchRepository.save(product);
                });
    }

    /**
     * 특정 쇼핑몰의 모든 상품 삭제
     */
    public void deleteProductsByMallType(String mallType) {
        log.info("Deleting all products from mall: {}", mallType);
        productSearchRepository.findByMallType(mallType, org.springframework.data.domain.Pageable.unpaged())
                .forEach(product -> productSearchRepository.deleteById(product.getId()));
    }

    /**
     * 전체 인덱스 재구축
     */
    public void reindexAll(List<ProductDocument> allProducts) {
        log.info("Starting full reindex with {} products", allProducts.size());
        productSearchRepository.deleteAll();
        bulkIndexProducts(allProducts);
        log.info("Reindex completed");
    }
}
