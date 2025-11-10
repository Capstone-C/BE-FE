package com.capstone.web.shopping.client;

import com.capstone.web.shopping.domain.ProductDocument;
import com.capstone.web.shopping.dto.NaverShoppingResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;

/**
 * 네이버 쇼핑 API 클라이언트 테스트
 * 
 * 실제 API 호출 없이 Mock을 사용하여 테스트
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("네이버 쇼핑 API 클라이언트 테스트")
class NaverShoppingApiClientTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private NaverShoppingApiClient naverShoppingApiClient;

    @BeforeEach
    void setUp() {
        // 테스트용 더미 값 설정 (실제 API 호출 없음)
        ReflectionTestUtils.setField(naverShoppingApiClient, "clientId", "test-client-id");
        ReflectionTestUtils.setField(naverShoppingApiClient, "clientSecret", "test-client-secret");
    }

    @Test
    @DisplayName("키워드로 상품 검색 - 성공")
    void searchProducts_Success() {
        // given
        String keyword = "물";
        
        NaverShoppingResponse.NaverProduct product = NaverShoppingResponse.NaverProduct.builder()
                .title("<b>물</b> 2L")
                .link("https://example.com/product")
                .image("https://example.com/image.jpg")
                .lprice("2000")
                .mallName("테스트몰")
                .productId("11111")
                .category1("식품")
                .category2("음료")
                .build();

        NaverShoppingResponse response = NaverShoppingResponse.builder()
                .total(1L)
                .display(1)
                .items(Arrays.asList(product))
                .build();

        given(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(NaverShoppingResponse.class)
        )).willReturn(new ResponseEntity<>(response, HttpStatus.OK));

        // when
        List<ProductDocument> products = naverShoppingApiClient.searchProducts(keyword, 10);

        // then
        assertThat(products).hasSize(1);
        
        ProductDocument product1 = products.get(0);
        // HTML 태그 제거 확인
        assertThat(product1.getName()).isEqualTo("물 2L");
        assertThat(product1.getName()).doesNotContain("<b>");
        assertThat(product1.getName()).doesNotContain("</b>");
        assertThat(product1.getPrice()).isEqualTo(2000);
    }

    @Test
    @DisplayName("빈 응답 처리")
    void searchProducts_EmptyResponse() {
        // given
        NaverShoppingResponse response = NaverShoppingResponse.builder()
                .total(0L)
                .display(0)
                .items(Arrays.asList())
                .build();

        given(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(NaverShoppingResponse.class)
        )).willReturn(new ResponseEntity<>(response, HttpStatus.OK));

        // when
        List<ProductDocument> products = naverShoppingApiClient.searchProducts("존재하지않는상품", 10);

        // then
        assertThat(products).isEmpty();
    }

    @Test
    @DisplayName("API 건강 체크 - Mock은 항상 true 반환")
    void isHealthy_Mock() {
        // when
        boolean healthy = naverShoppingApiClient.isHealthy();

        // then
        // Mock 환경에서는 실제 API 호출이 없으므로 항상 true
        assertThat(healthy).isTrue();
    }

    @Test
    @DisplayName("가격 파싱 - 정상 케이스")
    void parsePrice_ValidPrice() {
        // given
        String priceString = "15000";

        // when
        Integer price = invokeParsePrice(priceString);

        // then
        assertThat(price).isEqualTo(15000);
    }

    @Test
    @DisplayName("가격 파싱 - null 입력")
    void parsePrice_NullInput() {
        // when
        Integer price = invokeParsePrice(null);

        // then
        assertThat(price).isNull();
    }

    @Test
    @DisplayName("가격 파싱 - 빈 문자열")
    void parsePrice_EmptyString() {
        // when
        Integer price = invokeParsePrice("");

        // then
        assertThat(price).isNull();
    }

    @Test
    @DisplayName("HTML 태그 제거")
    void removeHtmlTags() {
        // given
        String htmlText = "<b>테스트</b> <em>상품</em> <strong>이름</strong>";

        // when
        String cleaned = invokeRemoveHtmlTags(htmlText);

        // then
        assertThat(cleaned).isEqualTo("테스트 상품 이름");
        assertThat(cleaned).doesNotContain("<");
        assertThat(cleaned).doesNotContain(">");
    }

    @Test
    @DisplayName("HTML 태그 제거 - null 입력")
    void removeHtmlTags_NullInput() {
        // when
        String cleaned = invokeRemoveHtmlTags(null);

        // then
        assertThat(cleaned).isNull();
    }

    // 리플렉션을 통한 private 메서드 테스트 헬퍼
    private Integer invokeParsePrice(String priceString) {
        try {
            var method = NaverShoppingApiClient.class.getDeclaredMethod("parsePrice", String.class);
            method.setAccessible(true);
            return (Integer) method.invoke(naverShoppingApiClient, priceString);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String invokeRemoveHtmlTags(String text) {
        try {
            var method = NaverShoppingApiClient.class.getDeclaredMethod("removeHtmlTags", String.class);
            method.setAccessible(true);
            return (String) method.invoke(naverShoppingApiClient, text);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
