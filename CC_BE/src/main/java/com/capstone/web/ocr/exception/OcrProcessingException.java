package com.capstone.web.ocr.exception;

/**
 * OCR 처리 실패 예외
 */
public class OcrProcessingException extends RuntimeException {
    
    public OcrProcessingException() {
        super(OcrErrorCode.OCR_PROCESSING_FAILED.getMessage());
    }
    
    public OcrProcessingException(String message) {
        super(message);
    }
    
    public OcrProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public OcrProcessingException(Throwable cause) {
        super(OcrErrorCode.OCR_PROCESSING_FAILED.getMessage(), cause);
    }
}
