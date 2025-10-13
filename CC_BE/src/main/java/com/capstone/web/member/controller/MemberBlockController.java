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

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/members/blocks")
public class MemberBlockController {
    private final MemberBlockService memberBlockService;

    // POST /api/v1/members/blocks
    @PostMapping
    public ResponseEntity<Void> block(@Valid @RequestBody BlockRequest request, Authentication authentication) {
        Long memberId = extractMemberId(authentication);
        memberBlockService.block(memberId, request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    // DELETE /api/v1/members/blocks/{blockedId}
    @DeleteMapping("/{blockedId}")
    public ResponseEntity<Void> unblock(@PathVariable("blockedId") Long blockedId, Authentication authentication) {
        Long memberId = extractMemberId(authentication);
        memberBlockService.unblock(memberId, blockedId);
        return ResponseEntity.noContent().build();
    }

    // GET /api/v1/members/blocks
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
