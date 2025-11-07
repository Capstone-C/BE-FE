package com.capstone.web.refrigerator.service;

import com.capstone.web.refrigerator.config.ClovaOcrConfig;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

/**
 * CLOVA OCR General API를 사용한 텍스트 추출 서비스
 * 참고: https://www.ncloud.com/product/aiService/ocr
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ClovaOcrService {

    private final ClovaOcrConfig config;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    /**
     * 이미지에서 텍스트 추출
     *
     * @param image 영수증 이미지 파일
     * @return 추출된 전체 텍스트
     */
    public String extractText(MultipartFile image) {
        try {
            // CLOVA OCR API 요청 구성
            HttpHeaders headers = new HttpHeaders();
            headers.set("X-OCR-SECRET", config.getSecretKey());
            headers.setContentType(MediaType.APPLICATION_JSON);

            // 요청 바디 구성 (Base64 인코딩된 이미지)
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("version", "V2");
            requestBody.put("requestId", UUID.randomUUID().toString());
            requestBody.put("timestamp", System.currentTimeMillis());

            Map<String, Object> imageInfo = new HashMap<>();
            imageInfo.put("format", getImageFormat(image.getOriginalFilename()));
            imageInfo.put("name", image.getOriginalFilename());
            imageInfo.put("data", Base64.getEncoder().encodeToString(image.getBytes()));

            requestBody.put("images", Collections.singletonList(imageInfo));

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            // API 호출
            log.info("CLOVA OCR API 호출: {}", config.getApiUrl());
            ResponseEntity<String> response = restTemplate.exchange(
                    config.getApiUrl(),
                    HttpMethod.POST,
                    entity,
                    String.class
            );

            // 응답 파싱
            return parseOcrResponse(response.getBody());

        } catch (Exception e) {
            log.error("CLOVA OCR 처리 실패: {}", e.getMessage(), e);
            throw new RuntimeException("OCR 텍스트 추출에 실패했습니다: " + e.getMessage());
        }
    }

    /**
     * CLOVA OCR 응답에서 텍스트 추출
     */
    private String parseOcrResponse(String responseBody) throws Exception {
        JsonNode root = objectMapper.readTree(responseBody);
        JsonNode images = root.path("images");

        if (images.isEmpty()) {
            throw new RuntimeException("OCR 응답에 이미지 데이터가 없습니다.");
        }

        StringBuilder fullText = new StringBuilder();
        JsonNode fields = images.get(0).path("fields");

        for (JsonNode field : fields) {
            String inferText = field.path("inferText").asText();
            if (!inferText.isEmpty()) {
                fullText.append(inferText).append("\n");
            }
        }

        String result = fullText.toString().trim();
        log.info("CLOVA OCR 추출 완료: {} 글자", result.length());
        return result;
    }

    /**
     * 파일 확장자에서 이미지 포맷 추출
     */
    private String getImageFormat(String filename) {
        if (filename == null) {
            return "jpg";
        }
        String extension = filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
        return switch (extension) {
            case "png" -> "png";
            case "jpeg", "jpg" -> "jpg";
            case "pdf" -> "pdf";
            default -> "jpg";
        };
    }
}
