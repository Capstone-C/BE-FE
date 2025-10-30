package com.capstone.web.diary.exception;

public class DiaryNotFoundException extends RuntimeException {

    private final DiaryErrorCode errorCode;

    public DiaryNotFoundException() {
        super(DiaryErrorCode.DIARY_NOT_FOUND.getMessage());
        this.errorCode = DiaryErrorCode.DIARY_NOT_FOUND;
    }

    public DiaryErrorCode getErrorCode() {
        return errorCode;
    }
}
