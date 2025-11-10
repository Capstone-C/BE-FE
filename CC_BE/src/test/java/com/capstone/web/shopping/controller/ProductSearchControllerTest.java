package com.capstone.web.shopping.controller;

import com.capstone.web.shopping.domain.ProductDocument;
import com.capstone.web.shopping.dto.ProductSearchRequest;
import com.capstone.web.shopping.service.ProductSearchService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * ProductSearchController 테스트
 * 
 * NOTE: Elasticsearch 관련 빈들은 TestElasticsearchConfig에서 자동으로 Mock으로 제공됩니다.
 * ProductSearchService는 @MockitoBean으로 제공합니다.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("ProductSearchController 테스트")
class ProductSearchControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductSearchService productSearchService;

    @Test
    @DisplayName("GET /api/v1/shopping/search - 키워드 검색 성공")
    void searchProducts_ApiSuccess_WithKeyword() throws Exception {
        // given
        List<ProductDocument> mockProducts = List.of(
                createMockProduct("1", "유기농 계란 30구", 10000),
                createMockProduct("2", "유기농 계란 10구", 5000)
        );
        Page<ProductDocument> mockPage = new PageImpl<>(mockProducts);

        when(productSearchService.searchProducts(any(ProductSearchRequest.class)))
                .thenReturn(mockPage);

        // when & then
        mockMvc.perform(get("/api/v1/shopping/search")
                        .param("keyword", "유기농 계란")
                        .param("page", "0")
                        .param("size", "20"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.products").isArray())
                .andExpect(jsonPath("$.products.length()").value(2))
                .andExpect(jsonPath("$.products[0].name").value("유기농 계란 30구"))
                .andExpect(jsonPath("$.products[0].price").value(10000))
                .andExpect(jsonPath("$.totalCount").value(2))
                .andExpect(jsonPath("$.currentPage").value(0))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.pageSize").value(2));

        verify(productSearchService).searchProducts(any(ProductSearchRequest.class));
    }

    @Test
    @DisplayName("GET /api/v1/shopping/search - 카테고리 필터링 성공")
    void searchProducts_ApiSuccess_WithCategory() throws Exception {
        // given
        List<ProductDocument> mockProducts = List.of(
                createMockProduct("1", "우유", 3000),
                createMockProduct("2", "치즈", 5000)
        );
        Page<ProductDocument> mockPage = new PageImpl<>(mockProducts);

        when(productSearchService.searchProducts(any(ProductSearchRequest.class)))
                .thenReturn(mockPage);

        // when & then
        mockMvc.perform(get("/api/v1/shopping/search")
                        .param("category", "DAIRY")
                        .param("page", "0")
                        .param("size", "20"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.products").isArray())
                .andExpect(jsonPath("$.products.length()").value(2))
                .andExpect(jsonPath("$.totalCount").value(2));

        verify(productSearchService).searchProducts(any(ProductSearchRequest.class));
    }

    @Test
    @DisplayName("GET /api/v1/shopping/search - 가격 범위 필터링 성공")
    void searchProducts_ApiSuccess_WithPriceRange() throws Exception {
        // given
        List<ProductDocument> mockProducts = List.of(
                createMockProduct("1", "상품A", 5000),
                createMockProduct("2", "상품B", 8000)
        );
        Page<ProductDocument> mockPage = new PageImpl<>(mockProducts);

        when(productSearchService.searchProducts(any(ProductSearchRequest.class)))
                .thenReturn(mockPage);

        // when & then
        mockMvc.perform(get("/api/v1/shopping/search")
                        .param("minPrice", "3000")
                        .param("maxPrice", "10000")
                        .param("page", "0")
                        .param("size", "20"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.products").isArray())
                .andExpect(jsonPath("$.products.length()").value(2));

        verify(productSearchService).searchProducts(any(ProductSearchRequest.class));
    }

    @Test
    @DisplayName("GET /api/v1/shopping/search - 정렬 성공 (가격 낮은순)")
    void searchProducts_ApiSuccess_SortByPriceAsc() throws Exception {
        // given
        List<ProductDocument> mockProducts = List.of(
                createMockProduct("1", "저렴한 상품", 1000),
                createMockProduct("2", "비싼 상품", 10000)
        );
        Page<ProductDocument> mockPage = new PageImpl<>(mockProducts);

        when(productSearchService.searchProducts(any(ProductSearchRequest.class)))
                .thenReturn(mockPage);

        // when & then
        mockMvc.perform(get("/api/v1/shopping/search")
                        .param("keyword", "상품")
                        .param("sortBy", "price_asc")
                        .param("page", "0")
                        .param("size", "20"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.products").isArray())
                .andExpect(jsonPath("$.products.length()").value(2))
                .andExpect(jsonPath("$.products[0].price").value(1000))
                .andExpect(jsonPath("$.products[1].price").value(10000));

        verify(productSearchService).searchProducts(any(ProductSearchRequest.class));
    }

    @Test
    @DisplayName("GET /api/v1/shopping/search - 빈 결과 성공")
    void searchProducts_ApiSuccess_EmptyResult() throws Exception {
        // given
        Page<ProductDocument> emptyPage = Page.empty();

        when(productSearchService.searchProducts(any(ProductSearchRequest.class)))
                .thenReturn(emptyPage);

        // when & then
        mockMvc.perform(get("/api/v1/shopping/search")
                        .param("keyword", "존재하지않는상품")
                        .param("page", "0")
                        .param("size", "20"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.products").isArray())
                .andExpect(jsonPath("$.products.length()").value(0))
                .andExpect(jsonPath("$.totalCount").value(0));

        verify(productSearchService).searchProducts(any(ProductSearchRequest.class));
    }

    @Test
    @DisplayName("GET /api/v1/shopping/search - 복합 필터 성공 (키워드 + 카테고리 + 가격범위)")
    void searchProducts_ApiSuccess_ComplexFilter() throws Exception {
        // given
        List<ProductDocument> mockProducts = List.of(
                createMockProduct("1", "유기농 우유", 3500)
        );
        Page<ProductDocument> mockPage = new PageImpl<>(mockProducts);

        when(productSearchService.searchProducts(any(ProductSearchRequest.class)))
                .thenReturn(mockPage);

        // when & then
        mockMvc.perform(get("/api/v1/shopping/search")
                        .param("keyword", "우유")
                        .param("category", "DAIRY")
                        .param("minPrice", "3000")
                        .param("maxPrice", "5000")
                        .param("sortBy", "price_asc")
                        .param("page", "0")
                        .param("size", "20"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.products").isArray())
                .andExpect(jsonPath("$.products.length()").value(1))
                .andExpect(jsonPath("$.products[0].name").value("유기농 우유"));

        verify(productSearchService).searchProducts(any(ProductSearchRequest.class));
    }

    @Test
    @DisplayName("GET /api/v1/shopping/search - 파라미터 없이 전체 조회 성공")
    void searchProducts_ApiSuccess_NoParameter() throws Exception {
        // given
        List<ProductDocument> mockProducts = List.of(
                createMockProduct("1", "상품1", 1000),
                createMockProduct("2", "상품2", 2000),
                createMockProduct("3", "상품3", 3000)
        );
        Page<ProductDocument> mockPage = new PageImpl<>(mockProducts);

        when(productSearchService.searchProducts(any(ProductSearchRequest.class)))
                .thenReturn(mockPage);

        // when & then
        mockMvc.perform(get("/api/v1/shopping/search"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.products").isArray())
                .andExpect(jsonPath("$.products.length()").value(3))
                .andExpect(jsonPath("$.totalCount").value(3));

        verify(productSearchService).searchProducts(any(ProductSearchRequest.class));
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
