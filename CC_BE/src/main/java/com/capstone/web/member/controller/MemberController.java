package com.capstone.web.member.controller;


import com.capstone.web.member.dto.MemberRegisterRequest;
import com.capstone.web.member.dto.MemberRegisterResponse;
import com.capstone.web.member.dto.MemberPasswordChangeRequest;
import com.capstone.web.member.service.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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

@Tag(name = "Member", description = "회원 관리 API")
@RestController
@RequestMapping("/api/v1/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @Operation(
        summary = "회원가입",
        description = """
            새로운 회원을 등록합니다.
            
            **필수 정보**:
            - username: 로그인 ID
            - password: 비밀번호 (최소 8자, 영문+숫자+특수문자)
            - nickname: 닉네임
            - email: 이메일
            
            **응답**:
            - memberId: 생성된 회원 ID
            """
    )
    @PostMapping
    public ResponseEntity<MemberRegisterResponse> register(@Valid @RequestBody MemberRegisterRequest request) {
        MemberRegisterResponse response = memberService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(
        summary = "비밀번호 변경",
        description = """
            로그인한 회원의 비밀번호를 변경합니다.
            
            **정책**:
            - 현재 비밀번호 확인 필수
            - 최소 8자, 영문+숫자+특수문자 조합
            - 최근 5회 사용한 비밀번호 재사용 금지
            - 현재 비밀번호와 동일하게 변경 불가
            
            **성공**: 204 No Content
            """,
        security = @SecurityRequirement(name = "JWT")
    )
    @PatchMapping("/password")
    public ResponseEntity<Void> changePassword(Authentication authentication,
                                              @Valid @RequestBody MemberPasswordChangeRequest request) {
        Long memberId = ((com.capstone.web.auth.jwt.JwtAuthenticationFilter.MemberPrincipal) authentication.getPrincipal()).id();
        memberService.changePassword(memberId, request);
        return ResponseEntity.noContent().build();
    }
}
