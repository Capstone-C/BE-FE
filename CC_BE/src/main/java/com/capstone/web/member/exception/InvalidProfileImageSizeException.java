package com.capstone.web.member.exception;

public class InvalidProfileImageSizeException extends RuntimeException {
    private final MemberErrorCode errorCode;

    public InvalidProfileImageSizeException() {
        super(MemberErrorCode.PROFILE_INVALID_SIZE.message());
        this.errorCode = MemberErrorCode.PROFILE_INVALID_SIZE;
    }

    public MemberErrorCode getErrorCode() {
        return errorCode;
    }
}
