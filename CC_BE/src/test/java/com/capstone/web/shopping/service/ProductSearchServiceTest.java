package com.capstone.web.shopping.service;

import com.capstone.web.shopping.domain.ProductDocument;
import com.capstone.web.shopping.dto.ProductSearchRequest;
import com.capstone.web.shopping.repository.ProductSearchRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * ProductSearchService 테스트
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ProductSearchService 테스트")
class ProductSearchServiceTest {

    @InjectMocks
    private ProductSearchService productSearchService;

    @Mock
    private ProductSearchRepository productSearchRepository;

    @Test
    @DisplayName("키워드로 상품 검색 성공")
    void searchProducts_Success_WithKeyword() {
        // given
        String keyword = "유기농 계란";
        ProductSearchRequest request = ProductSearchRequest.builder()
                .keyword(keyword)
                .page(0)
                .size(20)
                .sortBy("relevance")
                .build();

        List<ProductDocument> mockProducts = List.of(
                createMockProduct("1", "유기농 계란 30구", 10000),
                createMockProduct("2", "유기농 계란 10구", 5000)
        );
        Page<ProductDocument> mockPage = new PageImpl<>(mockProducts);

        when(productSearchRepository.findByNameContaining(eq(keyword), any(Pageable.class)))
                .thenReturn(mockPage);

        // when
        Page<ProductDocument> result = productSearchService.searchProducts(request);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent().get(0).getName()).contains("유기농 계란");
        verify(productSearchRepository).findByNameContaining(eq(keyword), any(Pageable.class));
    }

    @Test
    @DisplayName("카테고리로 필터링 성공")
    void searchProducts_Success_WithCategory() {
        // given
        ProductSearchRequest request = ProductSearchRequest.builder()
                .category("DAIRY")
                .page(0)
                .size(20)
                .build();

        List<ProductDocument> mockProducts = List.of(
                createMockProduct("1", "우유", 3000),
                createMockProduct("2", "치즈", 5000)
        );
        Page<ProductDocument> mockPage = new PageImpl<>(mockProducts);

        when(productSearchRepository.findByCategory(eq("DAIRY"), any(Pageable.class)))
                .thenReturn(mockPage);

        // when
        Page<ProductDocument> result = productSearchService.searchProducts(request);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(2);
        verify(productSearchRepository).findByCategory(eq("DAIRY"), any(Pageable.class));
    }

    @Test
    @DisplayName("쇼핑몰 타입으로 필터링 성공")
    void searchProducts_Success_WithMallType() {
        // given
        ProductSearchRequest request = ProductSearchRequest.builder()
                .mallType("NAVER")
                .page(0)
                .size(20)
                .build();

        List<ProductDocument> mockProducts = List.of(
                createMockProduct("1", "상품A", 10000),
                createMockProduct("2", "상품B", 20000)
        );
        Page<ProductDocument> mockPage = new PageImpl<>(mockProducts);

        when(productSearchRepository.findByMallType(eq("NAVER"), any(Pageable.class)))
                .thenReturn(mockPage);

        // when
        Page<ProductDocument> result = productSearchService.searchProducts(request);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(2);
        verify(productSearchRepository).findByMallType(eq("NAVER"), any(Pageable.class));
    }

    @Test
    @DisplayName("전체 상품 조회 성공 (필터 없음)")
    void searchProducts_Success_NoFilter() {
        // given
        ProductSearchRequest request = ProductSearchRequest.builder()
                .page(0)
                .size(20)
                .build();

        List<ProductDocument> mockProducts = List.of(
                createMockProduct("1", "상품1", 1000),
                createMockProduct("2", "상품2", 2000),
                createMockProduct("3", "상품3", 3000)
        );
        Page<ProductDocument> mockPage = new PageImpl<>(mockProducts);

        when(productSearchRepository.findAll(any(Pageable.class)))
                .thenReturn(mockPage);

        // when
        Page<ProductDocument> result = productSearchService.searchProducts(request);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(3);
        verify(productSearchRepository).findAll(any(Pageable.class));
    }

    @Test
    @DisplayName("가격순 정렬 성공")
    void searchProducts_Success_SortByPrice() {
        // given
        ProductSearchRequest request = ProductSearchRequest.builder()
                .keyword("계란")
                .sortBy("price_asc")
                .page(0)
                .size(20)
                .build();

        List<ProductDocument> mockProducts = List.of(
                createMockProduct("1", "계란 10구", 3000),
                createMockProduct("2", "계란 30구", 9000)
        );
        Page<ProductDocument> mockPage = new PageImpl<>(mockProducts);

        when(productSearchRepository.findByNameContaining(eq("계란"), any(Pageable.class)))
                .thenReturn(mockPage);

        // when
        Page<ProductDocument> result = productSearchService.searchProducts(request);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(2);
        verify(productSearchRepository).findByNameContaining(eq("계란"), any(Pageable.class));
    }

    @Test
    @DisplayName("빈 결과 반환 성공")
    void searchProducts_Success_EmptyResult() {
        // given
        ProductSearchRequest request = ProductSearchRequest.builder()
                .keyword("존재하지않는상품")
                .page(0)
                .size(20)
                .build();

        Page<ProductDocument> emptyPage = Page.empty();

        when(productSearchRepository.findByNameContaining(eq("존재하지않는상품"), any(Pageable.class)))
                .thenReturn(emptyPage);

        // when
        Page<ProductDocument> result = productSearchService.searchProducts(request);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isZero();
    }

    private ProductDocument createMockProduct(String id, String name, int price) {
        return ProductDocument.builder()
                .id(id)
                .name(name)
                .price(price)
                .originalPrice(price + 1000)
                .discountRate(10)
                .imageUrl("https://example.com/image.jpg")
                .productUrl("https://example.com/product")
                .mallType("NAVER")
                .category("DAIRY")
                .description("상품 설명")
                .deliveryInfo("무료배송")
                .rating(4.5)
                .reviewCount(100)
                .createdAt(LocalDateTime.now())
                .build();
    }
}
