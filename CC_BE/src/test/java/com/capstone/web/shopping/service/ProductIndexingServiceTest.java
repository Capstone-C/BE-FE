package com.capstone.web.shopping.service;

import com.capstone.web.shopping.domain.ProductDocument;
import com.capstone.web.shopping.domain.ShoppingMallType;
import com.capstone.web.shopping.repository.ProductSearchRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

/**
 * ProductIndexingService 테스트
 * 
 * Elasticsearch 인덱싱 로직을 Mock으로 테스트
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("상품 인덱싱 서비스 테스트")
class ProductIndexingServiceTest {

    @Mock
    private ProductSearchRepository productSearchRepository;

    @Mock
    private ElasticsearchOperations elasticsearchOperations;

    @InjectMocks
    private ProductIndexingService productIndexingService;

    @Test
    @DisplayName("단건 상품 인덱싱 - 성공")
    void indexProduct_Success() {
        // given
        ProductDocument product = createTestProduct("NAVER_12345", "테스트 상품");
        
        given(productSearchRepository.save(any(ProductDocument.class))).willReturn(product);

        // when
        ProductDocument result = productIndexingService.indexProduct(product);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("테스트 상품");
        verify(productSearchRepository, times(1)).save(product);
    }

    @Test
    @DisplayName("벌크 상품 인덱싱 - 성공")
    void bulkIndexProducts_Success() {
        // given
        List<ProductDocument> products = Arrays.asList(
                createTestProduct("NAVER_11111", "상품1"),
                createTestProduct("NAVER_22222", "상품2"),
                createTestProduct("NAVER_33333", "상품3")
        );

        // when
        productIndexingService.bulkIndexProducts(products);

        // then
        verify(elasticsearchOperations, times(1)).bulkIndex(
                anyList(),
                eq(ProductDocument.class)
        );
    }

    @Test
    @DisplayName("벌크 인덱싱 - 빈 리스트")
    void bulkIndexProducts_EmptyList() {
        // given
        List<ProductDocument> emptyList = Arrays.asList();

        // when
        productIndexingService.bulkIndexProducts(emptyList);

        // then
        verify(elasticsearchOperations, never()).bulkIndex(anyList(), eq(ProductDocument.class));
    }

    @Test
    @DisplayName("Upsert - 새 상품 생성")
    void upsertProduct_CreateNew() {
        // given
        ProductDocument newProduct = createTestProduct("NAVER_99999", "새 상품");
        
        given(productSearchRepository.findById("NAVER_99999")).willReturn(Optional.empty());
        given(productSearchRepository.save(any(ProductDocument.class))).willReturn(newProduct);

        // when
        ProductDocument result = productIndexingService.upsertProduct(newProduct);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("새 상품");
        verify(productSearchRepository, times(1)).findById("NAVER_99999");
        verify(productSearchRepository, times(1)).save(newProduct);
    }

    @Test
    @DisplayName("Upsert - 기존 상품 업데이트")
    void upsertProduct_UpdateExisting() {
        // given
        ProductDocument existing = createTestProduct("NAVER_12345", "기존 상품");
        ProductDocument updated = createTestProduct("NAVER_12345", "업데이트된 상품", 20000);
        
        given(productSearchRepository.findById("NAVER_12345")).willReturn(Optional.of(existing));
        given(productSearchRepository.save(any(ProductDocument.class))).willReturn(updated);

        // when
        ProductDocument result = productIndexingService.upsertProduct(updated);

        // then
        assertThat(result).isNotNull();
        verify(productSearchRepository, times(1)).findById("NAVER_12345");
        verify(productSearchRepository, times(1)).save(any(ProductDocument.class));
    }

    private ProductDocument createTestProduct(String id, String name) {
        return createTestProduct(id, name, 10000);
    }

    private ProductDocument createTestProduct(String id, String name, int price) {
        return ProductDocument.builder()
                .id(id)
                .name(name)
                .price(price)
                .originalPrice(15000)
                .discountRate(33)
                .imageUrl("https://example.com/image.jpg")
                .productUrl("https://example.com/product")
                .mallType(ShoppingMallType.NAVER.name())
                .category("BEVERAGES")
                .description("테스트 상품 설명")
                .deliveryInfo("무료배송")
                .rating(4.5)
                .reviewCount(100)
                .externalProductId(id.split("_")[1])
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }
}
