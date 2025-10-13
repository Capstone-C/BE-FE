package com.capstone.web.member.exception;

import org.springframework.http.HttpStatus;

public enum MemberErrorCode {
    DUPLICATE_EMAIL(HttpStatus.CONFLICT, "MEMBER_DUPLICATE_EMAIL", "이미 사용 중인 이메일입니다.", "email"),
    DUPLICATE_NICKNAME(HttpStatus.CONFLICT, "MEMBER_DUPLICATE_NICKNAME", "이미 사용 중인 닉네임입니다.", "nickname"),
    INVALID_NICKNAME(HttpStatus.BAD_REQUEST, "MEMBER_INVALID_NICKNAME", "닉네임은 2~10자의 한글/영문/숫자만 가능합니다.", "nickname"),
    PROFILE_INVALID_TYPE(HttpStatus.BAD_REQUEST, "MEMBER_PROFILE_INVALID_TYPE", "이미지 파일(jpg, png, gif)만 업로드할 수 있습니다.", "profileImage"),
    PROFILE_INVALID_SIZE(HttpStatus.BAD_REQUEST, "MEMBER_PROFILE_INVALID_SIZE", "파일 크기는 5MB를 초과할 수 없습니다.", "profileImage");

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
