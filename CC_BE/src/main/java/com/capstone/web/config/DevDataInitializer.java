package com.capstone.web.config;

import com.capstone.web.category.domain.Category;
import com.capstone.web.category.repository.CategoryRepository;
import com.capstone.web.diary.domain.Diary;
import com.capstone.web.diary.repository.DiaryRepository;
import com.capstone.web.member.domain.Member;
import com.capstone.web.member.repository.MemberRepository;
import com.capstone.web.refrigerator.domain.RefrigeratorItem;
import com.capstone.web.refrigerator.repository.RefrigeratorItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;

/**
 * 개발/테스트용 초기 데이터 생성기
 * 
 * <p>이 클래스는 'dev' 프로파일에서만 실행되며, 
 * 프론트엔드 개발 및 테스트를 위한 샘플 데이터를 자동으로 생성합니다.</p>
 * 
 * <h3>생성되는 데이터</h3>
 * <ul>
 *   <li>테스트 회원 3명 (test1@test.com, test2@test.com, admin@test.com)</li>
 *   <li>카테고리 (채식, 육식, 레시피 등)</li>
 *   <li>냉장고 식재료 샘플</li>
 *   <li>다이어리 식단 기록 샘플</li>
 * </ul>
 * 
 * <h3>사용법</h3>
 * <pre>
 * # application-dev.yml 또는 실행 시 프로파일 지정
 * spring.profiles.active=dev
 * 
 * # 또는 IntelliJ 실행 설정에서
 * Active profiles: dev
 * </pre>
 * 
 * @see DevDataInfo 초기화된 데이터 정보 문서
 */
@Slf4j
@Configuration
@Profile("dev") // dev 프로파일에서만 실행
@RequiredArgsConstructor
public class DevDataInitializer {

    private final MemberRepository memberRepository;
    private final CategoryRepository categoryRepository;
    private final RefrigeratorItemRepository refrigeratorItemRepository;
    private final DiaryRepository diaryRepository;
    private final PasswordEncoder passwordEncoder;

    @Bean
    public CommandLineRunner initDevData() {
        return args -> {
            log.info("========================================");
            log.info("개발용 초기 데이터 생성 시작");
            log.info("========================================");

            try {
                // 데이터가 이미 있으면 생성하지 않음
                if (memberRepository.count() > 0) {
                    log.info("이미 데이터가 존재합니다. 초기화를 건너뜁니다.");
                    return;
                }

                // 1. 회원 생성
                Member testUser1 = createTestMember("test1@test.com", "김철수", "Test1234!");
                createTestMember("test2@test.com", "이영희", "Test1234!");
                createTestMember("admin@test.com", "관리자", "Admin1234!");
                log.info("✓ 테스트 회원 3명 생성 완료");

                // 2. 카테고리 생성
                createCategories();
                log.info("✓ 카테고리 생성 완료");

                // 3. 냉장고 식재료 생성
                createRefrigeratorItems(testUser1);
                log.info("✓ 냉장고 식재료 생성 완료 (test1@test.com)");

                // 4. 다이어리 식단 기록 생성
                createDiaryEntries(testUser1);
                log.info("✓ 다이어리 식단 기록 생성 완료 (test1@test.com)");

                log.info("========================================");
                log.info("개발용 초기 데이터 생성 완료!");
                log.info("========================================");
                log.info("테스트 계정 정보:");
                log.info("  - 일반 사용자 1: test1@test.com / Test1234!");
                log.info("  - 일반 사용자 2: test2@test.com / Test1234!");
                log.info("  - 관리자: admin@test.com / Admin1234!");
                log.info("========================================");

            } catch (Exception e) {
                log.error("초기 데이터 생성 중 오류 발생", e);
            }
        };
    }

    /**
     * 테스트 회원 생성
     */
    private Member createTestMember(String email, String nickname, String password) {
        Member member = Member.builder()
                .email(email)
                .password(passwordEncoder.encode(password))
                .nickname(nickname)
                .build();
        return memberRepository.save(member);
    }

    /**
     * 카테고리 생성
     */
    private void createCategories() {
        // 채식 카테고리
        Category vegan = Category.builder()
                .name("채식")
                .type(Category.CategoryType.VEGAN)
                .build();
        categoryRepository.save(vegan);

        Category veganSub1 = Category.builder()
                .name("샐러드")
                .type(Category.CategoryType.VEGAN)
                .parent(vegan)
                .build();
        categoryRepository.save(veganSub1);

        Category veganSub2 = Category.builder()
                .name("과일")
                .type(Category.CategoryType.VEGAN)
                .parent(vegan)
                .build();
        categoryRepository.save(veganSub2);

        // 육식 카테고리
        Category carnivore = Category.builder()
                .name("육식")
                .type(Category.CategoryType.CARNIVORE)
                .build();
        categoryRepository.save(carnivore);

        Category carnivoreSub1 = Category.builder()
                .name("소고기")
                .type(Category.CategoryType.CARNIVORE)
                .parent(carnivore)
                .build();
        categoryRepository.save(carnivoreSub1);

        Category carnivoreSub2 = Category.builder()
                .name("닭고기")
                .type(Category.CategoryType.CARNIVORE)
                .parent(carnivore)
                .build();
        categoryRepository.save(carnivoreSub2);

        // 레시피 카테고리
        Category recipe = Category.builder()
                .name("레시피")
                .type(Category.CategoryType.RECIPE)
                .build();
        categoryRepository.save(recipe);

        // 자유게시판 카테고리
        Category free = Category.builder()
                .name("자유게시판")
                .type(Category.CategoryType.FREE)
                .build();
        categoryRepository.save(free);

        // Q&A 카테고리
        Category qa = Category.builder()
                .name("질문과답변")
                .type(Category.CategoryType.QA)
                .build();
        categoryRepository.save(qa);
    }

    /**
     * 냉장고 식재료 생성
     */
    private void createRefrigeratorItems(Member member) {
        // 소비기한 임박 식재료
        refrigeratorItemRepository.save(RefrigeratorItem.builder()
                .member(member)
                .name("우유")
                .quantity(1)
                .unit("개")
                .expirationDate(LocalDate.now().plusDays(2))
                .memo("개봉 후 3일 이내 섭취")
                .build());

        refrigeratorItemRepository.save(RefrigeratorItem.builder()
                .member(member)
                .name("요구르트")
                .quantity(4)
                .unit("개")
                .expirationDate(LocalDate.now().plusDays(3))
                .memo("딸기맛")
                .build());

        // 정상 소비기한 식재료
        refrigeratorItemRepository.save(RefrigeratorItem.builder()
                .member(member)
                .name("계란")
                .quantity(10)
                .unit("개")
                .expirationDate(LocalDate.now().plusDays(14))
                .build());

        refrigeratorItemRepository.save(RefrigeratorItem.builder()
                .member(member)
                .name("당근")
                .quantity(3)
                .unit("개")
                .expirationDate(LocalDate.now().plusDays(7))
                .build());

        refrigeratorItemRepository.save(RefrigeratorItem.builder()
                .member(member)
                .name("양파")
                .quantity(5)
                .unit("개")
                .expirationDate(LocalDate.now().plusDays(30))
                .build());

        refrigeratorItemRepository.save(RefrigeratorItem.builder()
                .member(member)
                .name("두부")
                .quantity(1)
                .unit("모")
                .expirationDate(LocalDate.now().plusDays(5))
                .memo("찌개용")
                .build());

        refrigeratorItemRepository.save(RefrigeratorItem.builder()
                .member(member)
                .name("고구마")
                .quantity(4)
                .unit("개")
                .expirationDate(LocalDate.now().plusDays(20))
                .build());

        // 소비기한 없는 식재료
        refrigeratorItemRepository.save(RefrigeratorItem.builder()
                .member(member)
                .name("쌀")
                .quantity(5)
                .unit("kg")
                .expirationDate(null)
                .memo("2024년산 햅쌀")
                .build());

        refrigeratorItemRepository.save(RefrigeratorItem.builder()
                .member(member)
                .name("간장")
                .quantity(1)
                .unit("병")
                .expirationDate(null)
                .build());

        refrigeratorItemRepository.save(RefrigeratorItem.builder()
                .member(member)
                .name("참기름")
                .quantity(1)
                .unit("병")
                .expirationDate(null)
                .build());
    }

    /**
     * 다이어리 식단 기록 생성
     */
    private void createDiaryEntries(Member member) {
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);

        // 오늘 식단
        diaryRepository.save(Diary.builder()
                .member(member)
                .date(today)
                .mealType(Diary.MealType.BREAKFAST)
                .content("계란후라이 2개, 토스트 2장, 우유 1잔")
                .imageUrl(null)
                .recipeId(null)
                .build());

        diaryRepository.save(Diary.builder()
                .member(member)
                .date(today)
                .mealType(Diary.MealType.LUNCH)
                .content("김치찌개, 밥, 계란말이")
                .imageUrl(null)
                .recipeId(null)
                .build());

        // 어제 식단
        diaryRepository.save(Diary.builder()
                .member(member)
                .date(yesterday)
                .mealType(Diary.MealType.BREAKFAST)
                .content("시리얼, 바나나 1개")
                .imageUrl(null)
                .recipeId(null)
                .build());

        diaryRepository.save(Diary.builder()
                .member(member)
                .date(yesterday)
                .mealType(Diary.MealType.LUNCH)
                .content("된장찌개, 밥, 김치")
                .imageUrl(null)
                .recipeId(null)
                .build());

        diaryRepository.save(Diary.builder()
                .member(member)
                .date(yesterday)
                .mealType(Diary.MealType.DINNER)
                .content("삼겹살구이, 상추쌈, 소주 2병")
                .imageUrl(null)
                .recipeId(null)
                .build());

        diaryRepository.save(Diary.builder()
                .member(member)
                .date(yesterday)
                .mealType(Diary.MealType.SNACK)
                .content("아이스크림 1개")
                .imageUrl(null)
                .recipeId(null)
                .build());

        // 2일 전 식단
        LocalDate twoDaysAgo = today.minusDays(2);
        diaryRepository.save(Diary.builder()
                .member(member)
                .date(twoDaysAgo)
                .mealType(Diary.MealType.LUNCH)
                .content("햄버거 세트")
                .imageUrl(null)
                .recipeId(null)
                .build());
    }
}
