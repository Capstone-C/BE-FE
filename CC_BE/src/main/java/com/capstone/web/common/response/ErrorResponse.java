package com.capstone.web.common.response;

import java.util.Collections;
import java.util.List;
import org.springframework.http.HttpStatus;

public record ErrorResponse(int status, String code, String message, List<FieldError> errors) {

    public record FieldError(String field, String message) {
    }

    public static ErrorResponse of(HttpStatus status, String code, String message) {
        return new ErrorResponse(status.value(), code, message, Collections.emptyList());
    }

    public static ErrorResponse of(HttpStatus status, String code, String message, List<FieldError> errors) {
        return new ErrorResponse(status.value(), code, message, errors);
    }
}
