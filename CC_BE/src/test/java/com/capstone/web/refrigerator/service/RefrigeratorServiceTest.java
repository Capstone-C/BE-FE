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
        // 데이터 초기화 순서 중요 (참조 관계 고려)
        refrigeratorItemRepository.deleteAll();
        recipeRepository.deleteAll();
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
        RefrigeratorDto.ItemListResponse response =
                refrigeratorService.getMyItems(testMember.getId(), "expirationDate");

        assertThat(response.getItems()).isEmpty();
        assertThat(response.getTotalCount()).isZero();
    }

    @DisplayName("식재료 목록 조회 - 소비기한순 정렬")
    @Test
    void getMyItems_SortByExpirationDate() {
        refrigeratorItemRepository.save(RefrigeratorItem.builder().member(testMember).name("당근").expirationDate(LocalDate.now().plusDays(10)).build());
        refrigeratorItemRepository.save(RefrigeratorItem.builder().member(testMember).name("소금").build());
        refrigeratorItemRepository.save(RefrigeratorItem.builder().member(testMember).name("우유").expirationDate(LocalDate.now().plusDays(2)).build());

        RefrigeratorDto.ItemListResponse response =
                refrigeratorService.getMyItems(testMember.getId(), "expirationDate");

        assertThat(response.getItems()).hasSize(3);
        assertThat(response.getItems().get(0).getName()).isEqualTo("우유");
        assertThat(response.getItems().get(1).getName()).isEqualTo("당근");
        assertThat(response.getItems().get(2).getName()).isEqualTo("소금");
    }

    @DisplayName("식재료 목록 조회 - 이름순 정렬")
    @Test
    void getMyItems_SortByName() {
        refrigeratorItemRepository.save(RefrigeratorItem.builder().member(testMember).name("토마토").build());
        refrigeratorItemRepository.save(RefrigeratorItem.builder().member(testMember).name("계란").build());
        refrigeratorItemRepository.save(RefrigeratorItem.builder().member(testMember).name("사과").build());

        RefrigeratorDto.ItemListResponse response =
                refrigeratorService.getMyItems(testMember.getId(), "name");

        assertThat(response.getItems()).hasSize(3);
        assertThat(response.getItems().get(0).getName()).isEqualTo("계란");
        assertThat(response.getItems().get(1).getName()).isEqualTo("사과");
        assertThat(response.getItems().get(2).getName()).isEqualTo("토마토");
    }

    @DisplayName("식재료 목록 조회 - 통계 정보 확인")
    @Test
    void getMyItems_Statistics() {
        refrigeratorItemRepository.save(RefrigeratorItem.builder().member(testMember).name("임박상품").expirationDate(LocalDate.now().plusDays(2)).build());
        refrigeratorItemRepository.save(RefrigeratorItem.builder().member(testMember).name("경과상품").expirationDate(LocalDate.now().minusDays(1)).build());
        refrigeratorItemRepository.save(RefrigeratorItem.builder().member(testMember).name("정상상품").expirationDate(LocalDate.now().plusDays(10)).build());

        RefrigeratorDto.ItemListResponse response =
                refrigeratorService.getMyItems(testMember.getId(), "expirationDate");

        assertThat(response.getTotalCount()).isEqualTo(3);
        assertThat(response.getExpiringCount()).isEqualTo(1);
        assertThat(response.getExpiredCount()).isEqualTo(1);
    }

    @DisplayName("식재료 목록 조회 - 다른 사용자 데이터는 조회되지 않음")
    @Test
    void getMyItems_OnlyOwnItems() {
        refrigeratorItemRepository.save(RefrigeratorItem.builder().member(testMember).name("내 우유").build());
        refrigeratorItemRepository.save(RefrigeratorItem.builder().member(otherMember).name("타인 우유").build());

        RefrigeratorDto.ItemListResponse response =
                refrigeratorService.getMyItems(testMember.getId(), "name");

        assertThat(response.getItems()).hasSize(1);
        assertThat(response.getItems().get(0).getName()).isEqualTo("내 우유");
    }

    // ========== REF-02: 식재료 추가 테스트 ==========

    @DisplayName("식재료 추가 - 성공")
    @Test
    void addItem_Success() {
        RefrigeratorDto.CreateRequest request = RefrigeratorDto.CreateRequest.builder()
                .name("우유").quantity(1).unit("L").expirationDate(LocalDate.now().plusDays(7)).memo("서울우유").build();

        RefrigeratorDto.Response response = refrigeratorService.addItem(testMember.getId(), request);

        assertThat(response.getId()).isNotNull();
        assertThat(response.getName()).isEqualTo("우유");
        assertThat(response.getQuantity()).isEqualTo(1);
    }

    @DisplayName("식재료 추가 - 동일 이름+동일 소비기한 시 수량 합산")
    @Test
    void addItem_MergeQuantity_SameNameAndExpiration() {
        LocalDate expDate = LocalDate.now().plusDays(7);
        refrigeratorItemRepository.save(RefrigeratorItem.builder().member(testMember).name("우유").quantity(2).expirationDate(expDate).build());

        RefrigeratorDto.CreateRequest request = RefrigeratorDto.CreateRequest.builder()
                .name("우유").quantity(3).expirationDate(expDate).build();

        RefrigeratorDto.Response response = refrigeratorService.addItem(testMember.getId(), request);

        assertThat(response.getName()).isEqualTo("우유");
        assertThat(response.getQuantity()).isEqualTo(5);
    }

    @DisplayName("식재료 추가 - 동일 이름+다른 소비기한 시 별도 항목 생성")
    @Test
    void addItem_CreateSeparate_SameNameDifferentExpiration() {
        LocalDate expDate1 = LocalDate.now().plusDays(7);
        LocalDate expDate2 = LocalDate.now().plusDays(14);
        refrigeratorItemRepository.save(RefrigeratorItem.builder().member(testMember).name("우유").quantity(2).expirationDate(expDate1).build());

        RefrigeratorDto.CreateRequest request = RefrigeratorDto.CreateRequest.builder()
                .name("우유").quantity(3).expirationDate(expDate2).build();

        RefrigeratorDto.Response response = refrigeratorService.addItem(testMember.getId(), request);

        assertThat(response.getName()).isEqualTo("우유");
        assertThat(response.getQuantity()).isEqualTo(3);
        assertThat(response.getExpirationDate()).isEqualTo(expDate2);

        List<RefrigeratorItem> items = refrigeratorItemRepository.findByMemberOrderByNameAsc(testMember);
        assertThat(items).hasSize(2);
    }

    @DisplayName("식재료 추가 - 소비기한 null인 경우 수량 합산")
    @Test
    void addItem_MergeQuantity_BothExpirationNull() {
        refrigeratorItemRepository.save(RefrigeratorItem.builder().member(testMember).name("소금").quantity(100).unit("g").expirationDate(null).build());

        RefrigeratorDto.CreateRequest request = RefrigeratorDto.CreateRequest.builder()
                .name("소금").quantity(50).unit("g").expirationDate(null).build();

        RefrigeratorDto.Response response = refrigeratorService.addItem(testMember.getId(), request);

        assertThat(response.getName()).isEqualTo("소금");
        assertThat(response.getQuantity()).isEqualTo(150);
    }

    @DisplayName("식재료 추가 - 동일 이름이지만 한쪽만 소비기한 null이면 별도 항목")
    @Test
    void addItem_CreateSeparate_OneExpirationNull() {
        refrigeratorItemRepository.save(RefrigeratorItem.builder().member(testMember).name("설탕").quantity(100).expirationDate(null).build());

        RefrigeratorDto.CreateRequest request = RefrigeratorDto.CreateRequest.builder()
                .name("설탕").quantity(50).expirationDate(LocalDate.now().plusDays(30)).build();

        RefrigeratorDto.Response response = refrigeratorService.addItem(testMember.getId(), request);

        assertThat(response.getName()).isEqualTo("설탕");
        assertThat(response.getQuantity()).isEqualTo(50);
        List<RefrigeratorItem> items = refrigeratorItemRepository.findByMemberOrderByNameAsc(testMember);
        assertThat(items).hasSize(2);
    }

    @DisplayName("식재료 추가 - 소비기한 없이 추가 가능")
    @Test
    void addItem_Success_WithoutExpirationDate() {
        RefrigeratorDto.CreateRequest request = RefrigeratorDto.CreateRequest.builder()
                .name("소금").quantity(500).unit("g").build();

        RefrigeratorDto.Response response = refrigeratorService.addItem(testMember.getId(), request);

        assertThat(response.getExpirationDate()).isNull();
    }

    // ========== REF-03,04: 식재료 일괄 추가 테스트 ==========

    @DisplayName("식재료 일괄 추가 - 모두 성공")
    @Test
    void addItemsBulk_AllSuccess() {
        List<RefrigeratorDto.CreateRequest> items = List.of(
                RefrigeratorDto.CreateRequest.builder().name("우유").quantity(1).unit("L").build(),
                RefrigeratorDto.CreateRequest.builder().name("계란").quantity(10).unit("개").build(),
                RefrigeratorDto.CreateRequest.builder().name("빵").quantity(1).unit("개").build()
        );
        RefrigeratorDto.BulkCreateRequest request = new RefrigeratorDto.BulkCreateRequest(items);

        RefrigeratorDto.BulkCreateResponse response = refrigeratorService.addItemsBulk(testMember.getId(), request);

        assertThat(response.getSuccessCount()).isEqualTo(3);
        assertThat(response.getAddedItems()).hasSize(3);
    }

    @DisplayName("식재료 일괄 추가 - 부분 성공 (중복 건너뛰기)")
    @Test
    void addItemsBulk_PartialSuccess() {
        refrigeratorItemRepository.save(RefrigeratorItem.builder().member(testMember).name("우유").build());

        List<RefrigeratorDto.CreateRequest> items = List.of(
                RefrigeratorDto.CreateRequest.builder().name("우유").quantity(1).build(),
                RefrigeratorDto.CreateRequest.builder().name("계란").quantity(10).build(),
                RefrigeratorDto.CreateRequest.builder().name("빵").quantity(1).build()
        );
        RefrigeratorDto.BulkCreateRequest request = new RefrigeratorDto.BulkCreateRequest(items);

        RefrigeratorDto.BulkCreateResponse response = refrigeratorService.addItemsBulk(testMember.getId(), request);

        assertThat(response.getSuccessCount()).isEqualTo(3);

        RefrigeratorItem milk = refrigeratorItemRepository.findByMemberAndNameAndExpirationDateIsNull(testMember, "우유").orElseThrow();
        assertThat(milk.getQuantity()).isEqualTo(2);
    }

    @DisplayName("식재료 일괄 추가 - 빈 목록")
    @Test
    void addItemsBulk_EmptyList() {
        RefrigeratorDto.BulkCreateRequest request = new RefrigeratorDto.BulkCreateRequest(List.of());
        RefrigeratorDto.BulkCreateResponse response = refrigeratorService.addItemsBulk(testMember.getId(), request);

        assertThat(response.getSuccessCount()).isZero();
    }

    // ========== REF-05: 식재료 수정 테스트 ==========

    @DisplayName("식재료 수정 - 성공")
    @Test
    void updateItem_Success() {
        RefrigeratorItem saved = refrigeratorItemRepository.save(RefrigeratorItem.builder()
                .member(testMember).name("우유").quantity(1).unit("L").expirationDate(LocalDate.now().plusDays(7)).build());
        RefrigeratorDto.UpdateRequest request = RefrigeratorDto.UpdateRequest.builder()
                .quantity(2).unit("L").expirationDate(LocalDate.now().plusDays(10)).memo("추가 구매").build();

        RefrigeratorDto.Response response = refrigeratorService.updateItem(testMember.getId(), saved.getId(), request);

        assertThat(response.getName()).isEqualTo("우유");
        assertThat(response.getQuantity()).isEqualTo(2);
        assertThat(response.getExpirationDate()).isEqualTo(request.getExpirationDate());
        assertThat(response.getMemo()).isEqualTo("추가 구매");
    }

    @DisplayName("식재료 수정 - 존재하지 않는 ID로 수정 시 예외")
    @Test
    void updateItem_Fail_NotFound() {
        RefrigeratorDto.UpdateRequest request = RefrigeratorDto.UpdateRequest.builder().quantity(2).build();

        assertThatThrownBy(() -> refrigeratorService.updateItem(testMember.getId(), 999L, request))
                .isInstanceOf(ItemNotFoundException.class);
    }

    @DisplayName("식재료 수정 - 권한 없는 사용자가 수정 시 예외")
    @Test
    void updateItem_Fail_Unauthorized() {
        RefrigeratorItem saved = refrigeratorItemRepository.save(RefrigeratorItem.builder().member(testMember).name("우유").build());
        RefrigeratorDto.UpdateRequest request = RefrigeratorDto.UpdateRequest.builder().quantity(2).build();

        assertThatThrownBy(() -> refrigeratorService.updateItem(otherMember.getId(), saved.getId(), request))
                .isInstanceOf(UnauthorizedItemAccessException.class);
    }

    // ========== REF-06: 식재료 삭제 테스트 ==========

    @DisplayName("식재료 삭제 - 성공")
    @Test
    void deleteItem_Success() {
        RefrigeratorItem saved = refrigeratorItemRepository.save(RefrigeratorItem.builder().member(testMember).name("우유").build());

        refrigeratorService.deleteItem(testMember.getId(), saved.getId());

        assertThat(refrigeratorItemRepository.existsById(saved.getId())).isFalse();
    }

    @DisplayName("식재료 삭제 - 존재하지 않는 ID로 삭제 시 예외")
    @Test
    void deleteItem_Fail_NotFound() {
        assertThatThrownBy(() -> refrigeratorService.deleteItem(testMember.getId(), 999L))
                .isInstanceOf(ItemNotFoundException.class);
    }

    @DisplayName("식재료 삭제 - 권한 없는 사용자가 삭제 시 예외")
    @Test
    void deleteItem_Fail_Unauthorized() {
        RefrigeratorItem saved = refrigeratorItemRepository.save(RefrigeratorItem.builder().member(testMember).name("우유").build());

        assertThatThrownBy(() -> refrigeratorService.deleteItem(otherMember.getId(), saved.getId()))
                .isInstanceOf(UnauthorizedItemAccessException.class);
    }

    // ========== REF-07: 레시피 추천 기능 테스트 ==========

    @DisplayName("REF-07: 보유 재료가 많을수록 매칭률이 높다")
    @Test
    void getRecommendations_MatchRate() {
        refrigeratorItemRepository.save(RefrigeratorItem.builder().member(testMember).name("김치").quantity(1).build());
        refrigeratorItemRepository.save(RefrigeratorItem.builder().member(testMember).name("돼지고기").quantity(1).build());
        refrigeratorItemRepository.save(RefrigeratorItem.builder().member(testMember).name("두부").quantity(1).build());

        Recipe recipe1 = Recipe.builder().name("김치찌개").cookTime(30).servings(2).difficulty(Recipe.Difficulty.EASY).build();
        recipe1.addIngredient(RecipeIngredient.builder().name("김치").amount("200g").build());
        recipe1.addIngredient(RecipeIngredient.builder().name("돼지고기").amount("100g").build());
        recipe1.addIngredient(RecipeIngredient.builder().name("두부").amount("1모").build());
        // [수정] saveAndFlush 사용
        recipeRepository.saveAndFlush(recipe1);

        Recipe recipe2 = Recipe.builder().name("김치볶음밥").cookTime(15).servings(1).difficulty(Recipe.Difficulty.EASY).build();
        recipe2.addIngredient(RecipeIngredient.builder().name("김치").amount("100g").build());
        recipe2.addIngredient(RecipeIngredient.builder().name("밥").amount("1공기").build());
        recipe2.addIngredient(RecipeIngredient.builder().name("참기름").amount("1스푼").build());
        // [수정] saveAndFlush 사용
        recipeRepository.saveAndFlush(recipe2);

        RecommendationDto.RecommendationResponse response = refrigeratorService.getRecommendations(testMember.getId(), 10);

        assertThat(response.getRecommendations()).hasSize(2);
        assertThat(response.getRecommendations().get(0).getRecipeName()).isEqualTo("김치찌개");
        assertThat(response.getRecommendations().get(1).getRecipeName()).isEqualTo("김치볶음밥");
    }

    @DisplayName("REF-07: Fuzzy matching으로 유사 재료도 매칭된다")
    @Test
    void getRecommendations_FuzzyMatching() {
        refrigeratorItemRepository.save(RefrigeratorItem.builder().member(testMember).name("양파").quantity(1).build());

        Recipe recipe = Recipe.builder().name("양파요리").build();
        recipe.addIngredient(RecipeIngredient.builder().name("양파즙").build());
        recipeRepository.saveAndFlush(recipe);

        RecommendationDto.RecommendationResponse response = refrigeratorService.getRecommendations(testMember.getId(), 10);

        assertThat(response.getRecommendations()).hasSize(1);
        assertThat(response.getRecommendations().get(0).getMatchRate()).isEqualTo(100.0);
    }

    @DisplayName("REF-07: limit 파라미터로 결과 개수 제한")
    @Test
    void getRecommendations_LimitWorks() {
        for (int i = 1; i <= 5; i++) {
            Recipe recipe = Recipe.builder().name("레시피" + i).build();
            recipe.addIngredient(RecipeIngredient.builder().name("재료" + i).build());
            recipeRepository.saveAndFlush(recipe);
        }

        refrigeratorItemRepository.save(RefrigeratorItem.builder().member(testMember).name("재료1").build());

        RecommendationDto.RecommendationResponse response = refrigeratorService.getRecommendations(testMember.getId(), 3);

        assertThat(response.getRecommendations()).hasSizeLessThanOrEqualTo(3);
    }

    @DisplayName("REF-07: 매칭률 0인 레시피는 제외된다")
    @Test
    void getRecommendations_ExcludeZeroMatch() {
        refrigeratorItemRepository.save(RefrigeratorItem.builder().member(testMember).name("김치").build());

        Recipe recipe1 = Recipe.builder().name("김치찌개").build();
        recipe1.addIngredient(RecipeIngredient.builder().name("김치").build());
        recipeRepository.saveAndFlush(recipe1);

        Recipe recipe2 = Recipe.builder().name("된장찌개").build();
        recipe2.addIngredient(RecipeIngredient.builder().name("된장").build());
        recipeRepository.saveAndFlush(recipe2);

        RecommendationDto.RecommendationResponse response = refrigeratorService.getRecommendations(testMember.getId(), 10);

        assertThat(response.getRecommendations()).hasSize(1);
        assertThat(response.getRecommendations().get(0).getRecipeName()).isEqualTo("김치찌개");
    }

    // ========== REF-08: 재료 자동 차감 기능 테스트 ==========

    @DisplayName("REF-08: 재료 차감 미리보기 - 모든 재료 충분")
    @Test
    void previewDeduction_AllOK() {
        refrigeratorItemRepository.save(RefrigeratorItem.builder().member(testMember).name("김치").quantity(5).build());
        refrigeratorItemRepository.save(RefrigeratorItem.builder().member(testMember).name("돼지고기").quantity(3).build());

        Recipe recipe = Recipe.builder().name("김치찌개").build();
        recipe.addIngredient(RecipeIngredient.builder().name("김치").isRequired(true).build());
        recipe.addIngredient(RecipeIngredient.builder().name("돼지고기").isRequired(true).build());

        // [핵심 수정] 저장된 객체를 받아 ID 사용
        Recipe saved = recipeRepository.saveAndFlush(recipe);

        DeductionDto.DeductPreviewResponse response = refrigeratorService.previewDeduction(testMember.getId(), saved.getId());

        assertThat(response.isCanProceed()).isTrue();
    }

    @DisplayName("REF-08: 재료 차감 미리보기 - 필수 재료 부족")
    @Test
    void previewDeduction_RequiredInsufficient() {
        refrigeratorItemRepository.save(RefrigeratorItem.builder().member(testMember).name("김치").quantity(0).build());

        Recipe recipe = Recipe.builder().name("김치찌개").build();
        recipe.addIngredient(RecipeIngredient.builder().name("김치").isRequired(true).build());

        Recipe saved = recipeRepository.saveAndFlush(recipe);

        DeductionDto.DeductPreviewResponse response = refrigeratorService.previewDeduction(testMember.getId(), saved.getId());

        assertThat(response.isCanProceed()).isFalse();
        assertThat(response.getIngredients().get(0).getStatus()).isEqualTo(DeductionDto.DeductionStatus.INSUFFICIENT);
    }

    @DisplayName("REF-08: 재료 차감 미리보기 - 재료 없음")
    @Test
    void previewDeduction_NotFound() {
        Recipe recipe = Recipe.builder().name("김치찌개").build();
        recipe.addIngredient(RecipeIngredient.builder().name("김치").isRequired(true).build());

        Recipe saved = recipeRepository.saveAndFlush(recipe);

        DeductionDto.DeductPreviewResponse response = refrigeratorService.previewDeduction(testMember.getId(), saved.getId());

        assertThat(response.isCanProceed()).isFalse();
        assertThat(response.getIngredients().get(0).getStatus()).isEqualTo(DeductionDto.DeductionStatus.NOT_FOUND);
    }

    @DisplayName("REF-08: 재료 차감 실행 - 정상 차감")
    @Test
    void deductIngredients_Success() {
        RefrigeratorItem item1 = refrigeratorItemRepository.save(RefrigeratorItem.builder().member(testMember).name("김치").quantity(5).build());
        RefrigeratorItem item2 = refrigeratorItemRepository.save(RefrigeratorItem.builder().member(testMember).name("돼지고기").quantity(3).build());

        Recipe recipe = Recipe.builder().name("김치찌개").build();
        recipe.addIngredient(RecipeIngredient.builder().name("김치").build());
        recipe.addIngredient(RecipeIngredient.builder().name("돼지고기").build());

        Recipe saved = recipeRepository.saveAndFlush(recipe);

        DeductionDto.DeductRequest request = DeductionDto.DeductRequest.builder()
                .recipeId(saved.getId()) // 저장된 ID 사용
                .ignoreWarnings(false)
                .build();

        DeductionDto.DeductResponse response = refrigeratorService.deductIngredients(testMember.getId(), request);

        assertThat(response.getSuccessCount()).isEqualTo(2);
        RefrigeratorItem updatedItem1 = refrigeratorItemRepository.findById(item1.getId()).get();
        assertThat(updatedItem1.getQuantity()).isEqualTo(4);
    }

    @DisplayName("REF-08: 재료 차감 실행 - 필수 재료 부족 시 예외")
    @Test
    void deductIngredients_ThrowsWhenRequiredInsufficient() {
        refrigeratorItemRepository.save(RefrigeratorItem.builder().member(testMember).name("김치").quantity(0).build());

        Recipe recipe = Recipe.builder().name("김치찌개").build();
        recipe.addIngredient(RecipeIngredient.builder().name("김치").isRequired(true).build());

        Recipe saved = recipeRepository.saveAndFlush(recipe);

        DeductionDto.DeductRequest request = DeductionDto.DeductRequest.builder()
                .recipeId(saved.getId())
                .ignoreWarnings(false)
                .build();

        assertThatThrownBy(() -> refrigeratorService.deductIngredients(testMember.getId(), request))
                .isInstanceOf(IllegalStateException.class);
    }

    @DisplayName("REF-08: 재료 차감 실행 - ignoreWarnings=true면 강제 실행")
    @Test
    void deductIngredients_IgnoreWarnings() {
        refrigeratorItemRepository.save(RefrigeratorItem.builder().member(testMember).name("김치").quantity(0).build());

        Recipe recipe = Recipe.builder().name("김치찌개").build();
        recipe.addIngredient(RecipeIngredient.builder().name("김치").isRequired(true).build());

        Recipe saved = recipeRepository.saveAndFlush(recipe);

        DeductionDto.DeductRequest request = DeductionDto.DeductRequest.builder()
                .recipeId(saved.getId())
                .ignoreWarnings(true)
                .build();

        DeductionDto.DeductResponse response = refrigeratorService.deductIngredients(testMember.getId(), request);
        assertThat(response.getFailedCount()).isEqualTo(1);
    }

    @DisplayName("REF-08: 수량은 최소 0까지만 감소한다")
    @Test
    void deductIngredients_MinimumZero() {
        RefrigeratorItem item = refrigeratorItemRepository.save(RefrigeratorItem.builder().member(testMember).name("김치").quantity(1).build());

        Recipe recipe = Recipe.builder().name("김치찌개").build();
        recipe.addIngredient(RecipeIngredient.builder().name("김치").build());

        Recipe saved = recipeRepository.saveAndFlush(recipe);

        DeductionDto.DeductRequest request = DeductionDto.DeductRequest.builder()
                .recipeId(saved.getId())
                .build();

        refrigeratorService.deductIngredients(testMember.getId(), request);

        RefrigeratorItem updated = refrigeratorItemRepository.findById(item.getId()).get();
        assertThat(updated.getQuantity()).isEqualTo(0);
    }
}