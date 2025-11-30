package com.capstone.web.shopping.service;

import com.capstone.web.shopping.client.NaverShoppingApiClient;
import com.capstone.web.shopping.domain.ProductDocument;
import com.capstone.web.shopping.dto.ProductSearchRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * ProductSearchService 테스트
 * Naver API 직접 호출 방식으로 변경됨
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ProductSearchService 테스트")
class ProductSearchServiceTest {

    @InjectMocks
    private ProductSearchService productSearchService;

    @Mock
    private NaverShoppingApiClient naverShoppingApiClient;

    @Test
    @DisplayName("키워드로 상품 검색 성공")
    void searchProducts_Success_WithKeyword() {
        // given
        String keyword = "김치";
        ProductSearchRequest request = ProductSearchRequest.builder()
                .keyword(keyword)
                .page(0)
                .size(20)
                .sortBy("relevance")
                .build();

        List<ProductDocument> mockProducts = List.of(
                createMockProduct("1", "국내산 배추김치 2kg", 17900, "PROCESSED"),
                createMockProduct("2", "전라도식 포기김치 5kg", 29900, "PROCESSED")
        );

        when(naverShoppingApiClient.searchProducts(eq(keyword), anyInt()))
                .thenReturn(mockProducts);

        // when
        Page<ProductDocument> result = productSearchService.searchProducts(request);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent().get(0).getName()).contains("김치");
        verify(naverShoppingApiClient).searchProducts(eq(keyword), anyInt());
    }

    @Test
    @DisplayName("카테고리로 필터링 성공")
    void searchProducts_Success_WithCategory() {
        // given
        String keyword = "간식";
        ProductSearchRequest request = ProductSearchRequest.builder()
                .keyword(keyword)
                .category("SNACKS")
                .page(0)
                .size(20)
                .build();

        List<ProductDocument> mockProducts = List.of(
                createMockProduct("1", "과자", 3000, "SNACKS"),
                createMockProduct("2", "우유", 2500, "DAIRY"),
                createMockProduct("3", "초콜릿", 5000, "SNACKS")
        );

        when(naverShoppingApiClient.searchProducts(eq(keyword), anyInt()))
                .thenReturn(mockProducts);

        // when
        Page<ProductDocument> result = productSearchService.searchProducts(request);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(2); // SNACKS만 필터링됨
        assertThat(result.getContent()).allMatch(p -> "SNACKS".equals(p.getCategory()));
        verify(naverShoppingApiClient).searchProducts(eq(keyword), anyInt());
    }

    @Test
    @DisplayName("가격 범위로 필터링 성공")
    void searchProducts_Success_WithPriceRange() {
        // given
        String keyword = "계란";
        ProductSearchRequest request = ProductSearchRequest.builder()
                .keyword(keyword)
                .minPrice(5000)
                .maxPrice(10000)
                .page(0)
                .size(20)
                .build();

        List<ProductDocument> mockProducts = List.of(
                createMockProduct("1", "계란 10구", 3000, "DAIRY"),
                createMockProduct("2", "계란 30구", 9000, "DAIRY"),
                createMockProduct("3", "계란 50구", 15000, "DAIRY")
        );

        when(naverShoppingApiClient.searchProducts(eq(keyword), anyInt()))
                .thenReturn(mockProducts);

        // when
        Page<ProductDocument> result = productSearchService.searchProducts(request);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1); // 9000원 상품만
        assertThat(result.getContent().get(0).getPrice()).isBetween(5000, 10000);
        verify(naverShoppingApiClient).searchProducts(eq(keyword), anyInt());
    }

    @Test
    @DisplayName("가격순 정렬 성공")
    void searchProducts_Success_SortByPrice() {
        // given
        String keyword = "계란";
        ProductSearchRequest request = ProductSearchRequest.builder()
                .keyword(keyword)
                .sortBy("price_asc")
                .page(0)
                .size(20)
                .build();

        List<ProductDocument> mockProducts = List.of(
                createMockProduct("1", "계란 30구", 9000, "DAIRY"),
                createMockProduct("2", "계란 10구", 3000, "DAIRY"),
                createMockProduct("3", "계란 20구", 6000, "DAIRY")
        );

        when(naverShoppingApiClient.searchProducts(eq(keyword), anyInt()))
                .thenReturn(mockProducts);

        // when
        Page<ProductDocument> result = productSearchService.searchProducts(request);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(3);
        // 가격 오름차순 정렬 확인
        assertThat(result.getContent().get(0).getPrice()).isLessThanOrEqualTo(result.getContent().get(1).getPrice());
        assertThat(result.getContent().get(1).getPrice()).isLessThanOrEqualTo(result.getContent().get(2).getPrice());
        verify(naverShoppingApiClient).searchProducts(eq(keyword), anyInt());
    }

    @Test
    @DisplayName("빈 결과 반환 성공")
    void searchProducts_Success_EmptyResult() {
        // given
        String keyword = "존재하지않는상품";
        ProductSearchRequest request = ProductSearchRequest.builder()
                .keyword(keyword)
                .page(0)
                .size(20)
                .build();

        when(naverShoppingApiClient.searchProducts(eq(keyword), anyInt()))
                .thenReturn(List.of());

        // when
        Page<ProductDocument> result = productSearchService.searchProducts(request);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isZero();
    }

    @Test
    @DisplayName("페이지네이션 성공")
    void searchProducts_Success_Pagination() {
        // given
        String keyword = "김치";
        ProductSearchRequest request = ProductSearchRequest.builder()
                .keyword(keyword)
                .page(1) // 두 번째 페이지
                .size(2)
                .build();

        List<ProductDocument> mockProducts = List.of(
                createMockProduct("1", "김치 1kg", 10000, "PROCESSED"),
                createMockProduct("2", "김치 2kg", 18000, "PROCESSED"),
                createMockProduct("3", "김치 3kg", 25000, "PROCESSED"),
                createMockProduct("4", "김치 5kg", 40000, "PROCESSED")
        );

        when(naverShoppingApiClient.searchProducts(eq(keyword), anyInt()))
                .thenReturn(mockProducts);

        // when
        Page<ProductDocument> result = productSearchService.searchProducts(request);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getNumber()).isEqualTo(1); // 페이지 번호
        assertThat(result.getTotalElements()).isEqualTo(4);
        assertThat(result.getTotalPages()).isEqualTo(2);
    }

    private ProductDocument createMockProduct(String id, String name, int price, String category) {
        return ProductDocument.builder()
                .id(id)
                .name(name)
                .price(price)
                .originalPrice(price + 1000)
                .discountRate(10)
                .imageUrl("https://example.com/image.jpg")
                .productUrl("https://example.com/product")
                .mallType("NAVER")
                .category(category)
                .description("상품 설명")
                .deliveryInfo("무료배송")
                .rating(4.5)
                .reviewCount(100)
                .createdAt(LocalDateTime.now())
                .build();
    }
}
