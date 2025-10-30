package com.capstone.web.refrigerator.controller;

import com.capstone.web.auth.jwt.JwtAuthenticationFilter.MemberPrincipal;
import com.capstone.web.refrigerator.dto.RefrigeratorDto;
import com.capstone.web.refrigerator.service.RefrigeratorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

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
        Long memberId = extractMemberId(authentication);
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
        Long memberId = extractMemberId(authentication);
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
        Long memberId = extractMemberId(authentication);
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
        Long memberId = extractMemberId(authentication);
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
        Long memberId = extractMemberId(authentication);
        refrigeratorService.deleteItem(memberId, id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Authentication에서 회원 ID 추출
     */
    private Long extractMemberId(Authentication authentication) {
        MemberPrincipal principal = (MemberPrincipal) authentication.getPrincipal();
        return principal.id();
    }
}
