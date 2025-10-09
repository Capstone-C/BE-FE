package com.capstone.web.member.exception;

public class DuplicateEmailException extends RuntimeException {

    private final MemberErrorCode errorCode;

    public DuplicateEmailException() {
        super(MemberErrorCode.DUPLICATE_EMAIL.message());
        this.errorCode = MemberErrorCode.DUPLICATE_EMAIL;
    }

    public MemberErrorCode getErrorCode() {
        return errorCode;
    }
}
