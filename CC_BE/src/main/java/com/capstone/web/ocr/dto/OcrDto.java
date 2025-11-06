package com.capstone.web.ocr.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

/**
 * OCR 관련 DTO 클래스
 */
public class OcrDto {

    /**
     * OCR 스캔 응답 DTO
     */
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ScanResponse {
        
        /**
         * 추출된 원본 텍스트
         */
        private String extractedText;
        
        /**
         * 파싱된 식재료 목록
         */
        private List<ParsedItem> parsedItems;
        
        /**
         * 냉장고에 자동 등록된 식재료 개수
         */
        private Integer addedCount;
        
        /**
         * 등록 실패한 식재료 개수
         */
        private Integer failedCount;
        
        /**
         * 등록 실패한 식재료 목록 (이름: 실패 사유)
         */
        private List<String> failedItems;
    }

    /**
     * 파싱된 식재료 정보 DTO
     */
    @Getter
    @Builder
    @ToString
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ParsedItem {
        
        /**
         * 식재료명
         */
        @NotNull
        private String name;
        
        /**
         * 수량 (선택)
         */
        private Integer quantity;
        
        /**
         * 용량/단위 (선택)
         */
        private String unit;
        
        /**
         * 가격 (파싱용, 저장하지 않음)
         */
        private Integer price;
    }
}
