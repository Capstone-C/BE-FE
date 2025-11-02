package com.capstone.web.diary.exception;

import org.springframework.http.HttpStatus;

public enum DiaryErrorCode {
    DIARY_NOT_FOUND(HttpStatus.NOT_FOUND, "DIARY_NOT_FOUND", "식단 기록을 찾을 수 없습니다.", "diaryId"),
    DUPLICATE_DIARY_ENTRY(HttpStatus.CONFLICT, "DUPLICATE_DIARY_ENTRY", "해당 날짜, 해당 식사 시간에 이미 기록이 존재합니다.", "date, mealType"),
    UNAUTHORIZED_DIARY_ACCESS(HttpStatus.FORBIDDEN, "UNAUTHORIZED_DIARY_ACCESS", "식단 기록에 대한 권한이 없습니다.", "diaryId");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
    private final String fieldName;

    DiaryErrorCode(HttpStatus httpStatus, String code, String message, String fieldName) {
        this.httpStatus = httpStatus;
        this.code = code;
        this.message = message;
        this.fieldName = fieldName;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public String getFieldName() {
        return fieldName;
    }
}
