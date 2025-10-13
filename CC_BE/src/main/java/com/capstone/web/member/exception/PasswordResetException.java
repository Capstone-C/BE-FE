package com.capstone.web.member.exception;

public class PasswordResetException extends RuntimeException {
    private final PasswordResetErrorCode errorCode;

    public PasswordResetException(PasswordResetErrorCode errorCode) {
        super(errorCode.message());
        this.errorCode = errorCode;
    }

    public PasswordResetErrorCode getErrorCode() { return errorCode; }
}
