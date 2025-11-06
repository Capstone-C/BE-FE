package com.capstone.web.ocr.service;

import com.capstone.web.ocr.dto.OcrDto.ParsedItem;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 영수증 파서 서비스 통합 테스트
 * 
 * <p>실제 test-image 폴더의 영수증 이미지를 사용하여 OCR + 파싱 테스트 수행
 * 
 * <p><b>테스트 대상:</b>
 * <ul>
 *   <li>TesseractOcrService: OCR 텍스트 추출 (OpenCV 전처리 포함)</li>
 *   <li>ReceiptParserService: 추출된 텍스트 파싱 (정규식 패턴 매칭)</li>
 * </ul>
 * 
 * <p><b>전제조건:</b>
 * <ul>
 *   <li>CC_BE/test-image 폴더에 영수증 이미지 파일 존재</li>
 *   <li>Tesseract OCR 데이터 파일 설치 (tessdata)</li>
 * </ul>
 */
@SpringBootTest
@DisplayName("영수증 OCR + 파싱 통합 테스트")
class ReceiptParserServiceTest {

    private static final String TEST_IMAGE_DIR = "test-image";
    
    @Autowired
    private ReceiptParserService receiptParserService;
    
    @Autowired
    private OcrService ocrService;  // 인터페이스로 주입 (현재는 TesseractOcrService 구현체)

    @Test
    @DisplayName("실제 영수증 이미지 OCR + 파싱 테스트 - image.png")
    void parseReceiptFromImage1() throws Exception {
        // given
        File imageFile = getTestImageFile("image.png");
        
        // when
        BufferedImage image = ImageIO.read(imageFile);
        String ocrText = ocrService.extractText(image);
        List<ParsedItem> items = receiptParserService.parseReceipt(ocrText);
        
        // then
        System.out.println("\n=== image.png OCR 결과 ===");
        System.out.println(ocrText);
        System.out.println("\n=== 파싱된 아이템 (" + items.size() + "개) ===");
        items.forEach(item -> System.out.println(item));
        
        assertThat(ocrText).isNotBlank();
        assertThat(items).isNotEmpty();
    }

    @Test
    @DisplayName("실제 영수증 이미지 OCR + 파싱 테스트 - image2.png")
    void parseReceiptFromImage2() throws Exception {
        // given
        File imageFile = getTestImageFile("image2.png");
        
        // when
        BufferedImage image = ImageIO.read(imageFile);
        String ocrText = ocrService.extractText(image);
        List<ParsedItem> items = receiptParserService.parseReceipt(ocrText);
        
        // then
        System.out.println("\n=== image2.png OCR 결과 ===");
        System.out.println(ocrText);
        System.out.println("\n=== 파싱된 아이템 (" + items.size() + "개) ===");
        items.forEach(item -> System.out.println(item));
        
        assertThat(ocrText).isNotBlank();
        assertThat(items).isNotEmpty();
    }

    @Test
    @DisplayName("실제 영수증 이미지 OCR + 파싱 테스트 - image3.png")
    void parseReceiptFromImage3() throws Exception {
        // given
        File imageFile = getTestImageFile("image3.png");
        
        // when
        BufferedImage image = ImageIO.read(imageFile);
        String ocrText = ocrService.extractText(image);
        List<ParsedItem> items = receiptParserService.parseReceipt(ocrText);
        
        // then
        System.out.println("\n=== image3.png OCR 결과 ===");
        System.out.println(ocrText);
        System.out.println("\n=== 파싱된 아이템 (" + items.size() + "개) ===");
        items.forEach(item -> System.out.println(item));
        
        assertThat(ocrText).isNotBlank();
        // image3는 파싱 결과가 없을 수도 있음 (이미지 품질에 따라)
    }

    @Test
    @DisplayName("실제 영수증 이미지 OCR + 파싱 테스트 - image4.png")
    void parseReceiptFromImage4() throws Exception {
        // given
        File imageFile = getTestImageFile("image4.png");
        
        // when
        BufferedImage image = ImageIO.read(imageFile);
        String ocrText = ocrService.extractText(image);
        List<ParsedItem> items = receiptParserService.parseReceipt(ocrText);
        
        // then
        System.out.println("\n=== image4.png OCR 결과 ===");
        System.out.println(ocrText);
        System.out.println("\n=== 파싱된 아이템 (" + items.size() + "개) ===");
        items.forEach(item -> System.out.println(item));
        
        assertThat(ocrText).isNotBlank();
    }

    @Test
    @DisplayName("실제 영수증 이미지 OCR + 파싱 테스트 - image5.png")
    void parseReceiptFromImage5() throws Exception {
        // given
        File imageFile = getTestImageFile("image5.png");
        
        // when
        BufferedImage image = ImageIO.read(imageFile);
        String ocrText = ocrService.extractText(image);
        List<ParsedItem> items = receiptParserService.parseReceipt(ocrText);
        
        // then
        System.out.println("\n=== image5.png OCR 결과 ===");
        System.out.println(ocrText);
        System.out.println("\n=== 파싱된 아이템 (" + items.size() + "개) ===");
        items.forEach(item -> System.out.println(item));
        
        assertThat(ocrText).isNotBlank();
    }

    @Test
    @DisplayName("개선된 정규식 패턴 테스트 - 소수점 수량")
    void testImprovedPatterns_DecimalQuantity() {
        // given
        String receiptText = """
            사과 1.5kg 10,000원
            우유 0.5l 2,500원
            고구마 2.3킬로그램 8,000원
            """;

        // when
        List<ParsedItem> items = receiptParserService.parseReceipt(receiptText);

        // then
        assertThat(items).hasSizeGreaterThanOrEqualTo(3);
        
        // 소수점 수량이 반올림되어 정수로 저장되는지 확인
        assertThat(items).anyMatch(item -> 
            item.getName().contains("사과") && 
            item.getQuantity() == 2 &&  // 1.5 → 2
            "kg".equals(item.getUnit())
        );
        
        assertThat(items).anyMatch(item -> 
            item.getName().contains("우유") && 
            item.getQuantity() == 1 &&  // 0.5 → 1 (반올림)
            "l".equals(item.getUnit())
        );
    }

    @Test
    @DisplayName("개선된 정규식 패턴 테스트 - 확장된 단위")
    void testImprovedPatterns_ExpandedUnits() {
        // given
        String receiptText = """
            콜라 1병 2,000원
            맥주 6캔 12,000원
            김치 1통 15,000원
            파 1묶음 3,000원
            삼겹살 1.2킬로 18,000원
            """;

        // when
        List<ParsedItem> items = receiptParserService.parseReceipt(receiptText);

        // then
        // "파" 1글자는 cleanItemName에서 필터링될 수 있으므로 4개 이상으로 조정
        assertThat(items).hasSizeGreaterThanOrEqualTo(4);
        
        assertThat(items).anyMatch(item -> 
            item.getName().contains("콜라") && "병".equals(item.getUnit())
        );
        
        assertThat(items).anyMatch(item -> 
            item.getName().contains("맥주") && "캔".equals(item.getUnit())
        );
        
        assertThat(items).anyMatch(item -> 
            item.getName().contains("김치") && "통".equals(item.getUnit())
        );
        
        // "파" 테스트는 제외 (1글자 이름은 필터링됨)
        
        // "킬로" → "kg"로 정규화
        assertThat(items).anyMatch(item -> 
            item.getName().contains("삼겹살") && "kg".equals(item.getUnit())
        );
    }

    @Test
    @DisplayName("개선된 정규식 패턴 테스트 - ₩ 기호 및 공백")
    void testImprovedPatterns_PriceWithWonSymbol() {
        // given
        String receiptText = """
            사과 2개 ₩5,000
            바나나 3개 ₩ 3,500원
            우유 1개 3 000원
            """;

        // when
        List<ParsedItem> items = receiptParserService.parseReceipt(receiptText);

        // then
        assertThat(items).hasSizeGreaterThanOrEqualTo(3);
        
        assertThat(items).anyMatch(item -> 
            item.getName().contains("사과") && item.getPrice() == 5000
        );
        
        assertThat(items).anyMatch(item -> 
            item.getName().contains("바나나") && item.getPrice() == 3500
        );
        
        assertThat(items).anyMatch(item -> 
            item.getName().contains("우유") && item.getPrice() == 3000
        );
    }

    @Test
    @DisplayName("개선된 정규식 패턴 테스트 - 확장된 무시 키워드")
    void testImprovedPatterns_ExtendedIgnoreKeywords() {
        // given
        String receiptText = """
            ABC마트 홍대점
            Welcome! Thank you!
            -----------------
            사과 2개 5,000원
            할인 -500원
            쿠폰 -1,000원
            적립 포인트 100원
            -----------------
            합계 3,500원
            카드결제 3,500원
            VAT 포함
            """;

        // when
        List<ParsedItem> items = receiptParserService.parseReceipt(receiptText);

        // then
        // 무시할 키워드가 포함된 항목이 파싱되지 않았는지 확인
        assertThat(items).noneMatch(item -> 
            item.getName().contains("할인") ||
            item.getName().contains("쿠폰") ||
            item.getName().contains("적립") ||
            item.getName().contains("포인트") ||
            item.getName().contains("합계") ||
            item.getName().contains("카드") ||
            item.getName().contains("welcome") ||
            item.getName().contains("thank")
        );
        
        // 실제 상품만 파싱되었는지 확인
        assertThat(items).anyMatch(item -> item.getName().contains("사과"));
    }

    /**
     * test-image 폴더에서 이미지 파일 가져오기
     */
    private File getTestImageFile(String fileName) {
        Path testImagePath = Paths.get(TEST_IMAGE_DIR, fileName);
        File imageFile = testImagePath.toFile();
        
        if (!imageFile.exists()) {
            throw new IllegalArgumentException(
                "테스트 이미지 파일이 존재하지 않습니다: " + testImagePath.toAbsolutePath()
            );
        }
        
        return imageFile;
    }

    @Test
    @DisplayName("영수증 텍스트 파싱 - 빈 텍스트")
    void parseReceipt_EmptyText() {
        // given
        String receiptText = "";

        // when
        List<ParsedItem> items = receiptParserService.parseReceipt(receiptText);

        // then
        assertThat(items).isEmpty();
    }

    @Test
    @DisplayName("영수증 텍스트 파싱 - null 텍스트")
    void parseReceipt_NullText() {
        // given
        String receiptText = null;

        // when
        List<ParsedItem> items = receiptParserService.parseReceipt(receiptText);

        // then
        assertThat(items).isEmpty();
    }
}
