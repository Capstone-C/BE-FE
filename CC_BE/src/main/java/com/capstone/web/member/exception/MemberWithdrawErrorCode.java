package com.capstone.web.member.exception;

import org.springframework.http.HttpStatus;

public enum MemberWithdrawErrorCode {
    INVALID_PASSWORD(HttpStatus.BAD_REQUEST, "MEMBER_WITHDRAW_INVALID_PASSWORD", "비밀번호가 일치하지 않습니다.", "password");

    private final HttpStatus status;
    private final String code;
    private final String message;
    private final String field;

    MemberWithdrawErrorCode(HttpStatus status, String code, String message, String field) {
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
