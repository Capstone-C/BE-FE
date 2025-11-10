package com.capstone.web.shopping.repository;

import com.capstone.web.shopping.domain.Product;
import com.capstone.web.shopping.domain.ProductCategory;
import com.capstone.web.shopping.domain.ShoppingMallType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Product 엔티티 JPA Repository
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    /**
     * 쇼핑몰 타입과 외부 상품 ID로 상품 조회
     */
    Optional<Product> findByMallTypeAndExternalProductId(ShoppingMallType mallType, String externalProductId);

    /**
     * 카테고리별 상품 조회
     */
    List<Product> findByCategory(ProductCategory category);

    /**
     * 쇼핑몰 타입별 상품 조회
     */
    List<Product> findByMallType(ShoppingMallType mallType);

    /**
     * 카테고리와 쇼핑몰 타입으로 상품 조회
     */
    List<Product> findByCategoryAndMallType(ProductCategory category, ShoppingMallType mallType);
}
