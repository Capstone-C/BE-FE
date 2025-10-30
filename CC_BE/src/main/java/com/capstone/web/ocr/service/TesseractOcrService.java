package com.capstone.web.ocr.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;

/**
 * Tesseract OCR 서비스
 * 이미지에서 텍스트를 추출합니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TesseractOcrService {

    private final Tesseract tesseract;

    /**
     * 이미지 파일에서 텍스트 추출
     * 
     * @param imageFile 이미지 파일
     * @return 추출된 텍스트
     * @throws IOException 파일 읽기 오류
     * @throws TesseractException OCR 처리 오류
     */
    public String extractText(MultipartFile imageFile) throws IOException, TesseractException {
        log.info("Starting OCR text extraction for file: {}", imageFile.getOriginalFilename());
        
        // MultipartFile을 BufferedImage로 변환
        BufferedImage image = ImageIO.read(imageFile.getInputStream());
        
        if (image == null) {
            throw new IOException("Failed to read image file: " + imageFile.getOriginalFilename());
        }
        
        // Tesseract로 텍스트 추출
        String extractedText = tesseract.doOCR(image);
        
        log.info("OCR extraction completed. Extracted {} characters", 
                 extractedText != null ? extractedText.length() : 0);
        
        return extractedText != null ? extractedText.trim() : "";
    }

    /**
     * BufferedImage에서 텍스트 추출
     * 
     * @param image BufferedImage
     * @return 추출된 텍스트
     * @throws TesseractException OCR 처리 오류
     */
    public String extractText(BufferedImage image) throws TesseractException {
        log.info("Starting OCR text extraction from BufferedImage");
        
        String extractedText = tesseract.doOCR(image);
        
        log.info("OCR extraction completed. Extracted {} characters",
                 extractedText != null ? extractedText.length() : 0);
        
        return extractedText != null ? extractedText.trim() : "";
    }
}
