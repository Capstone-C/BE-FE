package com.capstone.web.refrigerator.service;

import com.capstone.web.refrigerator.config.OpenAIConfig;
import com.capstone.web.refrigerator.dto.RefrigeratorDto.ScanPurchaseHistoryResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * OpenAIService 단위 테스트
 * 실제 API 호출 없이 Mock을 사용하여 테스트 (토큰 0원)
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("OpenAI Service 테스트 (Mock, 토큰 안씀)")
class OpenAIServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private OpenAIConfig openAIConfig;

    private OpenAIService openAIService;

    @BeforeEach
    void setUp() {
        when(openAIConfig.getApiUrl()).thenReturn("https://mock-openai-api.com");
        when(openAIConfig.getApiKey()).thenReturn("mock-api-key");
        when(openAIConfig.getModel()).thenReturn("gpt-5-nano");
        when(openAIConfig.getMaxTokens()).thenReturn(1000);
        
        // ObjectMapper는 실제 객체 사용 (JSON 파싱 필요)
        ObjectMapper objectMapper = new ObjectMapper();
        openAIService = new OpenAIService(openAIConfig, restTemplate, objectMapper);
    }

    @Test
    @DisplayName("영수증 텍스트 파싱 - 성공")
    void parseReceipt_Success() {
        // Given
        String receiptText = """
            CU 편의점
            사과 2개 3000원
            우유 1개 2500원
            총액: 5500원
            2025-01-15
            """;

        String mockResponse = """
            {
                "choices": [{
                    "message": {
                        "content": "{\\"store\\":\\"CU 편의점\\",\\"date\\":\\"2025-01-15\\",\\"items\\":[{\\"name\\":\\"사과\\",\\"quantity\\":2,\\"price\\":3000},{\\"name\\":\\"우유\\",\\"quantity\\":1,\\"price\\":2500}],\\"total\\":5500}"
                    }
                }]
            }
            """;

        when(restTemplate.exchange(
            anyString(),
            any(),
            any(),
            eq(String.class)
        )).thenReturn(org.springframework.http.ResponseEntity.ok(mockResponse));

        // When
        ScanPurchaseHistoryResponse response = openAIService.parseReceipt(receiptText);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getStore()).isEqualTo("CU 편의점");
        assertThat(response.getItems()).hasSize(2);
    }

    @Test
    @DisplayName("빈 텍스트 - 예외 발생")
    void parseReceipt_EmptyText_ThrowsException() {
        // Given
        String emptyText = "";

        // When & Then
        assertThatThrownBy(() -> openAIService.parseReceipt(emptyText))
            .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("API 응답 없음 - 예외 발생")
    void parseReceipt_NoResponse_ThrowsException() {
        // Given
        String receiptText = "CU 편의점\n사과 2개 3000원";

        when(restTemplate.exchange(anyString(), any(), any(), eq(String.class)))
            .thenThrow(new RuntimeException("API 호출 실패"));

        // When & Then
        assertThatThrownBy(() -> openAIService.parseReceipt(receiptText))
            .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("JSON 파싱 - items 배열 검증")
    void parseReceipt_ItemsArray() {
        // Given
        String receiptText = "테스트 영수증";

        String mockResponse = """
            {
                "choices": [{
                    "message": {
                        "content": "{\\"store\\":\\"테스트마트\\",\\"date\\":\\"2025-01-15\\",\\"items\\":[{\\"name\\":\\"감자\\",\\"quantity\\":3,\\"price\\":1500}],\\"total\\":1500}"
                    }
                }]
            }
            """;

        when(restTemplate.exchange(anyString(), any(), any(), eq(String.class)))
            .thenReturn(org.springframework.http.ResponseEntity.ok(mockResponse));

        // When
        ScanPurchaseHistoryResponse response = openAIService.parseReceipt(receiptText);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getItems()).hasSize(1);
        assertThat(response.getItems().get(0).getName()).isEqualTo("감자");
    }
}
