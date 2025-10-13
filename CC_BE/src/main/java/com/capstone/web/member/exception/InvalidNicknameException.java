package com.capstone.web.member.exception;

public class InvalidNicknameException extends RuntimeException {
    private final MemberErrorCode errorCode;

    public InvalidNicknameException() {
        super(MemberErrorCode.INVALID_NICKNAME.message());
        this.errorCode = MemberErrorCode.INVALID_NICKNAME;
    }

    public MemberErrorCode getErrorCode() {
        return errorCode;
    }
}
