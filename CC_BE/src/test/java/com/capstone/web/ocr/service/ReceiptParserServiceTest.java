package com.capstone.web.ocr.service;

import com.capstone.web.ocr.dto.OcrDto.ParsedItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 영수증 파서 서비스 테스트
 * Spring Context 없이 순수 단위 테스트로 작성
 */
@DisplayName("영수증 파서 서비스 테스트")
class ReceiptParserServiceTest {

    private ReceiptParserService receiptParserService;

    @BeforeEach
    void setUp() {
        receiptParserService = new ReceiptParserService();
    }

    @Test
    @DisplayName("영수증 텍스트 파싱 성공 - 기본 케이스")
    void parseReceipt_Success_Basic() {
        // given
        String receiptText = """
            마트 영수증
            -----------------
            사과 2개 5,000원
            바나나 1봉 3,000원
            우유 1리터 2,500원
            -----------------
            합계 10,500원
            """;

        // when
        List<ParsedItem> items = receiptParserService.parseReceipt(receiptText);

        // then
        assertThat(items).isNotEmpty();
        assertThat(items).hasSizeGreaterThanOrEqualTo(3);
        
        // 사과 확인
        assertThat(items).anyMatch(item -> 
            item.getName().contains("사과") && 
            item.getQuantity() != null && 
            item.getQuantity() == 2
        );
        
        // 바나나 확인
        assertThat(items).anyMatch(item -> 
            item.getName().contains("바나나")
        );
        
        // 우유 확인
        assertThat(items).anyMatch(item -> 
            item.getName().contains("우유")
        );
    }

    @Test
    @DisplayName("영수증 텍스트 파싱 성공 - 다양한 단위")
    void parseReceipt_Success_VariousUnits() {
        // given
        String receiptText = """
            김치 1kg 8,000원
            라면 5개 4,500원
            물 2리터 1,500원
            계란 1팩 5,000원
            """;

        // when
        List<ParsedItem> items = receiptParserService.parseReceipt(receiptText);

        // then
        assertThat(items).isNotEmpty();
        
        // kg 단위 확인
        assertThat(items).anyMatch(item -> 
            item.getName().contains("김치") && 
            "kg".equals(item.getUnit())
        );
        
        // 개 단위 확인
        assertThat(items).anyMatch(item -> 
            item.getName().contains("라면") && 
            "개".equals(item.getUnit())
        );
    }

    @Test
    @DisplayName("영수증 텍스트 파싱 - 무시할 키워드 제외")
    void parseReceipt_IgnoreKeywords() {
        // given
        String receiptText = """
            ABC마트 홍대점
            전화: 02-123-4567
            주소: 서울시 마포구
            -----------------
            사과 2개 5,000원
            -----------------
            합계 5,000원
            카드결제 5,000원
            감사합니다
            """;

        // when
        List<ParsedItem> items = receiptParserService.parseReceipt(receiptText);

        // then
        assertThat(items).isNotEmpty();
        
        // 무시할 키워드가 포함된 항목이 파싱되지 않았는지 확인
        assertThat(items).noneMatch(item -> 
            item.getName().contains("합계") ||
            item.getName().contains("카드") ||
            item.getName().contains("마트") ||
            item.getName().contains("전화")
        );
        
        // 실제 상품만 파싱되었는지 확인
        assertThat(items).anyMatch(item -> item.getName().contains("사과"));
    }

    @Test
    @DisplayName("영수증 텍스트 파싱 - 빈 텍스트")
    void parseReceipt_EmptyText() {
        // given
        String receiptText = "";

        // when
        List<ParsedItem> items = receiptParserService.parseReceipt(receiptText);

        // then
        assertThat(items).isEmpty();
    }

    @Test
    @DisplayName("영수증 텍스트 파싱 - null 텍스트")
    void parseReceipt_NullText() {
        // given
        String receiptText = null;

        // when
        List<ParsedItem> items = receiptParserService.parseReceipt(receiptText);

        // then
        assertThat(items).isEmpty();
    }

    @Test
    @DisplayName("영수증 텍스트 파싱 - 가격만 있는 줄 무시")
    void parseReceipt_IgnorePriceOnlyLines() {
        // given
        String receiptText = """
            사과 5,000원
            5,000
            3,000
            바나나 3,000원
            """;

        // when
        List<ParsedItem> items = receiptParserService.parseReceipt(receiptText);

        // then
        assertThat(items).isNotEmpty();
        
        // 실제 상품명이 있는 항목만 파싱되었는지 확인
        assertThat(items).allMatch(item -> 
            item.getName() != null && !item.getName().isEmpty()
        );
    }

    @Test
    @DisplayName("영수증 텍스트 파싱 - 특수문자 제거")
    void parseReceipt_RemoveSpecialCharacters() {
        // given
        String receiptText = """
            *사과* 2개 5,000원
            [바나나] 1봉 3,000원
            #우유# 1리터 2,500원
            """;

        // when
        List<ParsedItem> items = receiptParserService.parseReceipt(receiptText);

        // then
        assertThat(items).isNotEmpty();
        
        // 특수문자가 제거되었는지 확인
        assertThat(items).allMatch(item -> 
            !item.getName().contains("*") &&
            !item.getName().contains("[") &&
            !item.getName().contains("]") &&
            !item.getName().contains("#")
        );
    }
}
