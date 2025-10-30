package com.capstone.web.ocr.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * OCR 관련 에러 코드
 */
@Getter
@RequiredArgsConstructor
public enum OcrErrorCode {
    
    OCR_PROCESSING_FAILED("OCR 처리 중 오류가 발생했습니다"),
    INVALID_IMAGE_FILE("유효하지 않은 이미지 파일입니다"),
    IMAGE_READ_FAILED("이미지 파일을 읽을 수 없습니다"),
    NO_TEXT_EXTRACTED("이미지에서 텍스트를 추출할 수 없습니다"),
    PARSING_FAILED("영수증 파싱 중 오류가 발생했습니다");
    
    private final String message;
}
