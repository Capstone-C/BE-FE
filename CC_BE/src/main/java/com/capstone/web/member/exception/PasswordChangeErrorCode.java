package com.capstone.web.member.exception;

import org.springframework.http.HttpStatus;

public enum PasswordChangeErrorCode {
    INVALID_OLD_PASSWORD(HttpStatus.BAD_REQUEST, "MEMBER_INVALID_OLD_PASSWORD", "기존 비밀번호가 일치하지 않습니다.", "oldPassword"),
    SAME_AS_OLD_PASSWORD(HttpStatus.BAD_REQUEST, "MEMBER_SAME_AS_OLD_PASSWORD", "새 비밀번호가 기존 비밀번호와 동일합니다.", "newPassword");

    private final HttpStatus status;
    private final String code;
    private final String message;
    private final String field;

    PasswordChangeErrorCode(HttpStatus status, String code, String message, String field) {
        this.status = status;
        this.code = code;
        this.message = message;
        this.field = field;
    }

    public HttpStatus status() {
        return status;
    }
    public String code() {
        return code;
    }
    public String message() {
        return message;
    }
    public String field() {
        return field;
    }
}
