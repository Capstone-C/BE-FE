package com.capstone.web.ocr.service;

// Tesseract 제거됨 - import net.sourceforge.tess4j.TesseractException;
import org.springframework.web.multipart.MultipartFile;

import java.awt.image.BufferedImage;
import java.io.IOException;

/**
 * OCR 서비스 인터페이스
 * <p>이미지에서 텍스트를 추출하는 전략(Strategy)을 정의합니다.
 * 
 * <p><b>구현체:</b>
 * <ul>
 *   <li>ClovaOcrService - CLOVA OCR API 기반 (현재 사용 중 - REF-04)</li>
 *   <li>TesseractOcrService - Tesseract 4.0 기반 (제거됨)</li>
 * </ul>
 * 
 * @deprecated 이 인터페이스는 Tesseract 제거로 인해 더 이상 사용되지 않습니다.
 *             대신 ClovaOcrService를 직접 사용하세요.
 */
@Deprecated
public interface OcrService {
    
    /**
     * 이미지 파일에서 텍스트 추출
     * 
     * @param imageFile 영수증 이미지 파일
     * @return 추출된 텍스트
     * @throws IOException 파일 읽기 오류
     * @deprecated Tesseract 제거됨. ClovaOcrService.extractText() 사용
     */
    @Deprecated
    String extractText(MultipartFile imageFile) throws IOException;
    
    /**
     * BufferedImage에서 텍스트 추출
     * 
     * @param image 영수증 이미지
     * @return 추출된 텍스트
     * @deprecated Tesseract 제거됨. ClovaOcrService.extractText() 사용
     */
    @Deprecated
    String extractText(BufferedImage image);
}
