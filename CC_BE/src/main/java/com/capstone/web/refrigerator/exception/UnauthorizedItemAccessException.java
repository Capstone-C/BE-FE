package com.capstone.web.refrigerator.exception;

public class UnauthorizedItemAccessException extends RuntimeException {
    public UnauthorizedItemAccessException() {
        super(RefrigeratorErrorCode.UNAUTHORIZED_ITEM_ACCESS.getMessage());
    }

    public UnauthorizedItemAccessException(String message) {
        super(message);
    }
}
