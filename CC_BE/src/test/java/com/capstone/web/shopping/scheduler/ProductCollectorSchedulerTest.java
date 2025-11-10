package com.capstone.web.shopping.scheduler;

import com.capstone.web.shopping.client.NaverShoppingApiClient;
import com.capstone.web.shopping.domain.ProductCategory;
import com.capstone.web.shopping.domain.ProductDocument;
import com.capstone.web.shopping.service.ProductIndexingService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * ProductCollectorScheduler 테스트
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ProductCollectorScheduler 테스트")
class ProductCollectorSchedulerTest {

    @InjectMocks
    private ProductCollectorScheduler productCollectorScheduler;

    @Mock
    private NaverShoppingApiClient naverShoppingApiClient;

    @Mock
    private ProductIndexingService productIndexingService;

    @Test
    @DisplayName("상품 수집 스케줄러 실행 성공")
    void collectProducts_Success() throws InterruptedException {
        // given
        List<ProductDocument> mockProducts = List.of(
                createMockProduct("1", "채소1", 1000),
                createMockProduct("2", "채소2", 2000)
        );

        when(naverShoppingApiClient.searchProducts(anyString(), anyInt()))
                .thenReturn(mockProducts);

        doNothing().when(productIndexingService).bulkIndexProducts(anyList());

        // when
        productCollectorScheduler.collectProducts();

        // then
        // 모든 카테고리(11개)에 대해 API 호출 검증
        verify(naverShoppingApiClient, times(ProductCategory.values().length))
                .searchProducts(anyString(), eq(100));

        // bulkIndexProducts가 한 번 호출되었는지 검증
        verify(productIndexingService, times(1))
                .bulkIndexProducts(anyList());
    }

    @Test
    @DisplayName("특정 카테고리 실패 시 다른 카테고리는 계속 수집")
    void collectProducts_Success_PartialFailure() throws InterruptedException {
        // given
        List<ProductDocument> mockProducts = List.of(
                createMockProduct("1", "상품", 1000)
        );

        // 첫 번째 호출은 실패, 나머지는 성공
        when(naverShoppingApiClient.searchProducts(anyString(), anyInt()))
                .thenThrow(new RuntimeException("API 호출 실패"))
                .thenReturn(mockProducts);

        doNothing().when(productIndexingService).bulkIndexProducts(anyList());

        // when
        productCollectorScheduler.collectProducts();

        // then
        // 모든 카테고리에 대해 API 호출 시도 검증
        verify(naverShoppingApiClient, times(ProductCategory.values().length))
                .searchProducts(anyString(), eq(100));

        // 실패한 카테고리를 제외한 나머지는 인덱싱됨
        verify(productIndexingService, times(1))
                .bulkIndexProducts(anyList());
    }

    @Test
    @DisplayName("수집된 상품이 없을 때 인덱싱 호출 안됨")
    void collectProducts_NoProducts_SkipIndexing() throws InterruptedException {
        // given
        when(naverShoppingApiClient.searchProducts(anyString(), anyInt()))
                .thenReturn(new ArrayList<>());

        // when
        productCollectorScheduler.collectProducts();

        // then
        verify(naverShoppingApiClient, times(ProductCategory.values().length))
                .searchProducts(anyString(), eq(100));

        // 인덱싱은 호출되지 않음
        verify(productIndexingService, never())
                .bulkIndexProducts(anyList());
    }

    @Test
    @DisplayName("수동 상품 수집 실행 성공")
    void collectProductsManually_Success() throws InterruptedException {
        // given
        List<ProductDocument> mockProducts = List.of(
                createMockProduct("1", "상품", 1000)
        );

        when(naverShoppingApiClient.searchProducts(anyString(), anyInt()))
                .thenReturn(mockProducts);

        doNothing().when(productIndexingService).bulkIndexProducts(anyList());

        // when
        productCollectorScheduler.collectProductsManually();

        // then
        verify(naverShoppingApiClient, times(ProductCategory.values().length))
                .searchProducts(anyString(), eq(100));

        verify(productIndexingService, times(1))
                .bulkIndexProducts(anyList());
    }

    @Test
    @DisplayName("전체 카테고리 수집 실패 시 에러 로깅")
    void collectProducts_AllFailed_LogError() throws InterruptedException {
        // given
        when(naverShoppingApiClient.searchProducts(anyString(), anyInt()))
                .thenThrow(new RuntimeException("API 호출 실패"));

        // when
        productCollectorScheduler.collectProducts();

        // then
        verify(naverShoppingApiClient, times(ProductCategory.values().length))
                .searchProducts(anyString(), eq(100));

        // 인덱싱은 호출되지 않음 (수집된 상품이 없음)
        verify(productIndexingService, never())
                .bulkIndexProducts(anyList());
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
