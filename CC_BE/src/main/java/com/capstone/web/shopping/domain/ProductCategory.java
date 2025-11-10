package com.capstone.web.shopping.domain;

/**
 * 상품 카테고리
 * 식재료 종류를 분류하는 카테고리
 */
public enum ProductCategory {
    VEGETABLES("채소"),
    FRUITS("과일"),
    MEAT("육류"),
    SEAFOOD("수산물"),
    DAIRY("유제품"),
    GRAINS("곡물"),
    SNACKS("간식"),
    BEVERAGES("음료"),
    SEASONINGS("조미료/양념"),
    PROCESSED("가공식품"),
    ETC("기타");

    private final String displayName;

    ProductCategory(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
