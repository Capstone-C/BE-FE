package com.capstone.web.auth.exception;

import org.springframework.http.HttpStatus;

public enum AuthErrorCode {
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "AUTH_INVALID_CREDENTIALS", "이메일 또는 비밀번호가 일치하지 않습니다.", "email"),
    WITHDRAWN_MEMBER(HttpStatus.FORBIDDEN, "AUTH_WITHDRAWN_MEMBER", "탈퇴한 회원이거나 이용이 정지된 계정입니다.", "email");

    private final HttpStatus status;
    private final String code;
    private final String message;
    private final String field;

    AuthErrorCode(HttpStatus status, String code, String message, String field) {
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
