package com.capstone.web.auth.exception;

public class InvalidCredentialsException extends RuntimeException {

    private final AuthErrorCode errorCode;

    public InvalidCredentialsException() {
        super(AuthErrorCode.INVALID_CREDENTIALS.message());
        this.errorCode = AuthErrorCode.INVALID_CREDENTIALS;
    }

    public AuthErrorCode getErrorCode() {
        return errorCode;
    }
}
