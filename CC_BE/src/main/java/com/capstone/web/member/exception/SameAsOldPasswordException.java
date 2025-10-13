package com.capstone.web.member.exception;

public class SameAsOldPasswordException extends RuntimeException {
    private final PasswordChangeErrorCode errorCode;

    public SameAsOldPasswordException() {
        super(PasswordChangeErrorCode.SAME_AS_OLD_PASSWORD.message());
        this.errorCode = PasswordChangeErrorCode.SAME_AS_OLD_PASSWORD;
    }

    public PasswordChangeErrorCode getErrorCode() {
        return errorCode;
    }
}
