package com.capstone.web.ocr.exception;

/**
 * 유효하지 않은 이미지 파일 예외
 */
public class InvalidImageFileException extends RuntimeException {
    
    public InvalidImageFileException() {
        super(OcrErrorCode.INVALID_IMAGE_FILE.getMessage());
    }
    
    public InvalidImageFileException(String message) {
        super(message);
    }
    
    public InvalidImageFileException(String message, Throwable cause) {
        super(message, cause);
    }
}
