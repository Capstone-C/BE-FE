package com.capstone.web.member.exception;

public class InvalidOldPasswordException extends RuntimeException {
    private final PasswordChangeErrorCode errorCode;

    public InvalidOldPasswordException() {
        super(PasswordChangeErrorCode.INVALID_OLD_PASSWORD.message());
        this.errorCode = PasswordChangeErrorCode.INVALID_OLD_PASSWORD;
    }

    public PasswordChangeErrorCode getErrorCode() {
        return errorCode;
    }
}
