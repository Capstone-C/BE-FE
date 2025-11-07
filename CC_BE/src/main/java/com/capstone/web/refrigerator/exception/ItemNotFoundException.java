package com.capstone.web.refrigerator.exception;

public class ItemNotFoundException extends RuntimeException {
    public ItemNotFoundException() {
        super(RefrigeratorErrorCode.ITEM_NOT_FOUND.getMessage());
    }

    public ItemNotFoundException(String message) {
        super(message);
    }
}
