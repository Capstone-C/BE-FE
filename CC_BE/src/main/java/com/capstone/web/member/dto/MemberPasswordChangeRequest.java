package com.capstone.web.member.dto;

import com.capstone.web.member.validation.PasswordMatches;
import com.capstone.web.member.validation.PasswordPolicy;
import jakarta.validation.constraints.NotBlank;

@PasswordMatches
public record MemberPasswordChangeRequest(
        @NotBlank(message = "기존 비밀번호를 입력해주세요.")
        String oldPassword,

        @NotBlank(message = "새 비밀번호를 입력해주세요.")
        @PasswordPolicy
        String newPassword,

        @NotBlank(message = "새 비밀번호를 다시 입력해주세요.")
        String newPasswordConfirm
) {
}
