package com.capstone.web.member.exception;

public class InvalidProfileImageTypeException extends RuntimeException {
    private final MemberErrorCode errorCode;

    public InvalidProfileImageTypeException() {
        super(MemberErrorCode.PROFILE_INVALID_TYPE.message());
        this.errorCode = MemberErrorCode.PROFILE_INVALID_TYPE;
    }

    public MemberErrorCode getErrorCode() {
        return errorCode;
    }
}
