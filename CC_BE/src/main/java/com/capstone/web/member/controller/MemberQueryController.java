package com.capstone.web.member.controller;

import com.capstone.web.auth.jwt.JwtAuthenticationFilter.MemberPrincipal;
import com.capstone.web.member.domain.Member;
import com.capstone.web.member.dto.MemberProfileResponse;
import com.capstone.web.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/members")
@RequiredArgsConstructor
public class MemberQueryController {

    private final MemberRepository memberRepository;

    @GetMapping("/me")
    public ResponseEntity<MemberProfileResponse> me(Authentication authentication) {
        MemberPrincipal principal = (MemberPrincipal) authentication.getPrincipal();
        Member member = memberRepository.findById(principal.id()).orElseThrow();
        MemberProfileResponse response = new MemberProfileResponse(member.getId(), member.getEmail(), member.getNickname(), member.getRole(), member.getJoinedAt(), member.getLastLoginAt());
        return ResponseEntity.ok(response);
    }
}
