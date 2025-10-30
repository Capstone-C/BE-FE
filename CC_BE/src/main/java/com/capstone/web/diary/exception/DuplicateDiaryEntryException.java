package com.capstone.web.diary.exception;

public class DuplicateDiaryEntryException extends RuntimeException {

    private final DiaryErrorCode errorCode;

    public DuplicateDiaryEntryException() {
        super(DiaryErrorCode.DUPLICATE_DIARY_ENTRY.getMessage());
        this.errorCode = DiaryErrorCode.DUPLICATE_DIARY_ENTRY;
    }

    public DiaryErrorCode getErrorCode() {
        return errorCode;
    }
}
