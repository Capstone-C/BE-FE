package com.capstone.web.member.exception;

public class RecentPasswordReuseException extends RuntimeException {
    private final PasswordChangeErrorCode errorCode;

    public RecentPasswordReuseException() {
        super(PasswordChangeErrorCode.RECENT_PASSWORD_REUSE.message());
        this.errorCode = PasswordChangeErrorCode.RECENT_PASSWORD_REUSE;
    }

    public PasswordChangeErrorCode getErrorCode() {
        return errorCode;
    }
}
