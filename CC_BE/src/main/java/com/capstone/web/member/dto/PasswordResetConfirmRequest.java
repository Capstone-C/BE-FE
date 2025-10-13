package com.capstone.web.member.dto;

import jakarta.validation.constraints.NotBlank;

public record PasswordResetConfirmRequest(
        @NotBlank String token,
        @NotBlank String newPassword,
        @NotBlank String newPasswordConfirm
) {}
