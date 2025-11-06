package com.capstone.web.ocr.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;

/**
 * Tesseract OCR 서비스 구현체
 * <p>Tesseract 4.0 엔진을 사용하여 이미지에서 텍스트를 추출합니다.
 * 
 * <p><b>주요 기능:</b>
 * <ul>
 *   <li>OpenCV 기반 이미지 전처리 (노이즈 제거, 이진화 등)</li>
 *   <li>Tesseract 4.0 OCR 엔진을 통한 텍스트 추출</li>
 *   <li>전처리 실패 시 원본 이미지로 fallback</li>
 * </ul>
 * 
 * <p><b>전환 가이드:</b>
 * <p>AI API 기반 OCR로 전환하려면:
 * <ol>
 *   <li>AiOcrService 클래스 생성 (OcrService 인터페이스 구현)</li>
 *   <li>application.yml에 ocr.type: ai 설정</li>
 *   <li>@ConditionalOnProperty로 구현체 선택</li>
 * </ol>
 * 
 * @see OcrService
 * @see AI_OCR_MIGRATION_GUIDE.md
 */
@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "ocr.type", havingValue = "tesseract", matchIfMissing = true)
public class TesseractOcrService implements OcrService {

    private final Tesseract tesseract;
    private final ImagePreprocessorService imagePreprocessor;

    /**
     * 이미지 파일에서 텍스트 추출
     * <p>OpenCV 전처리를 적용한 후 Tesseract OCR 수행
     * 
     * @param imageFile 이미지 파일
     * @return 추출된 텍스트
     * @throws IOException 파일 읽기 오류
     * @throws TesseractException OCR 처리 오류
     */
    @Override
    public String extractText(MultipartFile imageFile) throws IOException, TesseractException {
        log.info("Starting OCR text extraction for file: {}", imageFile.getOriginalFilename());
        
        // MultipartFile을 BufferedImage로 변환
        BufferedImage originalImage = ImageIO.read(imageFile.getInputStream());
        
        if (originalImage == null) {
            throw new IOException("Failed to read image file: " + imageFile.getOriginalFilename());
        }
        
        // 이미지 전처리 (OpenCV)
        BufferedImage processedImage = preprocessImageIfNeeded(originalImage);
        
        // Tesseract로 텍스트 추출
        String extractedText = tesseract.doOCR(processedImage);
        
        log.info("OCR extraction completed. Extracted {} characters", 
                 extractedText != null ? extractedText.length() : 0);
        
        return extractedText != null ? extractedText.trim() : "";
    }

    /**
     * BufferedImage에서 텍스트 추출
     * <p>OpenCV 전처리를 적용한 후 Tesseract OCR 수행
     * 
     * @param image BufferedImage
     * @return 추출된 텍스트
     * @throws TesseractException OCR 처리 오류
     */
    @Override
    public String extractText(BufferedImage image) throws TesseractException {
        log.info("Starting OCR text extraction from BufferedImage");
        
        // 이미지 전처리 (OpenCV)
        BufferedImage processedImage = preprocessImageIfNeeded(image);
        
        String extractedText = tesseract.doOCR(processedImage);
        
        log.info("OCR extraction completed. Extracted {} characters",
                 extractedText != null ? extractedText.length() : 0);
        
        return extractedText != null ? extractedText.trim() : "";
    }

    /**
     * 이미지 전처리 적용 (조건부)
     * <p>전처리가 필요하고 가능한 경우에만 OpenCV 전처리 적용
     * 
     * @param originalImage 원본 이미지
     * @return 전처리된 이미지 (실패 시 원본)
     */
    private BufferedImage preprocessImageIfNeeded(BufferedImage originalImage) {
        try {
            // 전처리 필요 여부 확인 (너무 작은 이미지 등은 건너뜀)
            if (!imagePreprocessor.shouldPreprocess(originalImage)) {
                log.debug("이미지 전처리 건너뜀 (조건 미충족)");
                return originalImage;
            }

            log.debug("OpenCV 이미지 전처리 시작...");
            BufferedImage preprocessed = imagePreprocessor.preprocessImage(originalImage);
            log.info("OpenCV 이미지 전처리 완료 ✓");
            return preprocessed;

        } catch (Exception e) {
            // 전처리 실패 시 원본 이미지 사용 (fallback)
            log.warn("이미지 전처리 실패 (원본 사용): {}", e.getMessage());
            return originalImage;
        }
    }
}
