package com.capstone.web.refrigerator.controller;

import com.capstone.web.common.util.AuthenticationUtils;
import com.capstone.web.refrigerator.dto.DeductionDto;
import com.capstone.web.refrigerator.dto.RecommendationDto;
import com.capstone.web.refrigerator.dto.RefrigeratorDto;
import com.capstone.web.refrigerator.service.RefrigeratorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;

/**
 * 냉장고 식재료 컨트롤러
 * REF-01: 내 냉장고 식재료 목록 조회
 * REF-02: 수동으로 식재료 추가
 * REF-05: 식재료 정보 수정
 * REF-06: 식재료 삭제
 */
@Tag(name = "Refrigerator", description = "냉장고 식재료 관리 API")
@RestController
@RequestMapping("/api/v1/refrigerator")
@RequiredArgsConstructor
public class RefrigeratorController {

    private final RefrigeratorService refrigeratorService;

    @Operation(
            summary = "내 냉장고 식재료 목록 조회",
            description = """
                    로그인한 사용자의 모든 식재료를 조회합니다.
                    
                    **정렬 옵션** (sortBy 파라미터):
                    - `expirationDate` (기본값): 소비기한 임박순
                    - `name`: 식재료명 가나다순
                    - `createdAt`: 등록일 최신순
                    
                    **응답 정보**:
                    - 각 식재료의 D-day (소비기한까지 남은 일수)
                    - 소비기한 임박(3일 이내) 및 경과 여부
                    - 전체 개수, 임박 개수, 경과 개수
                    """,
            security = @SecurityRequirement(name = "JWT")
    )
    @GetMapping("/items")
    public ResponseEntity<RefrigeratorDto.ItemListResponse> getMyItems(
            @RequestParam(required = false, defaultValue = "expirationDate") String sortBy,
            Authentication authentication
    ) {
        Long memberId = AuthenticationUtils.extractMemberId(authentication);
        RefrigeratorDto.ItemListResponse response = refrigeratorService.getMyItems(memberId, sortBy);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "식재료 추가",
            description = """
                    새로운 식재료를 냉장고에 추가합니다.
                    
                    **필수 입력**: 식재료명
                    **선택 입력**: 수량, 용량/단위, 소비기한, 메모
                    
                    **중복 체크**: 같은 이름의 식재료는 등록할 수 없습니다.
                    (기존 항목의 수정을 권장)
                    """,
            security = @SecurityRequirement(name = "JWT")
    )
    @PostMapping("/items")
    public ResponseEntity<RefrigeratorDto.Response> addItem(
            @Valid @RequestBody RefrigeratorDto.CreateRequest request,
            Authentication authentication
    ) {
        Long memberId = AuthenticationUtils.extractMemberId(authentication);
        RefrigeratorDto.Response response = refrigeratorService.addItem(memberId, request);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.getId())
                .toUri();

        return ResponseEntity.created(location).body(response);
    }

    @Operation(
            summary = "식재료 일괄 추가",
            description = """
                    여러 식재료를 한 번에 추가합니다.
                    
                    **사용 시나리오**:
                    - REF-03, 04: 영수증 스캔 OCR 결과 일괄 등록
                    - 구매 내역 일괄 등록
                    
                    **처리 방식**:
                    - 중복된 식재료는 건너뛰고 나머지만 추가
                    - 성공/실패 개수 및 실패 항목 목록 반환
                    """,
            security = @SecurityRequirement(name = "JWT")
    )
    @PostMapping("/items/bulk")
    public ResponseEntity<RefrigeratorDto.BulkCreateResponse> addItemsBulk(
            @Valid @RequestBody RefrigeratorDto.BulkCreateRequest request,
            Authentication authentication
    ) {
        Long memberId = AuthenticationUtils.extractMemberId(authentication);
        RefrigeratorDto.BulkCreateResponse response = refrigeratorService.addItemsBulk(memberId, request);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "식재료 정보 수정",
            description = """
                    기존 식재료의 정보를 수정합니다.
                    
                    **수정 가능 항목**: 수량, 용량/단위, 소비기한, 메모
                    **수정 불가 항목**: 식재료명 (삭제 후 재등록 권장)
                    
                    **권한 확인**: 본인이 등록한 식재료만 수정 가능
                    """,
            security = @SecurityRequirement(name = "JWT")
    )
    @PutMapping("/items/{id}")
    public ResponseEntity<RefrigeratorDto.Response> updateItem(
            @PathVariable Long id,
            @Valid @RequestBody RefrigeratorDto.UpdateRequest request,
            Authentication authentication
    ) {
        Long memberId = AuthenticationUtils.extractMemberId(authentication);
        RefrigeratorDto.Response response = refrigeratorService.updateItem(memberId, id, request);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "식재료 삭제",
            description = """
                    냉장고에서 식재료를 삭제합니다.
                    
                    **사용 시나리오**:
                    - 다 사용한 식재료
                    - 소비기한이 지나 폐기한 식재료
                    - 잘못 등록한 식재료
                    
                    **권한 확인**: 본인이 등록한 식재료만 삭제 가능
                    """,
            security = @SecurityRequirement(name = "JWT")
    )
    @DeleteMapping("/items/{id}")
    public ResponseEntity<Void> deleteItem(
            @PathVariable Long id,
            Authentication authentication
    ) {
        Long memberId = AuthenticationUtils.extractMemberId(authentication);
        refrigeratorService.deleteItem(memberId, id);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "REF-07: 보유 재료 기반 레시피 추천",
            description = """
                    냉장고에 보유한 재료를 기반으로 만들 수 있는 레시피를 추천합니다.
                    
                    **추천 알고리즘**:
                    1. 사용자의 냉장고 재료 조회
                    2. 모든 레시피의 필요 재료와 비교
                    3. 매칭률 계산 = (보유 재료 / 전체 필요 재료) × 100
                    4. 매칭률 높은 순으로 정렬하여 반환
                    
                    **매칭 방식**:
                    - 완전 일치 또는 부분 일치 (예: "양파" ↔ "양파즙")
                    - 대소문자 무시
                    
                    **응답 정보**:
                    - 레시피 기본 정보 (이름, 조리시간, 난이도 등)
                    - 매칭률 (0-100%)
                    - 보유 중인 재료 목록
                    - 부족한 재료 목록 (필수 여부 포함)
                    
                    **파라미터**:
                    - limit: 추천 개수 (기본값: 10, 최대: 50)
                    """,
            security = @SecurityRequirement(name = "JWT")
    )
    @GetMapping("/recommendations")
    public ResponseEntity<RecommendationDto.RecommendationResponse> getRecommendations(
            @RequestParam(required = false, defaultValue = "10") Integer limit,
            Authentication authentication
    ) {
        Long memberId = AuthenticationUtils.extractMemberId(authentication);

        // limit 범위 검증
        if (limit <= 0 || limit > 50) {
            limit = 10;
        }

        RecommendationDto.RecommendationResponse response =
                refrigeratorService.getRecommendations(memberId, limit);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "REF-08: 레시피 재료 차감 미리보기",
            description = """
                    레시피를 만들 때 필요한 재료를 냉장고에서 차감하기 전에 미리 확인합니다.
                    
                    **확인 항목**:
                    - 각 재료별 보유 여부 (OK/INSUFFICIENT/NOT_FOUND)
                    - 현재 수량과 필요량 비교
                    - 필수 재료 부족 여부
                    
                    **상태**:
                    - OK: 충분히 보유 중
                    - INSUFFICIENT: 수량 부족
                    - NOT_FOUND: 냉장고에 없음
                    
                    **canProceed**: 모든 필수 재료가 충분할 때만 true
                    """,
            security = @SecurityRequirement(name = "JWT")
    )
    @GetMapping("/deduct-preview")
    public ResponseEntity<DeductionDto.DeductPreviewResponse> previewDeduction(
            @RequestParam Long recipeId,
            Authentication authentication
    ) {
        Long memberId = AuthenticationUtils.extractMemberId(authentication);
        DeductionDto.DeductPreviewResponse response =
                refrigeratorService.previewDeduction(memberId, recipeId);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "REF-08: 레시피 재료 차감 실행",
            description = """
                    레시피를 만들 때 사용한 재료를 냉장고에서 차감합니다.
                    
                    **처리 과정**:
                    1. 미리보기로 재료 확인
                    2. canProceed가 false이고 ignoreWarnings=false이면 예외 발생
                    3. 각 재료 수량 -1 (최소 0)
                    4. 차감 결과 반환
                    
                    **주의사항**:
                    - 수량만 감소, 재료 삭제는 하지 않음
                    - ignoreWarnings=true 시 경고 무시하고 강제 실행
                    - 트랜잭션으로 관리 (일부 실패 시 전체 롤백 아님)
                    
                    **요청 파라미터**:
                    - recipeId: 레시피 ID
                    - ignoreWarnings: 경고 무시 여부 (기본: false)
                    """,
            security = @SecurityRequirement(name = "JWT")
    )
    @PostMapping("/deduct")
    public ResponseEntity<DeductionDto.DeductResponse> deductIngredients(
            @Valid @RequestBody DeductionDto.DeductRequest request,
            Authentication authentication
    ) {
        Long memberId = AuthenticationUtils.extractMemberId(authentication);
        DeductionDto.DeductResponse response =
                refrigeratorService.deductIngredients(memberId, request);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "식재료 단건 조회",
            description = "수정 모달 진입 시 기존 데이터를 채우기 위한 단건 조회. 본인 소유만 조회 가능.",
            security = @SecurityRequirement(name = "JWT")
    )
    @GetMapping("/items/{id}")
    public ResponseEntity<RefrigeratorDto.Response> getItem(
            @PathVariable Long id,
            Authentication authentication
    ) {
        Long memberId = AuthenticationUtils.extractMemberId(authentication);
        RefrigeratorDto.Response response = refrigeratorService.getItem(memberId, id);
        return ResponseEntity.ok(response);
    }
}
