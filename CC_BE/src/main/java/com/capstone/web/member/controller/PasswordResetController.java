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

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class PasswordResetController {
    private final PasswordResetService passwordResetService;

    @PostMapping("/password-reset")
    public ResponseEntity<Void> requestReset(@RequestBody @Valid PasswordResetRequest request) {
        passwordResetService.requestReset(request);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/password-reset/confirm")
    public ResponseEntity<Void> confirmReset(@RequestBody @Valid PasswordResetConfirmRequest request) {
        passwordResetService.confirmReset(request);
        return ResponseEntity.noContent().build();
    }
}
