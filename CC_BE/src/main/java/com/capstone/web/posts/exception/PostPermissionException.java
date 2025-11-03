package com.capstone.web.posts.exception;

public class PostPermissionException extends RuntimeException {
    public PostPermissionException(String message) {
        super(message);
    }
}