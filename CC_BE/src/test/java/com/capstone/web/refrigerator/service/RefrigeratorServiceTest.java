package com.capstone.web.refrigerator.service;

import static org.assertj.core.api.Assertions.*;

import com.capstone.web.member.domain.Member;
import com.capstone.web.member.repository.MemberRepository;
import com.capstone.web.recipe.domain.Recipe;
import com.capstone.web.recipe.domain.RecipeIngredient;
import com.capstone.web.recipe.repository.RecipeRepository;
import com.capstone.web.refrigerator.domain.RefrigeratorItem;
import com.capstone.web.refrigerator.dto.RefrigeratorDto;
import com.capstone.web.refrigerator.dto.RecommendationDto;
import com.capstone.web.refrigerator.dto.DeductionDto;
import com.capstone.web.refrigerator.exception.DuplicateItemException;
import com.capstone.web.refrigerator.exception.ItemNotFoundException;
import com.capstone.web.refrigerator.exception.UnauthorizedItemAccessException;
import com.capstone.web.refrigerator.repository.RefrigeratorItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class RefrigeratorServiceTest {

    @Autowired
    private RefrigeratorService refrigeratorService;
    @Autowired
    private RefrigeratorItemRepository refrigeratorItemRepository;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private RecipeRepository recipeRepository;

    private Member testMember;
    private Member otherMember;

    @BeforeEach
    void setup() {
        refrigeratorItemRepository.deleteAll();
        memberRepository.deleteAll();

        testMember = memberRepository.save(Member.builder()
                .email("test@test.com")
                .password("password")
                .nickname("테스터")
                .build());

        otherMember = memberRepository.save(Member.builder()
                .email("other@test.com")
                .password("password")
                .nickname("타인")
                .build());
    }

    // ========== REF-01: 냉장고 식재료 목록 조회 테스트 ==========

    @DisplayName("식재료 목록 조회 - 빈 목록")
    @Test
    void getMyItems_EmptyList() {
        // when
        RefrigeratorDto.ItemListResponse response =
                refrigeratorService.getMyItems(testMember.getId(), "expirationDate");

        // then
        assertThat(response.getItems()).isEmpty();
        assertThat(response.getTotalCount()).isZero();
        assertThat(response.getExpiringCount()).isZero();
        assertThat(response.getExpiredCount()).isZero();
    }

    @DisplayName("식재료 목록 조회 - 소비기한순 정렬 (null은 마지막)")
    @Test
    void getMyItems_SortByExpirationDate() {
        // given
        refrigeratorItemRepository.save(RefrigeratorItem.builder()
                .member(testMember)
                .name("당근")
                .expirationDate(LocalDate.now().plusDays(10))
                .build());

        refrigeratorItemRepository.save(RefrigeratorItem.builder()
                .member(testMember)
                .name("소금")  // 소비기한 없음
                .build());

        refrigeratorItemRepository.save(RefrigeratorItem.builder()
                .member(testMember)
                .name("우유")
                .expirationDate(LocalDate.now().plusDays(2))
                .build());

        // when
        RefrigeratorDto.ItemListResponse response =
                refrigeratorService.getMyItems(testMember.getId(), "expirationDate");

        // then
        assertThat(response.getItems()).hasSize(3);
        assertThat(response.getItems().get(0).getName()).isEqualTo("우유");
        assertThat(response.getItems().get(1).getName()).isEqualTo("당근");
        assertThat(response.getItems().get(2).getName()).isEqualTo("소금");
    }

    @DisplayName("식재료 목록 조회 - 이름순 정렬")
    @Test
    void getMyItems_SortByName() {
        // given
        refrigeratorItemRepository.save(RefrigeratorItem.builder()
                .member(testMember)
                .name("토마토")
                .build());

        refrigeratorItemRepository.save(RefrigeratorItem.builder()
                .member(testMember)
                .name("계란")
                .build());

        refrigeratorItemRepository.save(RefrigeratorItem.builder()
                .member(testMember)
                .name("사과")
                .build());

        // when
        RefrigeratorDto.ItemListResponse response =
                refrigeratorService.getMyItems(testMember.getId(), "name");

        // then
        assertThat(response.getItems()).hasSize(3);
        assertThat(response.getItems().get(0).getName()).isEqualTo("계란");
        assertThat(response.getItems().get(1).getName()).isEqualTo("사과");
        assertThat(response.getItems().get(2).getName()).isEqualTo("토마토");
    }

    @DisplayName("식재료 목록 조회 - 통계 정보 확인")
    @Test
    void getMyItems_Statistics() {
        // given
        refrigeratorItemRepository.save(RefrigeratorItem.builder()
                .member(testMember)
                .name("임박상품")
                .expirationDate(LocalDate.now().plusDays(2))
                .build());

        refrigeratorItemRepository.save(RefrigeratorItem.builder()
                .member(testMember)
                .name("경과상품")
                .expirationDate(LocalDate.now().minusDays(1))
                .build());

        refrigeratorItemRepository.save(RefrigeratorItem.builder()
                .member(testMember)
                .name("정상상품")
                .expirationDate(LocalDate.now().plusDays(10))
                .build());

        // when
        RefrigeratorDto.ItemListResponse response =
                refrigeratorService.getMyItems(testMember.getId(), "expirationDate");

        // then
        assertThat(response.getTotalCount()).isEqualTo(3);
        assertThat(response.getExpiringCount()).isEqualTo(1);
        assertThat(response.getExpiredCount()).isEqualTo(1);
    }

    @DisplayName("식재료 목록 조회 - 다른 사용자 데이터는 조회되지 않음")
    @Test
    void getMyItems_OnlyOwnItems() {
        // given
        refrigeratorItemRepository.save(RefrigeratorItem.builder()
                .member(testMember)
                .name("내 우유")
                .build());

        refrigeratorItemRepository.save(RefrigeratorItem.builder()
                .member(otherMember)
                .name("타인 우유")
                .build());

        // when
        RefrigeratorDto.ItemListResponse response =
                refrigeratorService.getMyItems(testMember.getId(), "name");

        // then
        assertThat(response.getItems()).hasSize(1);
        assertThat(response.getItems().get(0).getName()).isEqualTo("내 우유");
    }

    // ========== REF-02: 식재료 추가 테스트 ==========

    @DisplayName("식재료 추가 - 성공")
    @Test
    void addItem_Success() {
        // given
        RefrigeratorDto.CreateRequest request = RefrigeratorDto.CreateRequest.builder()
                .name("우유")
                .quantity(1)
                .unit("L")
                .expirationDate(LocalDate.now().plusDays(7))
                .memo("서울우유")
                .build();

        // when
        RefrigeratorDto.Response response = refrigeratorService.addItem(testMember.getId(), request);

        // then
        assertThat(response.getId()).isNotNull();
        assertThat(response.getName()).isEqualTo("우유");
        assertThat(response.getQuantity()).isEqualTo(1);
        assertThat(response.getUnit()).isEqualTo("L");
        assertThat(response.getExpirationDate()).isEqualTo(request.getExpirationDate());
        assertThat(response.getMemo()).isEqualTo("서울우유");
        assertThat(response.getDaysUntilExpiration()).isEqualTo(7);
        assertThat(response.isExpirationSoon()).isFalse();
        assertThat(response.isExpired()).isFalse();
    }

    @DisplayName("식재료 추가 - 중복된 이름으로 추가 시 예외")
    @Test
    void addItem_Fail_Duplicate() {
        // given
        refrigeratorItemRepository.save(RefrigeratorItem.builder()
                .member(testMember)
                .name("우유")
                .build());

        RefrigeratorDto.CreateRequest request = RefrigeratorDto.CreateRequest.builder()
                .name("우유")
                .quantity(2)
                .build();

        // when & then
        assertThatThrownBy(() -> refrigeratorService.addItem(testMember.getId(), request))
                .isInstanceOf(DuplicateItemException.class)
                .hasMessageContaining("이미 등록된 식재료");
    }

    @DisplayName("식재료 추가 - 소비기한 없이 추가 가능")
    @Test
    void addItem_Success_WithoutExpirationDate() {
        // given
        RefrigeratorDto.CreateRequest request = RefrigeratorDto.CreateRequest.builder()
                .name("소금")
                .quantity(500)
                .unit("g")
                .build();

        // when
        RefrigeratorDto.Response response = refrigeratorService.addItem(testMember.getId(), request);

        // then
        assertThat(response.getExpirationDate()).isNull();
        assertThat(response.getDaysUntilExpiration()).isNull();
        assertThat(response.isExpirationSoon()).isFalse();
        assertThat(response.isExpired()).isFalse();
    }

    // ========== REF-03,04: 식재료 일괄 추가 테스트 ==========

    @DisplayName("식재료 일괄 추가 - 모두 성공")
    @Test
    void addItemsBulk_AllSuccess() {
        // given
        List<RefrigeratorDto.CreateRequest> items = List.of(
                RefrigeratorDto.CreateRequest.builder()
                        .name("우유")
                        .quantity(1)
                        .unit("L")
                        .build(),
                RefrigeratorDto.CreateRequest.builder()
                        .name("계란")
                        .quantity(10)
                        .unit("개")
                        .build(),
                RefrigeratorDto.CreateRequest.builder()
                        .name("빵")
                        .quantity(1)
                        .unit("개")
                        .build()
        );

        RefrigeratorDto.BulkCreateRequest request = new RefrigeratorDto.BulkCreateRequest(items);

        // when
        RefrigeratorDto.BulkCreateResponse response =
                refrigeratorService.addItemsBulk(testMember.getId(), request);

        // then
        assertThat(response.getSuccessCount()).isEqualTo(3);
        assertThat(response.getFailCount()).isZero();
        assertThat(response.getAddedItems()).hasSize(3);
        assertThat(response.getFailedItems()).isEmpty();
    }

    @DisplayName("식재료 일괄 추가 - 부분 성공 (중복 건너뛰기)")
    @Test
    void addItemsBulk_PartialSuccess() {
        // given
        refrigeratorItemRepository.save(RefrigeratorItem.builder()
                .member(testMember)
                .name("우유")
                .build());

        List<RefrigeratorDto.CreateRequest> items = List.of(
                RefrigeratorDto.CreateRequest.builder()
                        .name("우유")  // 중복
                        .quantity(1)
                        .build(),
                RefrigeratorDto.CreateRequest.builder()
                        .name("계란")  // 성공
                        .quantity(10)
                        .build(),
                RefrigeratorDto.CreateRequest.builder()
                        .name("빵")  // 성공
                        .quantity(1)
                        .build()
        );

        RefrigeratorDto.BulkCreateRequest request = new RefrigeratorDto.BulkCreateRequest(items);

        // when
        RefrigeratorDto.BulkCreateResponse response =
                refrigeratorService.addItemsBulk(testMember.getId(), request);

        // then
        assertThat(response.getSuccessCount()).isEqualTo(2);
        assertThat(response.getFailCount()).isEqualTo(1);
        assertThat(response.getAddedItems()).hasSize(2);
        assertThat(response.getFailedItems()).hasSize(1);
        assertThat(response.getFailedItems().get(0)).contains("우유");
    }

    @DisplayName("식재료 일괄 추가 - 빈 목록")
    @Test
    void addItemsBulk_EmptyList() {
        // given
        RefrigeratorDto.BulkCreateRequest request =
                new RefrigeratorDto.BulkCreateRequest(List.of());

        // when
        RefrigeratorDto.BulkCreateResponse response =
                refrigeratorService.addItemsBulk(testMember.getId(), request);

        // then
        assertThat(response.getSuccessCount()).isZero();
        assertThat(response.getFailCount()).isZero();
        assertThat(response.getAddedItems()).isEmpty();
        assertThat(response.getFailedItems()).isEmpty();
    }

    // ========== REF-05: 식재료 수정 테스트 ==========

    @DisplayName("식재료 수정 - 성공")
    @Test
    void updateItem_Success() {
        // given
        RefrigeratorItem saved = refrigeratorItemRepository.save(RefrigeratorItem.builder()
                .member(testMember)
                .name("우유")
                .quantity(1)
                .unit("L")
                .expirationDate(LocalDate.now().plusDays(7))
                .build());

        RefrigeratorDto.UpdateRequest request = RefrigeratorDto.UpdateRequest.builder()
                .quantity(2)
                .unit("L")
                .expirationDate(LocalDate.now().plusDays(10))
                .memo("추가 구매")
                .build();

        // when
        RefrigeratorDto.Response response =
                refrigeratorService.updateItem(testMember.getId(), saved.getId(), request);

        // then
        assertThat(response.getName()).isEqualTo("우유");  // 이름은 불변
        assertThat(response.getQuantity()).isEqualTo(2);
        assertThat(response.getUnit()).isEqualTo("L");
        assertThat(response.getExpirationDate()).isEqualTo(request.getExpirationDate());
        assertThat(response.getMemo()).isEqualTo("추가 구매");
    }

    @DisplayName("식재료 수정 - 존재하지 않는 ID로 수정 시 예외")
    @Test
    void updateItem_Fail_NotFound() {
        // given
        Long nonExistentId = 999L;

        RefrigeratorDto.UpdateRequest request = RefrigeratorDto.UpdateRequest.builder()
                .quantity(2)
                .build();

        // when & then
        assertThatThrownBy(() -> refrigeratorService.updateItem(testMember.getId(), nonExistentId, request))
                .isInstanceOf(ItemNotFoundException.class);
    }

    @DisplayName("식재료 수정 - 권한 없는 사용자가 수정 시 예외")
    @Test
    void updateItem_Fail_Unauthorized() {
        // given
        RefrigeratorItem saved = refrigeratorItemRepository.save(RefrigeratorItem.builder()
                .member(testMember)
                .name("우유")
                .build());

        RefrigeratorDto.UpdateRequest request = RefrigeratorDto.UpdateRequest.builder()
                .quantity(2)
                .build();

        // when & then
        assertThatThrownBy(() -> refrigeratorService.updateItem(otherMember.getId(), saved.getId(), request))
                .isInstanceOf(UnauthorizedItemAccessException.class)
                .hasMessageContaining("권한이 없습니다");
    }

    // ========== REF-06: 식재료 삭제 테스트 ==========

    @DisplayName("식재료 삭제 - 성공")
    @Test
    void deleteItem_Success() {
        // given
        RefrigeratorItem saved = refrigeratorItemRepository.save(RefrigeratorItem.builder()
                .member(testMember)
                .name("우유")
                .build());

        // when
        refrigeratorService.deleteItem(testMember.getId(), saved.getId());

        // then
        assertThat(refrigeratorItemRepository.existsById(saved.getId())).isFalse();
    }

    @DisplayName("식재료 삭제 - 존재하지 않는 ID로 삭제 시 예외")
    @Test
    void deleteItem_Fail_NotFound() {
        // given
        Long nonExistentId = 999L;

        // when & then
        assertThatThrownBy(() -> refrigeratorService.deleteItem(testMember.getId(), nonExistentId))
                .isInstanceOf(ItemNotFoundException.class);
    }

    @DisplayName("식재료 삭제 - 권한 없는 사용자가 삭제 시 예외")
    @Test
    void deleteItem_Fail_Unauthorized() {
        // given
        RefrigeratorItem saved = refrigeratorItemRepository.save(RefrigeratorItem.builder()
                .member(testMember)
                .name("우유")
                .build());

        // when & then
        assertThatThrownBy(() -> refrigeratorService.deleteItem(otherMember.getId(), saved.getId()))
                .isInstanceOf(UnauthorizedItemAccessException.class)
                .hasMessageContaining("권한이 없습니다");
    }

    // ========== REF-07: 레시피 추천 기능 테스트 ==========

    @DisplayName("REF-07: 보유 재료가 많을수록 매칭률이 높다")
    @Test
    void getRecommendations_MatchRate() {
        // given: 냉장고에 김치, 돼지고기, 두부 보유
        refrigeratorItemRepository.save(RefrigeratorItem.builder()
                .member(testMember)
                .name("김치")
                .quantity(1)
                .build());
        refrigeratorItemRepository.save(RefrigeratorItem.builder()
                .member(testMember)
                .name("돼지고기")
                .quantity(1)
                .build());
        refrigeratorItemRepository.save(RefrigeratorItem.builder()
                .member(testMember)
                .name("두부")
                .quantity(1)
                .build());

        // 레시피 1: 김치찌개 (김치, 돼지고기, 두부 필요) - 100% 매칭
        Recipe recipe1 = Recipe.builder()
                .name("김치찌개")
                .cookTime(30)
                .servings(2)
                .difficulty(Recipe.Difficulty.EASY)
                .build();
        recipe1.addIngredient(RecipeIngredient.builder().name("김치").amount("200g").build());
        recipe1.addIngredient(RecipeIngredient.builder().name("돼지고기").amount("100g").build());
        recipe1.addIngredient(RecipeIngredient.builder().name("두부").amount("1모").build());
        recipeRepository.save(recipe1);

        // 레시피 2: 김치볶음밥 (김치, 밥, 참기름 필요) - 33% 매칭
        Recipe recipe2 = Recipe.builder()
                .name("김치볶음밥")
                .cookTime(15)
                .servings(1)
                .difficulty(Recipe.Difficulty.EASY)
                .build();
        recipe2.addIngredient(RecipeIngredient.builder().name("김치").amount("100g").build());
        recipe2.addIngredient(RecipeIngredient.builder().name("밥").amount("1공기").build());
        recipe2.addIngredient(RecipeIngredient.builder().name("참기름").amount("1스푼").build());
        recipeRepository.save(recipe2);

        // when
        RecommendationDto.RecommendationResponse response =
                refrigeratorService.getRecommendations(testMember.getId(), 10);

        // then
        assertThat(response.getRecommendations()).hasSize(2);

        RecommendationDto.RecommendedRecipe first = response.getRecommendations().get(0);
        assertThat(first.getRecipeName()).isEqualTo("김치찌개");
        assertThat(first.getMatchRate()).isEqualTo(100.0);
        assertThat(first.getMatchedIngredients()).containsExactlyInAnyOrder("김치", "돼지고기", "두부");
        assertThat(first.getMissingIngredients()).isEmpty();

        RecommendationDto.RecommendedRecipe second = response.getRecommendations().get(1);
        assertThat(second.getRecipeName()).isEqualTo("김치볶음밥");
        assertThat(second.getMatchRate()).isCloseTo(33.3, within(0.1));
        assertThat(second.getMatchedIngredients()).containsExactly("김치");
        assertThat(second.getMissingIngredients()).hasSize(2);
    }

    @DisplayName("REF-07: Fuzzy matching으로 유사 재료도 매칭된다")
    @Test
    void getRecommendations_FuzzyMatching() {
        // given: 냉장고에 "양파" 보유
        refrigeratorItemRepository.save(RefrigeratorItem.builder()
                .member(testMember)
                .name("양파")
                .quantity(1)
                .build());

        // 레시피: "양파즙" 필요 (contains 매칭 되어야 함)
        Recipe recipe = Recipe.builder()
                .name("양파요리")
                .build();
        recipe.addIngredient(RecipeIngredient.builder().name("양파즙").build());
        recipeRepository.save(recipe);

        // when
        RecommendationDto.RecommendationResponse response =
                refrigeratorService.getRecommendations(testMember.getId(), 10);

        // then
        assertThat(response.getRecommendations()).hasSize(1);
        assertThat(response.getRecommendations().get(0).getMatchRate()).isEqualTo(100.0);
        assertThat(response.getRecommendations().get(0).getMatchedIngredients()).containsExactly("양파즙");
    }

    @DisplayName("REF-07: limit 파라미터로 결과 개수 제한")
    @Test
    void getRecommendations_LimitWorks() {
        // given: 5개 레시피 생성
        for (int i = 1; i <= 5; i++) {
            Recipe recipe = Recipe.builder().name("레시피" + i).build();
            recipe.addIngredient(RecipeIngredient.builder().name("재료" + i).build());
            recipeRepository.save(recipe);
        }

        refrigeratorItemRepository.save(RefrigeratorItem.builder()
                .member(testMember)
                .name("재료1")
                .build());

        // when: limit=3
        RecommendationDto.RecommendationResponse response =
                refrigeratorService.getRecommendations(testMember.getId(), 3);

        // then: 최대 3개만 반환
        assertThat(response.getRecommendations()).hasSizeLessThanOrEqualTo(3);
    }

    @DisplayName("REF-07: 매칭률 0인 레시피는 제외된다")
    @Test
    void getRecommendations_ExcludeZeroMatch() {
        // given: 냉장고에 김치만 보유
        refrigeratorItemRepository.save(RefrigeratorItem.builder()
                .member(testMember)
                .name("김치")
                .build());

        // 레시피 1: 김치 포함
        Recipe recipe1 = Recipe.builder().name("김치찌개").build();
        recipe1.addIngredient(RecipeIngredient.builder().name("김치").build());
        recipeRepository.save(recipe1);

        // 레시피 2: 김치 불포함
        Recipe recipe2 = Recipe.builder().name("된장찌개").build();
        recipe2.addIngredient(RecipeIngredient.builder().name("된장").build());
        recipeRepository.save(recipe2);

        // when
        RecommendationDto.RecommendationResponse response =
                refrigeratorService.getRecommendations(testMember.getId(), 10);

        // then: 매칭률 0인 레시피는 제외
        assertThat(response.getRecommendations()).hasSize(1);
        assertThat(response.getRecommendations().get(0).getRecipeName()).isEqualTo("김치찌개");
    }

    // ========== REF-08: 재료 자동 차감 기능 테스트 ==========

    @DisplayName("REF-08: 재료 차감 미리보기 - 모든 재료 충분")
    @Test
    void previewDeduction_AllOK() {
        // given: 냉장고에 충분한 재료
        refrigeratorItemRepository.save(RefrigeratorItem.builder()
                .member(testMember)
                .name("김치")
                .quantity(5)
                .build());
        refrigeratorItemRepository.save(RefrigeratorItem.builder()
                .member(testMember)
                .name("돼지고기")
                .quantity(3)
                .build());

        Recipe recipe = Recipe.builder().name("김치찌개").build();
        recipe.addIngredient(RecipeIngredient.builder().name("김치").isRequired(true).build());
        recipe.addIngredient(RecipeIngredient.builder().name("돼지고기").isRequired(true).build());
        Recipe saved = recipeRepository.save(recipe);

        // when
        DeductionDto.DeductPreviewResponse response =
                refrigeratorService.previewDeduction(testMember.getId(), saved.getId());

        // then
        assertThat(response.isCanProceed()).isTrue();
        assertThat(response.getWarnings()).isEmpty();
        assertThat(response.getIngredients()).allMatch(
                ing -> ing.getStatus() == DeductionDto.DeductionStatus.OK
        );
    }

    @DisplayName("REF-08: 재료 차감 미리보기 - 필수 재료 부족")
    @Test
    void previewDeduction_RequiredInsufficient() {
        // given: 김치 수량 0
        refrigeratorItemRepository.save(RefrigeratorItem.builder()
                .member(testMember)
                .name("김치")
                .quantity(0)  // 부족
                .build());

        Recipe recipe = Recipe.builder().name("김치찌개").build();
        recipe.addIngredient(RecipeIngredient.builder().name("김치").isRequired(true).build());
        Recipe saved = recipeRepository.save(recipe);

        // when
        DeductionDto.DeductPreviewResponse response =
                refrigeratorService.previewDeduction(testMember.getId(), saved.getId());

        // then
        assertThat(response.isCanProceed()).isFalse();
        assertThat(response.getWarnings()).isNotEmpty();
        assertThat(response.getIngredients().get(0).getStatus())
                .isEqualTo(DeductionDto.DeductionStatus.INSUFFICIENT);
    }

    @DisplayName("REF-08: 재료 차감 미리보기 - 재료 없음")
    @Test
    void previewDeduction_NotFound() {
        // given: 냉장고에 재료 없음
        Recipe recipe = Recipe.builder().name("김치찌개").build();
        recipe.addIngredient(RecipeIngredient.builder().name("김치").isRequired(true).build());
        Recipe saved = recipeRepository.save(recipe);

        // when
        DeductionDto.DeductPreviewResponse response =
                refrigeratorService.previewDeduction(testMember.getId(), saved.getId());

        // then
        assertThat(response.isCanProceed()).isFalse();
        assertThat(response.getIngredients().get(0).getStatus())
                .isEqualTo(DeductionDto.DeductionStatus.NOT_FOUND);
    }

    @DisplayName("REF-08: 재료 차감 실행 - 정상 차감")
    @Test
    void deductIngredients_Success() {
        // given
        RefrigeratorItem item1 = refrigeratorItemRepository.save(RefrigeratorItem.builder()
                .member(testMember)
                .name("김치")
                .quantity(5)
                .build());
        RefrigeratorItem item2 = refrigeratorItemRepository.save(RefrigeratorItem.builder()
                .member(testMember)
                .name("돼지고기")
                .quantity(3)
                .build());

        Recipe recipe = Recipe.builder().name("김치찌개").build();
        recipe.addIngredient(RecipeIngredient.builder().name("김치").build());
        recipe.addIngredient(RecipeIngredient.builder().name("돼지고기").build());
        Recipe saved = recipeRepository.save(recipe);

        DeductionDto.DeductRequest request = DeductionDto.DeductRequest.builder()
                .recipeId(saved.getId())
                .ignoreWarnings(false)
                .build();

        // when
        DeductionDto.DeductResponse response =
                refrigeratorService.deductIngredients(testMember.getId(), request);

        // then
        assertThat(response.getSuccessCount()).isEqualTo(2);
        assertThat(response.getFailedCount()).isZero();

        // 실제 수량 감소 확인
        RefrigeratorItem updatedItem1 = refrigeratorItemRepository.findById(item1.getId()).get();
        RefrigeratorItem updatedItem2 = refrigeratorItemRepository.findById(item2.getId()).get();
        assertThat(updatedItem1.getQuantity()).isEqualTo(4); // 5 - 1
        assertThat(updatedItem2.getQuantity()).isEqualTo(2); // 3 - 1
    }

    @DisplayName("REF-08: 재료 차감 실행 - 필수 재료 부족 시 예외")
    @Test
    void deductIngredients_ThrowsWhenRequiredInsufficient() {
        // given: 김치 부족
        refrigeratorItemRepository.save(RefrigeratorItem.builder()
                .member(testMember)
                .name("김치")
                .quantity(0)
                .build());

        Recipe recipe = Recipe.builder().name("김치찌개").build();
        recipe.addIngredient(RecipeIngredient.builder().name("김치").isRequired(true).build());
        Recipe saved = recipeRepository.save(recipe);

        DeductionDto.DeductRequest request = DeductionDto.DeductRequest.builder()
                .recipeId(saved.getId())
                .ignoreWarnings(false)  // 경고 무시 안 함
                .build();

        // when & then
        assertThatThrownBy(() ->
                refrigeratorService.deductIngredients(testMember.getId(), request)
        )
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("필수 재료가 부족합니다");
    }

    @DisplayName("REF-08: 재료 차감 실행 - ignoreWarnings=true면 강제 실행")
    @Test
    void deductIngredients_IgnoreWarnings() {
        // given: 김치 부족하지만 강제 실행
        refrigeratorItemRepository.save(RefrigeratorItem.builder()
                .member(testMember)
                .name("김치")
                .quantity(0)
                .build());

        Recipe recipe = Recipe.builder().name("김치찌개").build();
        recipe.addIngredient(RecipeIngredient.builder().name("김치").isRequired(true).build());
        Recipe saved = recipeRepository.save(recipe);

        DeductionDto.DeductRequest request = DeductionDto.DeductRequest.builder()
                .recipeId(saved.getId())
                .ignoreWarnings(true)  // 경고 무시
                .build();

        // when
        DeductionDto.DeductResponse response =
                refrigeratorService.deductIngredients(testMember.getId(), request);

        // then: 예외 발생 안 하고 실패로 기록
        assertThat(response.getFailedCount()).isEqualTo(1);
        assertThat(response.getSuccessCount()).isZero();
    }

    @DisplayName("REF-08: 수량은 최소 0까지만 감소한다")
    @Test
    void deductIngredients_MinimumZero() {
        // given: 수량 1
        RefrigeratorItem item = refrigeratorItemRepository.save(RefrigeratorItem.builder()
                .member(testMember)
                .name("김치")
                .quantity(1)
                .build());

        Recipe recipe = Recipe.builder().name("김치찌개").build();
        recipe.addIngredient(RecipeIngredient.builder().name("김치").build());
        Recipe saved = recipeRepository.save(recipe);

        DeductionDto.DeductRequest request = DeductionDto.DeductRequest.builder()
                .recipeId(saved.getId())
                .build();

        // when
        refrigeratorService.deductIngredients(testMember.getId(), request);

        // then: 1 - 1 = 0 (음수 안 됨)
        RefrigeratorItem updated = refrigeratorItemRepository.findById(item.getId()).get();
        assertThat(updated.getQuantity()).isEqualTo(0);
    }
}
