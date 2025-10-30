package com.capstone.web.ocr.config;

import net.sourceforge.tess4j.Tesseract;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OCR (Tesseract) 설정 클래스
 */
@Configuration
public class OcrConfig {

    @Value("${ocr.tesseract.datapath:}")
    private String tessDataPath;

    @Value("${ocr.tesseract.language:kor+eng}")
    private String language;

    /**
     * Tesseract OCR 엔진 빈 생성
     * 
     * @return Tesseract 인스턴스
     */
    @Bean
    public Tesseract tesseract() {
        Tesseract tesseract = new Tesseract();
        
        // tessdata 경로 설정 (비어있으면 시스템 기본 경로 사용)
        if (tessDataPath != null && !tessDataPath.isEmpty()) {
            tesseract.setDatapath(tessDataPath);
        }
        
        // 언어 설정 (기본: 한국어 + 영어)
        tesseract.setLanguage(language);
        
        // OCR 엔진 모드 설정 (LSTM OCR Engine)
        tesseract.setOcrEngineMode(1);
        
        // 페이지 세그멘테이션 모드 설정 (자동 페이지 세그멘테이션)
        tesseract.setPageSegMode(3);
        
        return tesseract;
    }
}
