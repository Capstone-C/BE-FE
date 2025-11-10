package com.capstone.web.integration;

import com.capstone.web.member.domain.Member;
import com.capstone.web.member.domain.MemberRole;
import com.capstone.web.member.repository.MemberRepository;
import com.capstone.web.refrigerator.dto.RefrigeratorDto;
import com.capstone.web.refrigerator.service.GeminiService;
import com.capstone.web.refrigerator.service.RefrigeratorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * 시나리오 1: 영수증 인식 → 냉장고 추가 통합 테스트
 * 
 * 테스트 흐름:
 * 1. Gemini API를 통해 영수증 이미지 파싱
 * 2. 파싱된 데이터를 검증
 * 3. 냉장고에 일괄 추가
 * 4. 추가된 데이터 확인
 * 
 * 필요 환경변수:
 * - GEMINI_API_KEY: Gemini API 키
 * - TEST_RECEIPT_IMAGE_PATH: 테스트용 영수증 이미지 경로 (선택)
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("통합 테스트 - 시나리오 1: 영수증 인식 → 냉장고 추가")
class ReceiptToRefrigeratorIntegrationTest {

    @Autowired
    private GeminiService geminiService;

    @Autowired
    private RefrigeratorService refrigeratorService;

    @Autowired
    private MemberRepository memberRepository;

    private Member testMember;

    @BeforeEach
    void setUp() {
        // 테스트용 회원 생성
        testMember = Member.builder()
                .email("test@example.com")
                .password("password")
                .nickname("테스트유저")
                .role(MemberRole.USER)
                .build();
        testMember = memberRepository.save(testMember);
    }

    @Test
    @DisplayName("전체 시나리오: 영수증 이미지 → Gemini 파싱 → 냉장고 일괄 추가")
    void fullScenario_receiptImageToRefrigerator() throws IOException {
        // given: 환경변수 확인
        String apiKey = System.getenv("GEMINI_API_KEY");
        assumeTrue(apiKey != null && !apiKey.isBlank(), 
                "GEMINI_API_KEY 환경변수가 설정되지 않았습니다. 테스트를 건너뜁니다.");

        // given: 테스트 영수증 이미지 준비
        MockMultipartFile receiptImage = prepareTestReceiptImage();

        // when: Step 1 - Gemini API로 영수증 파싱
        RefrigeratorDto.ScanPurchaseHistoryResponse scanResult = 
                geminiService.parseReceiptImage(receiptImage);

        // then: Step 1 검증 - Gemini가 적절한 값을 반환했는지 확인
        assertThat(scanResult).isNotNull();
        assertThat(scanResult.getItems()).isNotEmpty();
        
        // 파싱된 아이템 로깅
        System.out.println("=== Gemini 파싱 결과 ===");
        scanResult.getItems().forEach(item -> 
            System.out.printf("- %s: %d%s%n", 
                item.getName(), 
                item.getQuantity(), 
                item.getUnit() != null ? item.getUnit() : "")
        );

        // 기본적인 검증
        scanResult.getItems().forEach(item -> {
            assertThat(item.getName()).isNotBlank();
            assertThat(item.getQuantity()).isGreaterThan(0);
        });

        // when: Step 2 - 파싱 결과를 냉장고에 일괄 추가
        RefrigeratorDto.BulkCreateRequest bulkRequest = RefrigeratorDto.BulkCreateRequest.builder()
                .items(scanResult.getItems().stream()
                        .map(item -> RefrigeratorDto.CreateRequest.builder()
                                .name(item.getName())
                                .quantity(item.getQuantity())
                                .unit(item.getUnit())
                                .build())
                        .toList())
                .build();

        RefrigeratorDto.BulkCreateResponse bulkResult = 
                refrigeratorService.addItemsBulk(testMember.getId(), bulkRequest);

        // then: Step 2 검증 - 냉장고에 적절하게 추가되었는지 확인
        assertThat(bulkResult.getSuccessCount()).isGreaterThan(0);
        assertThat(bulkResult.getAddedItems()).isNotEmpty();
        
        System.out.println("=== 냉장고 추가 결과 ===");
        System.out.printf("성공: %d개, 실패: %d개%n", 
                bulkResult.getSuccessCount(), 
                bulkResult.getFailCount());

        // when: Step 3 - 냉장고 목록 조회로 최종 확인
        RefrigeratorDto.ItemListResponse myItems = 
                refrigeratorService.getMyItems(testMember.getId(), "name");

        // then: Step 3 검증 - 추가된 항목들이 조회되는지 확인
        assertThat(myItems.getItems()).hasSizeGreaterThanOrEqualTo(bulkResult.getSuccessCount());
        
        System.out.println("=== 최종 냉장고 목록 ===");
        myItems.getItems().forEach(item -> 
            System.out.printf("- %s: %d%s%n", 
                item.getName(), 
                item.getQuantity(), 
                item.getUnit() != null ? item.getUnit() : "")
        );
    }

    @Test
    @DisplayName("Step 1만 테스트: Gemini API 영수증 파싱")
    void step1_geminiParseReceipt() throws IOException {
        // given
        String apiKey = System.getenv("GEMINI_API_KEY");
        assumeTrue(apiKey != null && !apiKey.isBlank(), 
                "GEMINI_API_KEY 환경변수가 설정되지 않았습니다. 테스트를 건너뜁니다.");

        MockMultipartFile receiptImage = prepareTestReceiptImage();

        // when
        RefrigeratorDto.ScanPurchaseHistoryResponse result = 
                geminiService.parseReceiptImage(receiptImage);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getItems()).isNotEmpty();
        
        result.getItems().forEach(item -> {
            assertThat(item.getName()).isNotBlank();
            assertThat(item.getQuantity()).isGreaterThan(0);
        });
    }

    @Test
    @DisplayName("Step 2-3만 테스트: 파싱 데이터 → 냉장고 추가 (Gemini 없이)")
    void step2_3_bulkAddToRefrigerator() {
        // given: 사전 정의된 구매 이력 (Gemini 파싱 결과를 시뮬레이션)
        RefrigeratorDto.BulkCreateRequest request = RefrigeratorDto.BulkCreateRequest.builder()
                .items(java.util.List.of(
                        createItem("우유", 2, "개"),
                        createItem("계란", 1, "팩"),
                        createItem("사과", 5, "개"),
                        createItem("양파", 3, "개")
                ))
                .build();

        // when
        RefrigeratorDto.BulkCreateResponse result = 
                refrigeratorService.addItemsBulk(testMember.getId(), request);

        // then
        assertThat(result.getSuccessCount()).isEqualTo(4);
        assertThat(result.getFailCount()).isZero();
        assertThat(result.getAddedItems()).hasSize(4);

        // 냉장고 조회로 확인
        RefrigeratorDto.ItemListResponse myItems = 
                refrigeratorService.getMyItems(testMember.getId(), "name");
        assertThat(myItems.getItems()).hasSize(4);
    }

    /**
     * 테스트용 영수증 이미지 준비
     * 환경변수 TEST_RECEIPT_IMAGE_PATH가 있으면 해당 경로 사용 (파일 또는 디렉토리)
     * 없으면 기본 경로 ./test-image 디렉토리에서 첫 번째 이미지 사용
     * 이미지가 없으면 더미 이미지 생성
     */
    private MockMultipartFile prepareTestReceiptImage() throws IOException {
        String imagePath = System.getenv("TEST_RECEIPT_IMAGE_PATH");
        if (imagePath == null || imagePath.isBlank()) {
            imagePath = "./test-image"; // 기본 경로
        }
        
        Path path = Paths.get(imagePath);
        
        // 파일인 경우
        if (Files.exists(path) && Files.isRegularFile(path)) {
            byte[] content = Files.readAllBytes(path);
            String filename = path.getFileName().toString();
            return new MockMultipartFile(
                    "receipt",
                    filename,
                    getContentType(filename),
                    content
            );
        }
        
        // 디렉토리인 경우 - 첫 번째 이미지 파일 찾기
        if (Files.exists(path) && Files.isDirectory(path)) {
            Path imageFile = Files.list(path)
                    .filter(Files::isRegularFile)
                    .filter(p -> {
                        String name = p.getFileName().toString().toLowerCase();
                        return name.endsWith(".jpg") || name.endsWith(".jpeg") || 
                               name.endsWith(".png") || name.endsWith(".gif");
                    })
                    .findFirst()
                    .orElse(null);
            
            if (imageFile != null) {
                byte[] content = Files.readAllBytes(imageFile);
                String filename = imageFile.getFileName().toString();
                return new MockMultipartFile(
                        "receipt",
                        filename,
                        getContentType(filename),
                        content
                );
            }
        }

        // 더미 이미지 (실제 Gemini API 호출 시 실패할 수 있음)
        System.out.println("⚠️ 테스트 이미지를 찾을 수 없어 더미 이미지를 사용합니다. 경로: " + imagePath);
        byte[] dummyImage = "dummy receipt image content".getBytes();
        return new MockMultipartFile(
                "receipt",
                "receipt.jpg",
                "image/jpeg",
                dummyImage
        );
    }
    
    /**
     * 파일 확장자로부터 Content-Type 결정
     */
    private String getContentType(String filename) {
        String lower = filename.toLowerCase();
        if (lower.endsWith(".png")) return "image/png";
        if (lower.endsWith(".gif")) return "image/gif";
        return "image/jpeg"; // jpg, jpeg, 기타
    }

    private RefrigeratorDto.CreateRequest createItem(String name, int quantity, String unit) {
        return RefrigeratorDto.CreateRequest.builder()
                .name(name)
                .quantity(quantity)
                .unit(unit)
                .build();
    }
}
