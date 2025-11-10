package com.capstone.web.shopping.client;

import com.capstone.web.shopping.domain.ProductDocument;

import java.util.List;

/**
 * 쇼핑몰 API 클라이언트 인터페이스
 * Strategy 패턴을 사용하여 각 쇼핑몰별 구현체를 교체 가능하게 설계
 */
public interface ShoppingMallApiClient {

    /**
     * 키워드로 상품 검색
     * 
     * @param keyword 검색 키워드
     * @param maxResults 최대 결과 수
     * @return 상품 목록
     */
    List<ProductDocument> searchProducts(String keyword, int maxResults);

    /**
     * 카테고리별 상품 조회
     * 
     * @param category 카테고리
     * @param maxResults 최대 결과 수
     * @return 상품 목록
     */
    List<ProductDocument> getProductsByCategory(String category, int maxResults);

    /**
     * 지원하는 쇼핑몰 타입 반환
     * 
     * @return 쇼핑몰 타입 (예: "NAVER", "COUPANG")
     */
    String getMallType();

    /**
     * API 연결 상태 확인
     * 
     * @return 정상 연결 여부
     */
    boolean isHealthy();
}
