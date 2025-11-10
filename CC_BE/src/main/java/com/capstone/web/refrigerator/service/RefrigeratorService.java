package com.capstone.web.refrigerator.service;

import com.capstone.web.member.domain.Member;
import com.capstone.web.member.repository.MemberRepository;
import com.capstone.web.recipe.domain.Recipe;
import com.capstone.web.recipe.domain.RecipeIngredient;
import com.capstone.web.recipe.repository.RecipeRepository;
import com.capstone.web.refrigerator.domain.RefrigeratorItem;
import com.capstone.web.refrigerator.dto.DeductionDto;
import com.capstone.web.refrigerator.dto.RecommendationDto;
import com.capstone.web.refrigerator.dto.RefrigeratorDto;
import com.capstone.web.refrigerator.exception.DuplicateItemException;
import com.capstone.web.refrigerator.exception.ItemNotFoundException;
import com.capstone.web.refrigerator.exception.UnauthorizedItemAccessException;
import com.capstone.web.refrigerator.repository.RefrigeratorItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 냉장고 식재료 서비스
 * REF-01: 내 냉장고 식재료 목록 조회
 * REF-02: 수동으로 식재료 추가
 * REF-05: 식재료 정보 수정
 * REF-06: 식재료 삭제
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RefrigeratorService {

    private final RefrigeratorItemRepository refrigeratorItemRepository;
    private final MemberRepository memberRepository;
    private final RecipeRepository recipeRepository;

    /**
     * REF-01: 내 냉장고 식재료 목록 조회
     * 소비기한 임박순으로 정렬
     */
    public RefrigeratorDto.ItemListResponse getMyItems(Long memberId, String sortBy) {
        Member member = getMemberById(memberId);

        List<RefrigeratorItem> items;
        if ("name".equalsIgnoreCase(sortBy)) {
            items = refrigeratorItemRepository.findByMemberOrderByNameAsc(member);
        } else if ("createdAt".equalsIgnoreCase(sortBy)) {
            items = refrigeratorItemRepository.findByMemberOrderByCreatedAtDesc(member);
        } else {
            // 기본: 소비기한 임박순
            items = refrigeratorItemRepository.findByMemberOrderByExpirationDateAsc(member);
        }

        return RefrigeratorDto.ItemListResponse.builder()
                .items(items)
                .build();
    }

    /**
     * REF-02: 수동으로 식재료 추가
     */
    @Transactional
    public RefrigeratorDto.Response addItem(Long memberId, RefrigeratorDto.CreateRequest request) {
        Member member = getMemberById(memberId);

        // 중복 체크
        if (refrigeratorItemRepository.existsByMemberAndName(member, request.getName())) {
            throw new DuplicateItemException("이미 등록된 식재료입니다: " + request.getName());
        }

        RefrigeratorItem item = RefrigeratorItem.builder()
                .member(member)
                .name(request.getName())
                .quantity(request.getQuantity())
                .unit(request.getUnit())
                .expirationDate(request.getExpirationDate())
                .memo(request.getMemo())
                .build();

        RefrigeratorItem savedItem = refrigeratorItemRepository.save(item);
        log.info("식재료 추가: memberId={}, itemName={}", memberId, request.getName());

        return new RefrigeratorDto.Response(savedItem);
    }

    /**
     * REF-03, 04: 일괄 추가 (OCR 결과 등록)
     */
    @Transactional
    public RefrigeratorDto.BulkCreateResponse addItemsBulk(Long memberId, RefrigeratorDto.BulkCreateRequest request) {
        Member member = getMemberById(memberId);

        List<RefrigeratorItem> addedItems = new ArrayList<>();
        List<String> failedItems = new ArrayList<>();

        for (RefrigeratorDto.CreateRequest itemRequest : request.getItems()) {
            try {
                // 중복 체크: 중복이면 건너뛰기 (에러 발생 안 함)
                if (refrigeratorItemRepository.existsByMemberAndName(member, itemRequest.getName())) {
                    failedItems.add(itemRequest.getName() + " (이미 등록됨)");
                    continue;
                }

                RefrigeratorItem item = RefrigeratorItem.builder()
                        .member(member)
                        .name(itemRequest.getName())
                        .quantity(itemRequest.getQuantity())
                        .unit(itemRequest.getUnit())
                        .expirationDate(itemRequest.getExpirationDate())
                        .memo(itemRequest.getMemo())
                        .build();

                RefrigeratorItem savedItem = refrigeratorItemRepository.save(item);
                addedItems.add(savedItem);
            } catch (Exception e) {
                log.warn("식재료 일괄 추가 실패: {}", itemRequest.getName(), e);
                failedItems.add(itemRequest.getName() + " (오류)");
            }
        }

        log.info("식재료 일괄 추가: memberId={}, success={}, fail={}", memberId, addedItems.size(), failedItems.size());

        return RefrigeratorDto.BulkCreateResponse.builder()
                .addedItems(addedItems)
                .failedItems(failedItems)
                .build();
    }

    /**
     * REF-05: 식재료 정보 수정
     */
    @Transactional
    public RefrigeratorDto.Response updateItem(Long memberId, Long itemId, RefrigeratorDto.UpdateRequest request) {
        RefrigeratorItem item = getItemById(itemId);

        // 권한 확인
        if (!item.getMember().getId().equals(memberId)) {
            throw new UnauthorizedItemAccessException("식재료 수정 권한이 없습니다");
        }

        item.update(request.getQuantity(), request.getUnit(), request.getExpirationDate(), request.getMemo());
        log.info("식재료 수정: memberId={}, itemId={}", memberId, itemId);

        return new RefrigeratorDto.Response(item);
    }

    /**
     * REF-06: 식재료 삭제
     */
    @Transactional
    public void deleteItem(Long memberId, Long itemId) {
        RefrigeratorItem item = getItemById(itemId);

        // 권한 확인
        if (!item.getMember().getId().equals(memberId)) {
            throw new UnauthorizedItemAccessException("식재료 삭제 권한이 없습니다");
        }

        refrigeratorItemRepository.delete(item);
        log.info("식재료 삭제: memberId={}, itemId={}", memberId, itemId);
    }

    /**
     * REF-07: 보유 재료 기반 레시피 추천
     * 냉장고에 있는 재료와 레시피 재료를 매칭하여 추천
     */
    public RecommendationDto.RecommendationResponse getRecommendations(Long memberId, Integer limit) {
        // 1단계: 사용자의 냉장고 재료 조회
        List<RefrigeratorItem> myItems = refrigeratorItemRepository.findByMemberId(memberId);
        List<String> myIngredientNames = myItems.stream()
                .map(item -> item.getName().toLowerCase().trim())
                .collect(Collectors.toList());

        log.info("냉장고 재료 조회: memberId={}, 보유 재료 수={}", memberId, myIngredientNames.size());

        // 2단계: 모든 레시피 조회 (재료 포함)
        List<Recipe> allRecipes = recipeRepository.findAllWithIngredients();
        log.info("전체 레시피 조회 완료: 레시피 수={}", allRecipes.size());

        // 3단계: 각 레시피별 매칭률 계산
        List<RecommendationDto.RecommendedRecipe> recommendations = allRecipes.stream()
                .map(recipe -> calculateMatchRate(recipe, myIngredientNames))
                .filter(rec -> rec.getMatchRate() > 0) // 매칭률 0% 제외
                .sorted((r1, r2) -> Double.compare(r2.getMatchRate(), r1.getMatchRate())) // 매칭률 내림차순
                .limit(limit != null && limit > 0 ? limit : 10) // 기본 10개
                .collect(Collectors.toList());

        log.info("레시피 추천 완료: memberId={}, 추천 개수={}", memberId, recommendations.size());

        return RecommendationDto.RecommendationResponse.builder()
                .recommendations(recommendations)
                .totalCount(recommendations.size())
                .build();
    }

    /**
     * 레시피와 보유 재료의 매칭률 계산
     */
    private RecommendationDto.RecommendedRecipe calculateMatchRate(Recipe recipe, List<String> myIngredientNames) {
        List<RecipeIngredient> recipeIngredients = recipe.getIngredients();

        if (recipeIngredients.isEmpty()) {
            // 재료가 없는 레시피는 매칭률 0
            return buildRecommendedRecipe(recipe, 0.0, new ArrayList<>(), recipeIngredients);
        }

        // 매칭된 재료 찾기 (대소문자 무시, 부분 일치)
        List<String> matchedIngredients = new ArrayList<>();
        for (RecipeIngredient recipeIngredient : recipeIngredients) {
            String recipeName = recipeIngredient.getName().toLowerCase().trim();

            for (String myIngredient : myIngredientNames) {
                // 완전 일치 또는 포함 관계
                if (recipeName.equals(myIngredient) ||
                        recipeName.contains(myIngredient) ||
                        myIngredient.contains(recipeName)) {
                    matchedIngredients.add(recipeIngredient.getName());
                    break;
                }
            }
        }

        // 매칭률 계산: (매칭된 재료 수 / 전체 재료 수) * 100
        double matchRate = (double) matchedIngredients.size() / recipeIngredients.size() * 100;

        return buildRecommendedRecipe(recipe, matchRate, matchedIngredients, recipeIngredients);
    }

    /**
     * RecommendedRecipe DTO 생성
     */
    private RecommendationDto.RecommendedRecipe buildRecommendedRecipe(
            Recipe recipe,
            double matchRate,
            List<String> matchedIngredients,
            List<RecipeIngredient> recipeIngredients) {

        // 부족한 재료 찾기
        List<RecommendationDto.MissingIngredient> missingIngredients = recipeIngredients.stream()
                .filter(ri -> !matchedIngredients.contains(ri.getName()))
                .map(ri -> RecommendationDto.MissingIngredient.builder()
                        .name(ri.getName())
                        .amount(ri.getAmount())
                        .isRequired(ri.getIsRequired())
                        .build())
                .collect(Collectors.toList());

        return RecommendationDto.RecommendedRecipe.builder()
                .recipeId(recipe.getId())
                .recipeName(recipe.getName())
                .description(recipe.getDescription())
                .cookTime(recipe.getCookTime())
                .servings(recipe.getServings())
                .difficulty(recipe.getDifficulty() != null ? recipe.getDifficulty().name() : null)
                .imageUrl(recipe.getImageUrl())
                .matchRate(Math.round(matchRate * 10) / 10.0) // 소수점 1자리
                .matchedIngredients(matchedIngredients)
                .missingIngredients(missingIngredients)
                .totalIngredientsCount(recipeIngredients.size())
                .matchedIngredientsCount(matchedIngredients.size())
                .build();
    }

    /**
     * REF-08: 레시피 재료 차감 미리보기
     * 실제로 차감하지 않고 차감 가능 여부만 확인
     */
    public DeductionDto.DeductPreviewResponse previewDeduction(Long memberId, Long recipeId) {
        // 1단계: 레시피 조회
        Recipe recipe = recipeRepository.findByIdWithIngredients(recipeId);
        if (recipe == null) {
            throw new IllegalArgumentException("레시피를 찾을 수 없습니다: " + recipeId);
        }

        // 2단계: 사용자 냉장고 재료 조회
        List<RefrigeratorItem> myItems = refrigeratorItemRepository.findByMemberId(memberId);

        // 3단계: 각 재료별 차감 상태 확인
        List<DeductionDto.IngredientDeductionStatus> statusList = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        boolean canProceed = true;

        for (RecipeIngredient recipeIngredient : recipe.getIngredients()) {
            DeductionDto.IngredientDeductionStatus status =
                    checkIngredientStatus(recipeIngredient, myItems);
            statusList.add(status);

            // 필수 재료인데 OK가 아니면 진행 불가
            if (recipeIngredient.getIsRequired() && status.getStatus() != DeductionDto.DeductionStatus.OK) {
                canProceed = false;
                warnings.add(status.getMessage());
            }
        }

        log.info("재료 차감 미리보기: memberId={}, recipeId={}, canProceed={}",
                memberId, recipeId, canProceed);

        return DeductionDto.DeductPreviewResponse.builder()
                .recipeId(recipeId)
                .recipeName(recipe.getName())
                .ingredients(statusList)
                .canProceed(canProceed)
                .warnings(warnings)
                .build();
    }

    /**
     * REF-08: 레시피 재료 차감 실행
     * 냉장고에서 실제로 재료 수량 감소
     */
    @Transactional
    public DeductionDto.DeductResponse deductIngredients(Long memberId, DeductionDto.DeductRequest request) {
        // 1단계: 미리보기로 상태 확인
        DeductionDto.DeductPreviewResponse preview = previewDeduction(memberId, request.getRecipeId());

        // 2단계: 경고 무시하지 않고 진행 불가능하면 예외
        if (!preview.isCanProceed() && !request.getIgnoreWarnings()) {
            throw new IllegalStateException("필수 재료가 부족합니다. warnings: " + preview.getWarnings());
        }

        // 3단계: 레시피 조회
        Recipe recipe = recipeRepository.findByIdWithIngredients(request.getRecipeId());

        // 4단계: 사용자 냉장고 재료 조회
        List<RefrigeratorItem> myItems = refrigeratorItemRepository.findByMemberId(memberId);

        // 5단계: 재료 차감 실행
        List<DeductionDto.DeductedIngredient> deductedList = new ArrayList<>();
        List<String> failedList = new ArrayList<>();

        for (RecipeIngredient recipeIngredient : recipe.getIngredients()) {
            try {
                RefrigeratorItem item = findMatchingItem(recipeIngredient.getName(), myItems);

                if (item != null && item.getQuantity() > 0) {
                    int previousQty = item.getQuantity();
                    int newQty = Math.max(0, previousQty - 1); // 최소 0

                    item.updateQuantity(newQty);
                    refrigeratorItemRepository.save(item);

                    deductedList.add(DeductionDto.DeductedIngredient.builder()
                            .name(item.getName())
                            .previousQuantity(previousQty)
                            .newQuantity(newQty)
                            .build());

                    log.info("재료 차감: {} ({} → {})", item.getName(), previousQty, newQty);
                } else {
                    failedList.add(recipeIngredient.getName());
                }
            } catch (Exception e) {
                log.error("재료 차감 실패: {}", recipeIngredient.getName(), e);
                failedList.add(recipeIngredient.getName());
            }
        }

        log.info("재료 차감 완료: recipeId={}, 성공={}, 실패={}",
                request.getRecipeId(), deductedList.size(), failedList.size());

        return DeductionDto.DeductResponse.builder()
                .recipeId(request.getRecipeId())
                .recipeName(recipe.getName())
                .successCount(deductedList.size())
                .failedCount(failedList.size())
                .deductedIngredients(deductedList)
                .failedIngredients(failedList)
                .build();
    }

    /**
     * 재료 상태 확인 (OK/INSUFFICIENT/NOT_FOUND)
     */
    private DeductionDto.IngredientDeductionStatus checkIngredientStatus(
            RecipeIngredient recipeIngredient,
            List<RefrigeratorItem> myItems) {

        RefrigeratorItem item = findMatchingItem(recipeIngredient.getName(), myItems);

        if (item == null) {
            return DeductionDto.IngredientDeductionStatus.builder()
                    .name(recipeIngredient.getName())
                    .requiredAmount(recipeIngredient.getAmount())
                    .currentAmount(null)
                    .currentQuantity(0)
                    .status(DeductionDto.DeductionStatus.NOT_FOUND)
                    .isRequired(recipeIngredient.getIsRequired())
                    .message(recipeIngredient.getName() + " 없음")
                    .build();
        }

        // 수량 확인 (간단하게 1개 이상이면 OK)
        if (item.getQuantity() == null || item.getQuantity() <= 0) {
            return DeductionDto.IngredientDeductionStatus.builder()
                    .name(recipeIngredient.getName())
                    .requiredAmount(recipeIngredient.getAmount())
                    .currentAmount(item.getQuantity() + (item.getUnit() != null ? item.getUnit() : ""))
                    .currentQuantity(item.getQuantity())
                    .status(DeductionDto.DeductionStatus.INSUFFICIENT)
                    .isRequired(recipeIngredient.getIsRequired())
                    .message(recipeIngredient.getName() + " 부족 (수량: 0)")
                    .build();
        }

        return DeductionDto.IngredientDeductionStatus.builder()
                .name(recipeIngredient.getName())
                .requiredAmount(recipeIngredient.getAmount())
                .currentAmount(item.getQuantity() + (item.getUnit() != null ? item.getUnit() : ""))
                .currentQuantity(item.getQuantity())
                .status(DeductionDto.DeductionStatus.OK)
                .isRequired(recipeIngredient.getIsRequired())
                .message("충분")
                .build();
    }

    /**
     * 냉장고에서 재료 이름으로 아이템 찾기 (퍼지 매칭)
     */
    private RefrigeratorItem findMatchingItem(String recipeName, List<RefrigeratorItem> myItems) {
        String normalizedRecipeName = recipeName.toLowerCase().trim();

        // 1차: 완전 일치
        for (RefrigeratorItem item : myItems) {
            if (item.getName().toLowerCase().trim().equals(normalizedRecipeName)) {
                return item;
            }
        }

        // 2차: 포함 관계 (레시피 이름이 냉장고 이름에 포함 or 반대)
        for (RefrigeratorItem item : myItems) {
            String itemName = item.getName().toLowerCase().trim();
            if (itemName.contains(normalizedRecipeName) || normalizedRecipeName.contains(itemName)) {
                return item;
            }
        }

        return null;
    }

    private Member getMemberById(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다"));
    }

    private RefrigeratorItem getItemById(Long itemId) {
        return refrigeratorItemRepository.findById(itemId)
                .orElseThrow(() -> new ItemNotFoundException("식재료를 찾을 수 없습니다: " + itemId));
    }

    /**
     * REF-05: 식재료 단건 조회 (수정용)
     * 본인 소유 식재료만 조회 가능
     */
    public RefrigeratorDto.Response getItem(Long memberId, Long itemId) {
        RefrigeratorItem item = getItemById(itemId);
        if (!item.getMember().getId().equals(memberId)) {
            throw new UnauthorizedItemAccessException("식재료 조회 권한이 없습니다");
        }
        return new RefrigeratorDto.Response(item);
    }
}
