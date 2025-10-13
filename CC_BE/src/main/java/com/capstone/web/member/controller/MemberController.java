package com.capstone.web.member.controller;


import com.capstone.web.member.dto.MemberRegisterRequest;
import com.capstone.web.member.dto.MemberRegisterResponse;
import com.capstone.web.member.dto.MemberPasswordChangeRequest;
import com.capstone.web.member.service.MemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @PatchMapping("/password")
    public ResponseEntity<Void> changePassword(Authentication authentication,
                                              @Valid @RequestBody MemberPasswordChangeRequest request) {
        Long memberId = ((com.capstone.web.auth.jwt.JwtAuthenticationFilter.MemberPrincipal) authentication.getPrincipal()).id();
        memberService.changePassword(memberId, request);
        return ResponseEntity.noContent().build();
    }

    @PostMapping
    public ResponseEntity<MemberRegisterResponse> register(@Valid @RequestBody MemberRegisterRequest request) {
        MemberRegisterResponse response = memberService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
