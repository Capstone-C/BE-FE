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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@RestController
@RequestMapping("/api/v1/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @Operation(summary = "비밀번호 변경", description = "로그인한 회원이 비밀번호를 변경합니다.\n정책: 최소 길이/문자 조합/최근 N회(5) 사용 비밀번호 재사용 금지/동일 비밀번호 금지. 위반시 400 응답.\n성공 시 204(No Content) 반환.", security = @SecurityRequirement(name = "JWT"))
    @PatchMapping("/password")
    public ResponseEntity<Void> changePassword(Authentication authentication,
                                              @Valid @RequestBody MemberPasswordChangeRequest request) {
        Long memberId = ((com.capstone.web.auth.jwt.JwtAuthenticationFilter.MemberPrincipal) authentication.getPrincipal()).id();
        memberService.changePassword(memberId, request);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "회원가입", description = "새로운 회원을 등록합니다. 이메일, 닉네임, 비밀번호를 입력해야 합니다.")
    @PostMapping
    public ResponseEntity<MemberRegisterResponse> register(@Valid @RequestBody MemberRegisterRequest request) {
        MemberRegisterResponse response = memberService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
