package com.capstone.web.member.controller;

import com.capstone.web.member.dto.BlockListResponse;
import com.capstone.web.member.dto.BlockRequest;
import com.capstone.web.member.service.MemberBlockService;
import com.capstone.web.auth.jwt.JwtAuthenticationFilter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/members/blocks")
public class MemberBlockController {
    private final MemberBlockService memberBlockService;

    // POST /api/v1/members/blocks
    @Operation(summary = "회원 차단", description = "특정 회원을 차단합니다.\n제약: 자기 자신 차단 불가, 이미 차단한 대상 재요청 시 400.\n중복/자기차단 로직은 서비스 단에서 검증.", security = @SecurityRequirement(name = "JWT"))
    @PostMapping
    public ResponseEntity<Void> block(@Valid @RequestBody BlockRequest request, Authentication authentication) {
        Long memberId = extractMemberId(authentication);
        memberBlockService.block(memberId, request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    // DELETE /api/v1/members/blocks/{blockedId}
    @Operation(summary = "차단 해제", description = "차단한 회원을 차단 해제합니다.", security = @SecurityRequirement(name = "JWT"))
    @DeleteMapping("/{blockedId}")
    public ResponseEntity<Void> unblock(@PathVariable("blockedId") Long blockedId, Authentication authentication) {
        Long memberId = extractMemberId(authentication);
        memberBlockService.unblock(memberId, blockedId);
        return ResponseEntity.noContent().build();
    }

    // GET /api/v1/members/blocks
    @Operation(summary = "차단 목록 조회", description = "내가 차단한 회원 목록을 최신순(createdAt DESC)으로 반환합니다.", security = @SecurityRequirement(name = "JWT"))
    @GetMapping
    public ResponseEntity<List<BlockListResponse>> list(Authentication authentication) {
        Long memberId = extractMemberId(authentication);
        return ResponseEntity.ok(memberBlockService.list(memberId));
    }

    private Long extractMemberId(Authentication authentication) {
        if (authentication == null) {
            throw new IllegalStateException("인증 정보가 없습니다.");
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof JwtAuthenticationFilter.MemberPrincipal mp) {
            return mp.id();
        }
        // MockMvc tests may supply a UsernamePasswordAuthenticationToken with principal as a String username
        String name = authentication.getName();
        try {
            return Long.valueOf(name);
        } catch (NumberFormatException e) {
            throw new IllegalStateException("인증 Principal 에서 회원 ID를 추출할 수 없습니다: " + name);
        }
    }
}
