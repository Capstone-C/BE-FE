package com.capstone.web.diary.exception;

public class UnauthorizedDiaryAccessException extends RuntimeException {

    private final DiaryErrorCode errorCode;

    public UnauthorizedDiaryAccessException() {
        super(DiaryErrorCode.UNAUTHORIZED_DIARY_ACCESS.getMessage());
        this.errorCode = DiaryErrorCode.UNAUTHORIZED_DIARY_ACCESS;
    }

    public DiaryErrorCode getErrorCode() {
        return errorCode;
    }
}
