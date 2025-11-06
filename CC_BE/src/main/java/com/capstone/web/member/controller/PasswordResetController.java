package com.capstone.web.member.controller;

import com.capstone.web.member.dto.PasswordResetRequest;
import com.capstone.web.member.dto.PasswordResetConfirmRequest;
import com.capstone.web.member.service.PasswordResetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Auth", description = "인증 관련 API")
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class PasswordResetController {
    private final PasswordResetService passwordResetService;

    @Operation(
        summary = "비밀번호 재설정 요청",
        description = """
            이메일을 입력하면 비밀번호 재설정 토큰이 발급됩니다.
            
            **개발 모드**:
            - 현재 메일 전송 미구현
            - 발급된 토큰은 서버 로그에서 확인: `[PASSWORD-RESET] token=... email=...`
            
            **실서비스 예정**:
            - 이 토큰을 포함한 링크를 이메일로 전송
            - 프론트엔드에서 재설정 화면으로 이동
            
            **성공**: 204 No Content
            """
    )
    @PostMapping("/password-reset")
    public ResponseEntity<Void> requestReset(@RequestBody @Valid PasswordResetRequest request) {
        passwordResetService.requestReset(request);
        return ResponseEntity.noContent().build();
    }

    @Operation(
        summary = "비밀번호 재설정 확인",
        description = """
            로그에서 확인한 토큰과 새 비밀번호를 제출하여 비밀번호를 재설정합니다.
            
            **토큰 정책**:
            - 1회만 사용 가능
            - 30분 후 만료
            - 이미 사용/무효/만료된 토큰은 에러 반환
            
            **성공**: 204 No Content
            """
    )
    @PostMapping("/password-reset/confirm")
    public ResponseEntity<Void> confirmReset(@RequestBody @Valid PasswordResetConfirmRequest request) {
        passwordResetService.confirmReset(request);
        return ResponseEntity.noContent().build();
    }
}
