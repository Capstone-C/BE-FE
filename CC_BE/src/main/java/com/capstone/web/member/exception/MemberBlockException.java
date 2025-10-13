package com.capstone.web.member.exception;

public class MemberBlockException extends RuntimeException {
    private final MemberBlockErrorCode errorCode;

    public MemberBlockException(MemberBlockErrorCode errorCode) {
        super(errorCode.name());
        this.errorCode = errorCode;
    }

    public MemberBlockErrorCode getErrorCode() {
        return errorCode;
    }
}
