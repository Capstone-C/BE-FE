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

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class PasswordResetController {
    private final PasswordResetService passwordResetService;

    @Operation(summary = "비밀번호 재설정 요청", description = "이메일을 입력하면 비밀번호 재설정 토큰이 발급됩니다.\n현재는 메일 전송이 구현되지 않았으며, 발급된 토큰은 서버 로그(`[PASSWORD-RESET] token=... email=...`)로 확인하는 개발용(stub) 상태입니다.\n실서비스에서는 이 토큰을 포함한 링크를 이메일로 보내 프론트엔드에서 재설정 화면으로 이동시키게 됩니다.")
    @PostMapping("/password-reset")
    public ResponseEntity<Void> requestReset(@RequestBody @Valid PasswordResetRequest request) {
        passwordResetService.requestReset(request);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "비밀번호 재설정 확인", description = "로그에서 확인한(또는 실제 서비스에서는 이메일로 받은) 토큰과 새 비밀번호를 제출하여 비밀번호를 재설정합니다.\n토큰은 1회만 사용 가능하며 30분 후 만료됩니다. 이미 사용/무효/만료된 토큰은 에러를 반환합니다.")
    @PostMapping("/password-reset/confirm")
    public ResponseEntity<Void> confirmReset(@RequestBody @Valid PasswordResetConfirmRequest request) {
        passwordResetService.confirmReset(request);
        return ResponseEntity.noContent().build();
    }
}
