package com.capstone.web.ocr.service;

import net.sourceforge.tess4j.TesseractException;
import org.springframework.web.multipart.MultipartFile;

import java.awt.image.BufferedImage;
import java.io.IOException;

/**
 * OCR 서비스 인터페이스
 * <p>이미지에서 텍스트를 추출하는 전략(Strategy)을 정의합니다.
 * 
 * <p><b>구현체:</b>
 * <ul>
 *   <li>{@link TesseractOcrService} - Tesseract 4.0 기반 로컬 OCR (현재 구현)</li>
 *   <li>AiOcrService - AI API 기반 OCR (향후 구현 예정)</li>
 * </ul>
 * 
 * <p><b>사용 예시:</b>
 * <pre>{@code
 * @Autowired
 * private OcrService ocrService;  // Spring이 자동으로 구현체 주입
 * 
 * String text = ocrService.extractText(imageFile);
 * }</pre>
 */
public interface OcrService {
    
    /**
     * 이미지 파일에서 텍스트 추출
     * 
     * @param imageFile 영수증 이미지 파일
     * @return 추출된 텍스트
     * @throws IOException 파일 읽기 오류
     * @throws TesseractException OCR 처리 오류
     */
    String extractText(MultipartFile imageFile) throws IOException, TesseractException;
    
    /**
     * BufferedImage에서 텍스트 추출
     * 
     * @param image 영수증 이미지
     * @return 추출된 텍스트
     * @throws TesseractException OCR 처리 오류
     */
    String extractText(BufferedImage image) throws TesseractException;
}
