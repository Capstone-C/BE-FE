package com.capstone.web.member.exception;

public class InvalidWithdrawPasswordException extends RuntimeException {
    private final MemberWithdrawErrorCode errorCode;

    public InvalidWithdrawPasswordException() {
        super(MemberWithdrawErrorCode.INVALID_PASSWORD.message());
        this.errorCode = MemberWithdrawErrorCode.INVALID_PASSWORD;
    }

    public MemberWithdrawErrorCode getErrorCode() {
        return errorCode;
    }
}
