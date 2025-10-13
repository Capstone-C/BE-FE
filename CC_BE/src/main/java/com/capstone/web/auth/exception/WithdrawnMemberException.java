package com.capstone.web.auth.exception;

public class WithdrawnMemberException extends RuntimeException {

    private final AuthErrorCode errorCode;

    public WithdrawnMemberException() {
        super(AuthErrorCode.WITHDRAWN_MEMBER.message());
        this.errorCode = AuthErrorCode.WITHDRAWN_MEMBER;
    }

    public AuthErrorCode getErrorCode() {
        return errorCode;
    }
}
