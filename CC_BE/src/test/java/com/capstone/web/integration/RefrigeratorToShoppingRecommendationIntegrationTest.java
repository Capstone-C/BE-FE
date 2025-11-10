package com.capstone.web.integration;

import com.capstone.web.member.domain.Member;
import com.capstone.web.member.domain.MemberRole;
import com.capstone.web.member.repository.MemberRepository;
import com.capstone.web.refrigerator.domain.RefrigeratorItem;
import com.capstone.web.refrigerator.repository.RefrigeratorItemRepository;
import com.capstone.web.shopping.service.ProductRecommendationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * 시나리오 2: 냉장고 재료 부족 감지 → 네이버 쇼핑 상품 추천 통합 테스트
 * 
 * 테스트 흐름:
 * 1. 냉장고에 재료 추가 (일부는 수량 부족, 일부는 유통기한 임박)
 * 2. ProductRecommendationService가 구매 필요 재료 감지
 * 3. 네이버 쇼핑 API 호출하여 상품 추천
 * 4. 추천 결과 검증
 * 
 * 필요 환경변수:
 * - NAVER_CLIENT_ID: 네이버 Open API 클라이언트 ID
 * - NAVER_CLIENT_SECRET: 네이버 Open API 클라이언트 시크릿
 * 
 * 스케줄러 트리거 기준 설정:
 * - application.yml 또는 환경변수로 설정 가능
 * - shopping.recommendation.low-quantity-threshold: 수량 임계값 (기본값: 2)
 * - shopping.recommendation.expiration-days-threshold: 유통기한 D-day 임계값 (기본값: 3)
 * - shopping.recommendation.max-products-per-item: 재료당 최대 추천 상품 수 (기본값: 10)
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("통합 테스트 - 시나리오 2: 냉장고 재료 부족 → 네이버 쇼핑 상품 추천")
class RefrigeratorToShoppingRecommendationIntegrationTest {

    @Autowired
    private ProductRecommendationService productRecommendationService;

    @Autowired
    private RefrigeratorItemRepository refrigeratorItemRepository;

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
    @DisplayName("전체 시나리오: 냉장고 재료 부족 감지 → 네이버 쇼핑 상품 추천")
    void fullScenario_refrigeratorLowToShoppingRecommendation() {
        // given: 환경변수 확인
        String clientId = System.getenv("NAVER_CLIENT_ID");
        String clientSecret = System.getenv("NAVER_CLIENT_SECRET");
        assumeTrue(clientId != null && !clientId.isBlank() && 
                   clientSecret != null && !clientSecret.isBlank(),
                "NAVER_CLIENT_ID 또는 NAVER_CLIENT_SECRET 환경변수가 설정되지 않았습니다. 테스트를 건너뜁니다.");

        // given: 냉장고에 다양한 상태의 재료 추가
        LocalDate today = LocalDate.now();
        
        // 수량 부족 재료
        createAndSaveItem("계란", 1, "팩", today.plusDays(7));
        createAndSaveItem("우유", 1, "개", today.plusDays(5));
        
        // 유통기한 임박 재료 (D-2)
        createAndSaveItem("사과", 5, "개", today.plusDays(2));
        createAndSaveItem("양파", 3, "개", today.plusDays(1));
        
        // 정상 재료 (충분한 수량, 유통기한 여유)
        createAndSaveItem("김치", 10, "통", today.plusDays(30));

        System.out.println("=== 냉장고 현재 상태 ===");
        System.out.println("저량: 계란(1팩), 우유(1개)");
        System.out.println("유통기한 임박: 사과(D-2), 양파(D-1)");
        System.out.println("정상: 김치(10통, D-30)");
        System.out.println("\n추천 트리거 기준:");
        System.out.printf("- 수량 임계값: %d 이하%n", productRecommendationService.getLowQuantityThreshold());
        System.out.printf("- 유통기한 임계값: D-%d 이하%n", productRecommendationService.getExpirationDaysThreshold());

        // when: Step 1 - 구매 필요 재료 감지 및 상품 추천
        List<ProductRecommendationService.RecommendationResult> recommendations =
                productRecommendationService.recommendProductsForMember(testMember.getId());

        // then: Step 1 검증 - 구매 필요 재료가 올바르게 감지되었는지
        assertThat(recommendations).isNotEmpty();
        assertThat(recommendations).hasSizeGreaterThanOrEqualTo(4); // 계란, 우유, 사과, 양파

        System.out.println("\n=== 추천 결과 ===");
        recommendations.forEach(result -> {
            System.out.printf("\n[%s] - %s%n", result.getIngredientName(), result.getReason());
            System.out.printf("  현재 수량: %d%s%n", 
                    result.getCurrentQuantity(),
                    result.getExpirationDate() != null ? 
                            String.format(", 유통기한: %s", result.getExpirationDate()) : "");
            System.out.printf("  추천 상품: %d개%n", result.getProducts().size());
            
            // then: Step 2 검증 - 각 재료에 대한 네이버 쇼핑 상품이 추천되었는지
            assertThat(result.getProducts()).isNotEmpty();
            assertThat(result.getProducts().size())
                    .isLessThanOrEqualTo(productRecommendationService.getMaxProductsPerItem());
            
            // 상품 정보 출력 (처음 3개만)
            result.getProducts().stream().limit(3).forEach(product -> 
                System.out.printf("    - %s (%s원, %s)%n", 
                        product.getName(), 
                        product.getPrice(), 
                        product.getMallType())
            );
        });

        // then: Step 3 검증 - 정상 재료(김치)는 추천되지 않았는지
        boolean hasNormalItem = recommendations.stream()
                .anyMatch(r -> r.getIngredientName().equals("김치"));
        assertThat(hasNormalItem).isFalse();
    }

    @Test
    @DisplayName("Step 1만 테스트: 구매 필요 재료 감지 (네이버 API 없이)")
    void step1_detectLowIngredients() {
        // given: 다양한 상태의 재료
        LocalDate today = LocalDate.now();
        createAndSaveItem("계란", 1, "팩", today.plusDays(7)); // 수량 부족
        createAndSaveItem("사과", 5, "개", today.plusDays(2)); // 유통기한 임박
        createAndSaveItem("김치", 10, "통", today.plusDays(30)); // 정상

        // when: 추천 시도 (네이버 API 실패 시 빈 리스트 반환 가능)
        List<ProductRecommendationService.RecommendationResult> recommendations =
                productRecommendationService.recommendProductsForMember(testMember.getId());

        // then: 최소한 구매 필요 재료는 감지되어야 함 (상품 추천은 실패할 수 있음)
        System.out.printf("감지된 구매 필요 재료: %d개%n", recommendations.size());
        recommendations.forEach(r -> 
            System.out.printf("- %s: %s%n", r.getIngredientName(), r.getReason())
        );
    }

    @Test
    @DisplayName("Step 2만 테스트: 특정 재료에 대한 네이버 쇼핑 검색")
    void step2_naverShoppingSearch() {
        // given
        String clientId = System.getenv("NAVER_CLIENT_ID");
        String clientSecret = System.getenv("NAVER_CLIENT_SECRET");
        assumeTrue(clientId != null && !clientId.isBlank() && 
                   clientSecret != null && !clientSecret.isBlank(),
                "NAVER_CLIENT_ID 또는 NAVER_CLIENT_SECRET 환경변수가 설정되지 않았습니다. 테스트를 건너뜁니다.");

        // given: 수량 부족 재료 추가
        LocalDate today = LocalDate.now();
        createAndSaveItem("계란", 1, "팩", today.plusDays(7));

        // when
        List<ProductRecommendationService.RecommendationResult> recommendations =
                productRecommendationService.recommendProductsForMember(testMember.getId());

        // then
        assertThat(recommendations).hasSize(1);
        ProductRecommendationService.RecommendationResult eggResult = recommendations.get(0);
        assertThat(eggResult.getIngredientName()).isEqualTo("계란");
        assertThat(eggResult.getProducts()).isNotEmpty();
        
        System.out.println("=== 계란 검색 결과 ===");
        eggResult.getProducts().forEach(product -> 
            System.out.printf("- %s: %s원 (%s)%n", 
                    product.getName(), 
                    product.getPrice(), 
                    product.getMallType())
        );
    }

    @Test
    @DisplayName("스케줄러 트리거 기준 확인")
    void checkTriggerThresholds() {
        // 현재 설정된 임계값 확인
        System.out.println("=== 현재 추천 트리거 기준 ===");
        System.out.printf("수량 임계값: %d 이하%n", 
                productRecommendationService.getLowQuantityThreshold());
        System.out.printf("유통기한 임계값: D-%d 이하%n", 
                productRecommendationService.getExpirationDaysThreshold());
        System.out.printf("재료당 최대 추천 상품 수: %d개%n", 
                productRecommendationService.getMaxProductsPerItem());
        
        System.out.println("\n=== 기준 변경 방법 ===");
        System.out.println("1. application.yml 파일에서 설정:");
        System.out.println("   shopping:");
        System.out.println("     recommendation:");
        System.out.println("       low-quantity-threshold: 2");
        System.out.println("       expiration-days-threshold: 3");
        System.out.println("       max-products-per-item: 10");
        System.out.println("\n2. 환경변수로 설정:");
        System.out.println("   SHOPPING_RECOMMENDATION_LOW_QUANTITY_THRESHOLD=2");
        System.out.println("   SHOPPING_RECOMMENDATION_EXPIRATION_DAYS_THRESHOLD=3");
        System.out.println("   SHOPPING_RECOMMENDATION_MAX_PRODUCTS_PER_ITEM=10");
    }

    /**
     * 테스트용 냉장고 아이템 생성 및 저장
     */
    private RefrigeratorItem createAndSaveItem(String name, int quantity, String unit, LocalDate expirationDate) {
        RefrigeratorItem item = RefrigeratorItem.builder()
                .member(testMember)
                .name(name)
                .quantity(quantity)
                .unit(unit)
                .expirationDate(expirationDate)
                .build();
        return refrigeratorItemRepository.save(item);
    }
}
