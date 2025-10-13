package com.capstone.web.member.controller;

import com.capstone.web.auth.jwt.JwtAuthenticationFilter.MemberPrincipal;
import com.capstone.web.member.domain.Member;
import com.capstone.web.member.dto.MemberProfileResponse;
import com.capstone.web.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.capstone.web.member.service.MemberUpdateService;

@RestController
@RequestMapping("/api/v1/members")
@RequiredArgsConstructor
public class MemberQueryController {

    private final MemberRepository memberRepository;
    private final MemberUpdateService memberUpdateService;

    @GetMapping("/me")
    public ResponseEntity<MemberProfileResponse> me(Authentication authentication) {
        MemberPrincipal principal = (MemberPrincipal) authentication.getPrincipal();
        Member member = memberRepository.findById(principal.id()).orElseThrow();
    MemberProfileResponse response = new MemberProfileResponse(
        member.getId(),
        member.getEmail(),
        member.getNickname(),
        member.getRole(),
        member.getProfile(),
        member.getExportScore(),
        member.getRepresentativeBadgeId(),
        member.getJoinedAt(),
        member.getLastLoginAt());
        return ResponseEntity.ok(response);
    }

    @PatchMapping(value = "/me", consumes = {"multipart/form-data"})
    public ResponseEntity<MemberProfileResponse> updateMe(Authentication authentication,
                                                         @RequestPart(required = false) String nickname,
                                                         @RequestPart(required = false) MultipartFile profileImage) {
        MemberPrincipal principal = (MemberPrincipal) authentication.getPrincipal();
        Member member = memberRepository.findById(principal.id()).orElseThrow();
        MemberProfileResponse updated = memberUpdateService.update(member, nickname, profileImage);
        return ResponseEntity.ok(updated);
    }
}
