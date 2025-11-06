package com.capstone.web.ocr.service;

import lombok.extern.slf4j.Slf4j;
import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * OpenCV를 활용한 이미지 전처리 서비스
 * 
 * <p>OCR 정확도 향상을 위해 다음과 같은 이미지 전처리를 수행합니다:
 * <ul>
 *   <li><b>그레이스케일 변환</b>: 컬러 정보 제거로 텍스트 인식 집중</li>
 *   <li><b>노이즈 제거</b>: Gaussian Blur로 이미지 잡음 제거</li>
 *   <li><b>적응형 이진화</b>: 조명 불균형 보정 및 텍스트 강조</li>
 *   <li><b>형태학적 연산</b>: 텍스트 연결성 개선</li>
 *   <li><b>리사이즈</b>: OCR 최적 크기로 조정</li>
 * </ul>
 * 
 * <p><b>사용 예시:</b>
 * <pre>
 * {@code
 * BufferedImage original = ImageIO.read(uploadedFile);
 * BufferedImage preprocessed = imagePreprocessor.preprocessImage(original);
 * String ocrText = tesseractService.doOCR(preprocessed);
 * }
 * </pre>
 * 
 * @author Capstone Team
 * @version 1.0
 * @see TesseractOcrService
 */
@Slf4j
@Service
public class ImagePreprocessorService {

    // OpenCV 네이티브 라이브러리 로드
    static {
        try {
            nu.pattern.OpenCV.loadLocally();
            log.info("OpenCV 라이브러리 로드 완료");
        } catch (Exception e) {
            log.error("OpenCV 라이브러리 로드 실패: {}", e.getMessage());
            throw new RuntimeException("OpenCV 초기화 실패", e);
        }
    }

    // === 전처리 설정값 ===
    private static final int TARGET_WIDTH = 1800;      // OCR 최적 너비 (픽셀)
    private static final int GAUSSIAN_KERNEL = 3;       // 가우시안 블러 커널 크기 (홀수)
    private static final int ADAPTIVE_BLOCK_SIZE = 15;  // 적응형 이진화 블록 크기 (홀수)
    private static final int ADAPTIVE_C = 10;           // 적응형 이진화 상수

    /**
     * 이미지 전처리 메인 메서드
     * 
     * @param originalImage 원본 BufferedImage
     * @return 전처리된 BufferedImage (OCR 최적화)
     * @throws IllegalArgumentException 이미지가 null인 경우
     */
    public BufferedImage preprocessImage(BufferedImage originalImage) {
        if (originalImage == null) {
            throw new IllegalArgumentException("이미지가 null입니다.");
        }

        log.debug("이미지 전처리 시작: {}x{}", originalImage.getWidth(), originalImage.getHeight());

        try {
            // 1. BufferedImage → OpenCV Mat 변환
            Mat mat = bufferedImageToMat(originalImage);
            log.debug("1. Mat 변환 완료: {}x{}", mat.width(), mat.height());

            // 2. 그레이스케일 변환
            Mat gray = convertToGrayscale(mat);
            log.debug("2. 그레이스케일 변환 완료");

            // 3. 노이즈 제거 (Gaussian Blur)
            Mat denoised = removeNoise(gray);
            log.debug("3. 노이즈 제거 완료");

            // 4. 적응형 이진화 (Adaptive Thresholding)
            Mat binary = applyAdaptiveThreshold(denoised);
            log.debug("4. 적응형 이진화 완료");

            // 5. 형태학적 연산 (Morphological Operations)
            Mat morphed = applyMorphology(binary);
            log.debug("5. 형태학적 연산 완료");

            // 6. 리사이즈 (OCR 최적 크기)
            Mat resized = resizeForOcr(morphed);
            log.debug("6. 리사이즈 완료: {}x{}", resized.width(), resized.height());

            // 7. OpenCV Mat → BufferedImage 변환
            BufferedImage result = matToBufferedImage(resized);
            log.debug("7. 이미지 전처리 완료");

            // 메모리 해제
            mat.release();
            gray.release();
            denoised.release();
            binary.release();
            morphed.release();
            resized.release();

            return result;

        } catch (Exception e) {
            log.error("이미지 전처리 중 오류 발생: {}", e.getMessage(), e);
            // 전처리 실패 시 원본 이미지 반환 (fallback)
            return originalImage;
        }
    }

    /**
     * BufferedImage → OpenCV Mat 변환
     */
    private Mat bufferedImageToMat(BufferedImage image) {
        // BGR 컬러 공간으로 변환
        BufferedImage convertedImage = new BufferedImage(
                image.getWidth(),
                image.getHeight(),
                BufferedImage.TYPE_3BYTE_BGR
        );
        convertedImage.getGraphics().drawImage(image, 0, 0, null);

        byte[] pixels = ((DataBufferByte) convertedImage.getRaster().getDataBuffer()).getData();
        Mat mat = new Mat(convertedImage.getHeight(), convertedImage.getWidth(), CvType.CV_8UC3);
        mat.put(0, 0, pixels);

        return mat;
    }

    /**
     * OpenCV Mat → BufferedImage 변환
     */
    private BufferedImage matToBufferedImage(Mat mat) throws IOException {
        MatOfByte mob = new MatOfByte();
        org.opencv.imgcodecs.Imgcodecs.imencode(".png", mat, mob);
        byte[] byteArray = mob.toArray();

        return ImageIO.read(new ByteArrayInputStream(byteArray));
    }

    /**
     * 그레이스케일 변환
     * - 컬러 정보를 제거하여 텍스트 인식에 집중
     */
    private Mat convertToGrayscale(Mat src) {
        Mat gray = new Mat();
        if (src.channels() > 1) {
            Imgproc.cvtColor(src, gray, Imgproc.COLOR_BGR2GRAY);
        } else {
            gray = src.clone();
        }
        return gray;
    }

    /**
     * 노이즈 제거 (Gaussian Blur)
     * - 이미지 잡음을 제거하여 텍스트 경계를 명확하게 함
     * 
     * @param src 입력 Mat (그레이스케일)
     * @return 노이즈가 제거된 Mat
     */
    private Mat removeNoise(Mat src) {
        Mat denoised = new Mat();
        Imgproc.GaussianBlur(
                src,
                denoised,
                new Size(GAUSSIAN_KERNEL, GAUSSIAN_KERNEL),
                0  // sigmaX (자동 계산)
        );
        return denoised;
    }

    /**
     * 적응형 이진화 (Adaptive Thresholding)
     * - 조명 불균형을 보정하고 텍스트를 강조
     * - 영수증처럼 조명이 불균일한 이미지에 효과적
     * 
     * @param src 입력 Mat (그레이스케일, 노이즈 제거됨)
     * @return 이진화된 Mat
     */
    private Mat applyAdaptiveThreshold(Mat src) {
        Mat binary = new Mat();
        Imgproc.adaptiveThreshold(
                src,
                binary,
                255,                                    // maxValue
                Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C,     // 적응형 방법 (가우시안)
                Imgproc.THRESH_BINARY,                  // 이진화 타입
                ADAPTIVE_BLOCK_SIZE,                    // 블록 크기 (홀수)
                ADAPTIVE_C                              // 상수 (평균에서 빼는 값)
        );
        return binary;
    }

    /**
     * 형태학적 연산 (Morphological Operations)
     * - Opening: 작은 노이즈 제거
     * - Closing: 텍스트 내부 공백 채우기
     * 
     * @param src 입력 Mat (이진화됨)
     * @return 형태학적 연산이 적용된 Mat
     */
    private Mat applyMorphology(Mat src) {
        Mat morphed = new Mat();

        // 커널 생성 (사각형 2x2)
        Mat kernel = Imgproc.getStructuringElement(
                Imgproc.MORPH_RECT,
                new Size(2, 2)
        );

        // Opening: 노이즈 제거 (침식 → 팽창)
        Imgproc.morphologyEx(src, morphed, Imgproc.MORPH_OPEN, kernel);

        // Closing: 텍스트 연결성 개선 (팽창 → 침식)
        Imgproc.morphologyEx(morphed, morphed, Imgproc.MORPH_CLOSE, kernel);

        kernel.release();
        return morphed;
    }

    /**
     * OCR 최적 크기로 리사이즈
     * - Tesseract OCR은 300 DPI (약 1800px 너비) 권장
     * - 너비 기준으로 비율 유지하며 리사이즈
     * 
     * @param src 입력 Mat
     * @return 리사이즈된 Mat
     */
    private Mat resizeForOcr(Mat src) {
        // 이미 충분히 큰 경우 리사이즈 생략
        if (src.width() >= TARGET_WIDTH * 0.9 && src.width() <= TARGET_WIDTH * 1.1) {
            return src.clone();
        }

        Mat resized = new Mat();
        double scale = (double) TARGET_WIDTH / src.width();
        int newHeight = (int) (src.height() * scale);

        Imgproc.resize(
                src,
                resized,
                new Size(TARGET_WIDTH, newHeight),
                0,
                0,
                Imgproc.INTER_CUBIC  // 고품질 보간법
        );

        return resized;
    }

    /**
     * 이미지 전처리 적용 여부 확인
     * - 이미지가 너무 작거나 이미 전처리된 것으로 보이면 false 반환
     * 
     * @param image 확인할 이미지
     * @return 전처리 필요 여부
     */
    public boolean shouldPreprocess(BufferedImage image) {
        if (image == null) {
            return false;
        }

        // 너무 작은 이미지는 전처리 생략 (OCR 정확도 낮음)
        if (image.getWidth() < 200 || image.getHeight() < 200) {
            log.warn("이미지 크기가 너무 작습니다: {}x{}", image.getWidth(), image.getHeight());
            return false;
        }

        return true;
    }
}
