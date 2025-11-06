package com.capstone.web.refrigerator.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * ReceiptPreprocessor 단위 테스트
 */
@DisplayName("영수증 텍스트 전처리 테스트")
class ReceiptPreprocessorTest {

    private final ReceiptPreprocessor preprocessor = new ReceiptPreprocessor();

    @Test
    @DisplayName("빈 줄 제거")
    void removeEmptyLines() {
        // given
        String input = "CU 편의점\n\n\n2025-01-15\n\n총액";

        // when
        String cleaned = preprocessor.clean(input);

        // then
        assertThat(cleaned).doesNotContain("\n\n");
        assertThat(cleaned.split("\n")).hasSize(3);
    }

    @Test
    @DisplayName("광고/프로모션 줄 제거")
    void removeAdvertisements() {
        // given
        String input = """
                CU 편의점
                쿠폰 사용 가능
                포인트 적립 안내
                바나나 1,500원
                우유 2,000원
                총액 3,500원
                """;

        // when
        String cleaned = preprocessor.clean(input);

        // then
        assertThat(cleaned).doesNotContain("쿠폰");
        assertThat(cleaned).doesNotContain("포인트");
        assertThat(cleaned).contains("바나나");
        assertThat(cleaned).contains("우유");
    }

    @Test
    @DisplayName("감사 인사 제거")
    void removeThankYouMessages() {
        // given
        String input = """
                CU 편의점
                바나나 1,500원
                감사합니다
                Thank you
                """;

        // when
        String cleaned = preprocessor.clean(input);

        // then
        assertThat(cleaned).doesNotContain("감사합니다");
        assertThat(cleaned).doesNotContain("Thank you");
        assertThat(cleaned).contains("바나나");
    }

    @Test
    @DisplayName("바코드/긴 숫자 줄 제거")
    void removeBarcodes() {
        // given
        String input = """
                CU 편의점
                8801234567890
                바나나 1,500원
                1234567890123456
                총액 1,500원
                """;

        // when
        String cleaned = preprocessor.clean(input);

        // then
        assertThat(cleaned).doesNotContain("8801234567890");
        assertThat(cleaned).doesNotContain("1234567890123456");
        assertThat(cleaned).contains("바나나");
        assertThat(cleaned).contains("총액");
    }

    @Test
    @DisplayName("연락처/URL 제거")
    void removeContactInfo() {
        // given
        String input = """
                CU 편의점
                www.cu.co.kr
                Tel: 02-1234-5678
                바나나 1,500원
                문의: 1234-5678
                """;

        // when
        String cleaned = preprocessor.clean(input);

        // then
        assertThat(cleaned).doesNotContain("www");
        assertThat(cleaned).doesNotContain("Tel");
        assertThat(cleaned).doesNotContain("문의");
        assertThat(cleaned).contains("바나나");
    }

    @Test
    @DisplayName("실제 영수증 전처리 (통합)")
    void realReceiptPreprocessing() {
        // given
        String input = """
                CU 강남점
                www.cu.co.kr
                Tel: 02-1234-5678
                ================================
                2025-01-15 14:30:25
                
                바나나        1,500원
                우유          2,000원
                빵            1,000원
                
                바코드: 8801234567890
                포인트 적립 가능
                쿠폰 사용 안내
                
                총액          4,500원
                
                감사합니다
                Thank you
                방문해 주셔서 감사합니다
                """;

        // when
        String cleaned = preprocessor.clean(input);

        // then
        // 필요한 정보만 남아있는지 확인
        assertThat(cleaned).contains("CU 강남점");
        assertThat(cleaned).contains("2025-01-15");
        assertThat(cleaned).contains("바나나");
        assertThat(cleaned).contains("우유");
        assertThat(cleaned).contains("빵");
        assertThat(cleaned).contains("4,500");

        // 불필요한 정보가 제거되었는지 확인
        assertThat(cleaned).doesNotContain("www");
        assertThat(cleaned).doesNotContain("Tel");
        assertThat(cleaned).doesNotContain("바코드");
        assertThat(cleaned).doesNotContain("포인트");
        assertThat(cleaned).doesNotContain("쿠폰");
        assertThat(cleaned).doesNotContain("감사합니다");
        assertThat(cleaned).doesNotContain("Thank you");
        assertThat(cleaned).doesNotContain("방문");

        // 텍스트가 줄어들었는지 확인 (토큰 절감)
        assertThat(cleaned.length()).isLessThan(input.length());
    }

    @Test
    @DisplayName("null 또는 빈 문자열 처리")
    void handleNullOrEmpty() {
        // when & then
        assertThat(preprocessor.clean(null)).isEmpty();
        assertThat(preprocessor.clean("")).isEmpty();
        assertThat(preprocessor.clean("   ")).isEmpty();
    }
}
