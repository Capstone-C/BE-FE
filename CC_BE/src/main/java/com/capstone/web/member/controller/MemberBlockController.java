package com.capstone.web.member.controller;

import com.capstone.web.common.util.AuthenticationUtils;
import com.capstone.web.member.dto.BlockListResponse;
import com.capstone.web.member.dto.BlockRequest;
import com.capstone.web.member.service.MemberBlockService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

@Tag(name = "Member", description = "회원 관리 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/members/blocks")
public class MemberBlockController {
    private final MemberBlockService memberBlockService;

    @Operation(
        summary = "회원 차단",
        description = """
            특정 회원을 차단합니다.
            
            **제약**:
            - 자기 자신 차단 불가
            - 이미 차단한 대상 재요청 시 400 에러
            
            **성공**: 201 Created
            """,
        security = @SecurityRequirement(name = "JWT")
    )
    @PostMapping
    public ResponseEntity<Void> block(@Valid @RequestBody BlockRequest request, Authentication authentication) {
        Long memberId = AuthenticationUtils.extractMemberId(authentication);
        memberBlockService.block(memberId, request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Operation(
        summary = "차단 해제",
        description = """
            차단한 회원을 차단 해제합니다.
            
            **성공**: 204 No Content
            """,
        security = @SecurityRequirement(name = "JWT")
    )
    @DeleteMapping("/{blockedId}")
    public ResponseEntity<Void> unblock(@PathVariable("blockedId") Long blockedId, Authentication authentication) {
        Long memberId = AuthenticationUtils.extractMemberId(authentication);
        memberBlockService.unblock(memberId, blockedId);
        return ResponseEntity.noContent().build();
    }

    @Operation(
        summary = "차단 목록 조회",
        description = """
            내가 차단한 회원 목록을 반환합니다.
            
            **정렬**: 최신순 (createdAt DESC)
            """,
        security = @SecurityRequirement(name = "JWT")
    )
    @GetMapping
    public ResponseEntity<List<BlockListResponse>> list(Authentication authentication) {
        Long memberId = AuthenticationUtils.extractMemberId(authentication);
        return ResponseEntity.ok(memberBlockService.list(memberId));
    }
}
