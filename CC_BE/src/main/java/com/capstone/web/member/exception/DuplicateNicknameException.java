package com.capstone.web.member.exception;

public class DuplicateNicknameException extends RuntimeException {

    private final MemberErrorCode errorCode;

    public DuplicateNicknameException() {
        super(MemberErrorCode.DUPLICATE_NICKNAME.message());
        this.errorCode = MemberErrorCode.DUPLICATE_NICKNAME;
    }

    public MemberErrorCode getErrorCode() {
        return errorCode;
    }
}
