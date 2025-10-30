package com.capstone.web.refrigerator.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum RefrigeratorErrorCode {
    ITEM_NOT_FOUND("ITEM_NOT_FOUND", "식재료를 찾을 수 없습니다"),
    DUPLICATE_ITEM("DUPLICATE_ITEM", "이미 등록된 식재료입니다"),
    UNAUTHORIZED_ITEM_ACCESS("UNAUTHORIZED_ITEM_ACCESS", "식재료 접근 권한이 없습니다");

    private final String code;
    private final String message;
}
