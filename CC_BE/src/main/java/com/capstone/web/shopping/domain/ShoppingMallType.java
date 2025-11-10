package com.capstone.web.shopping.domain;

/**
 * 쇼핑몰 타입
 * 외부 쇼핑몰 API 연동 시 사용되는 쇼핑몰 식별자
 */
public enum ShoppingMallType {
    NAVER("네이버 쇼핑", "naver"),
    COUPANG("쿠팡", "coupang"),
    GMARKET("G마켓", "gmarket"),
    ELEVENST("11번가", "11st");

    private final String displayName;
    private final String code;

    ShoppingMallType(String displayName, String code) {
        this.displayName = displayName;
        this.code = code;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getCode() {
        return code;
    }
}
