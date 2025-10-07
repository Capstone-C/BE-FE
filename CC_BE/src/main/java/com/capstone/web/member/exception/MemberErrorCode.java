package com.capstone.web.member.exception;

import org.springframework.http.HttpStatus;

public enum MemberErrorCode {
    DUPLICATE_EMAIL(HttpStatus.CONFLICT, "MEMBER_DUPLICATE_EMAIL", "이미 사용 중인 이메일입니다.", "email"),
    DUPLICATE_NICKNAME(HttpStatus.CONFLICT, "MEMBER_DUPLICATE_NICKNAME", "이미 사용 중인 닉네임입니다.", "nickname");

    private final HttpStatus status;
    private final String code;
    private final String message;
    private final String field;

    MemberErrorCode(HttpStatus status, String code, String message, String field) {
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
