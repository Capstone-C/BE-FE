package com.capstone.web.refrigerator.exception;

public class DuplicateItemException extends RuntimeException {
    public DuplicateItemException() {
        super(RefrigeratorErrorCode.DUPLICATE_ITEM.getMessage());
    }

    public DuplicateItemException(String message) {
        super(message);
    }
}
