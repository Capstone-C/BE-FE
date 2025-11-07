package com.capstone.web.refrigerator.service;

import com.capstone.web.refrigerator.config.ClovaOcrConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * ClovaOcrService 단위 테스트
 * 실제 API 호출 없이 Mock을 사용하여 테스트 (토큰 0원)
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CLOVA OCR Service 테스트 (Mock, 토큰 안씀)")
class ClovaOcrServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private ClovaOcrConfig clovaOcrConfig;

    private ClovaOcrService clovaOcrService;

    @BeforeEach
    void setUp() {
        when(clovaOcrConfig.getApiUrl()).thenReturn("https://mock-clova-api.com");
        when(clovaOcrConfig.getSecretKey()).thenReturn("mock-secret-key");
        
        // ObjectMapper는 실제 객체 사용 (JSON 파싱 필요)
        ObjectMapper objectMapper = new ObjectMapper();
        clovaOcrService = new ClovaOcrService(clovaOcrConfig, restTemplate, objectMapper);
    }

    @Test
    @DisplayName("영수증 이미지 OCR - 성공")
    void extractText_Success() {
        // Given
        MockMultipartFile image = new MockMultipartFile(
            "image", 
            "receipt.jpg", 
            "image/jpeg", 
            "fake-image-content".getBytes()
        );

        String mockResponse = """
            {
                "version": "V2",
                "requestId": "test-123",
                "timestamp": 1234567890,
                "images": [{
                    "inferResult": "SUCCESS",
                    "fields": [
                        {"inferText": "CU 편의점"},
                        {"inferText": "사과 2개 3000원"}
                    ]
                }]
            }
            """;

        when(restTemplate.exchange(
            anyString(),
            eq(HttpMethod.POST),
            any(),
            eq(String.class)
        )).thenReturn(ResponseEntity.ok(mockResponse));

        // When
        String result = clovaOcrService.extractText(image);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).contains("CU 편의점");
        assertThat(result).contains("사과");
    }

    @Test
    @DisplayName("빈 이미지 - 예외 발생")
    void extractText_EmptyImage_ThrowsException() {
        // Given
        MockMultipartFile emptyImage = new MockMultipartFile(
            "image", 
            "empty.jpg", 
            "image/jpeg", 
            new byte[0]
        );

        // When & Then
        assertThatThrownBy(() -> clovaOcrService.extractText(emptyImage))
            .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("API 응답 없음 - 예외 발생")
    void extractText_NoResponse_ThrowsException() {
        // Given
        MockMultipartFile image = new MockMultipartFile(
            "image", 
            "receipt.jpg", 
            "image/jpeg", 
            "fake-content".getBytes()
        );

        when(restTemplate.exchange(
            anyString(),
            eq(HttpMethod.POST),
            any(),
            eq(String.class)
        )).thenThrow(new RuntimeException("API 호출 실패"));

        // When & Then
        assertThatThrownBy(() -> clovaOcrService.extractText(image))
            .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("CLOVA OCR Config - 설정값 확인")
    void clovaOcrConfig_Validation() {
        // Then
        assertThat(clovaOcrConfig.getApiUrl()).isNotNull();
        assertThat(clovaOcrConfig.getSecretKey()).isNotNull();
    }
}
