package com.capstone.web.member.exception;

import org.springframework.http.HttpStatus;

public enum PasswordResetErrorCode {
    EMAIL_NOT_FOUND(HttpStatus.NOT_FOUND, "PASSWORD_RESET_EMAIL_NOT_FOUND", "해당 이메일의 회원이 존재하지 않습니다.", "email"),
    TOKEN_NOT_FOUND_OR_EXPIRED(HttpStatus.NOT_FOUND, "PASSWORD_RESET_TOKEN_NOT_FOUND_OR_EXPIRED", "비밀번호 재설정 토큰이 없거나 만료되었습니다.", "token"),
    TOKEN_ALREADY_USED_OR_INVALIDATED(HttpStatus.BAD_REQUEST, "PASSWORD_RESET_TOKEN_ALREADY_USED_OR_INVALIDATED", "이미 사용되었거나 무효화된 토큰입니다.", "token"),
    SAME_AS_OLD_PASSWORD(HttpStatus.BAD_REQUEST, "PASSWORD_RESET_SAME_AS_OLD_PASSWORD", "새 비밀번호가 기존 비밀번호와 동일합니다.", "newPassword"),
    PASSWORD_CONFIRM_MISMATCH(HttpStatus.BAD_REQUEST, "PASSWORD_RESET_CONFIRM_MISMATCH", "비밀번호 확인이 일치하지 않습니다.", "newPasswordConfirm");

    private final HttpStatus status;
    private final String code;
    private final String message;
    private final String field;

    PasswordResetErrorCode(HttpStatus status, String code, String message, String field) {
        this.status = status;
        this.code = code;
        this.message = message;
        this.field = field;
    }

    public HttpStatus status() { return status; }
    public String code() { return code; }
    public String message() { return message; }
    public String field() { return field; }
}
