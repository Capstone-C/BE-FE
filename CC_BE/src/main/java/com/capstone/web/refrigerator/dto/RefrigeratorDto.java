package com.capstone.web.refrigerator.dto;

import com.capstone.web.refrigerator.domain.RefrigeratorItem;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class RefrigeratorDto {

    /**
     * 식재료 추가 요청 DTO (REF-02)
     */
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateRequest {
        @NotBlank(message = "식재료명은 필수입니다")
        @Size(max = 50, message = "식재료명은 50자 이하이어야 합니다")
        private String name;

        @Min(value = 0, message = "수량은 0 이상이어야 합니다")
        private Integer quantity;

        @Size(max = 10, message = "용량/단위는 10자 이하이어야 합니다")
        private String unit;

        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate expirationDate;

        @Size(max = 200, message = "메모는 200자 이하이어야 합니다")
        private String memo;
    }

    /**
     * 식재료 수정 요청 DTO (REF-05)
     * 식재료명(name)은 수정 불가 (삭제 후 재등록 유도)
     */
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateRequest {
        @Min(value = 0, message = "수량은 0 이상이어야 합니다")
        private Integer quantity;

        @Size(max = 10, message = "용량/단위는 10자 이하이어야 합니다")
        private String unit;

        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate expirationDate;

        @Size(max = 200, message = "메모는 200자 이하이어야 합니다")
        private String memo;
    }

    /**
     * 식재료 응답 DTO (REF-01, 02, 05)
     */
    @Getter
    public static class Response {
        private final Long id;
        private final Long memberId;
        private final String name;
        private final Integer quantity;
        private final String unit;
        @JsonFormat(pattern = "yyyy-MM-dd")
        private final LocalDate expirationDate;
        private final String memo;
        private final Long daysUntilExpiration; // D-day
        private final boolean expirationSoon; // 소비기한 임박 (3일 이내)
        private final boolean expired; // 소비기한 경과
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        private final LocalDateTime createdAt;
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        private final LocalDateTime updatedAt;

        @Builder
        public Response(RefrigeratorItem item) {
            this.id = item.getId();
            this.memberId = item.getMember().getId();
            this.name = item.getName();
            this.quantity = item.getQuantity();
            this.unit = item.getUnit();
            this.expirationDate = item.getExpirationDate();
            this.memo = item.getMemo();
            this.daysUntilExpiration = item.getDaysUntilExpiration();
            this.expirationSoon = item.isExpirationSoon();
            this.expired = item.isExpired();
            this.createdAt = item.getCreatedAt();
            this.updatedAt = item.getUpdatedAt();
        }
    }

    /**
     * 식재료 목록 응답 DTO (REF-01)
     */
    @Getter
    public static class ItemListResponse {
        private final List<Response> items;
        private final int totalCount;
        private final int expiringCount; // 소비기한 임박 항목 수
        private final int expiredCount; // 소비기한 경과 항목 수

        @Builder
        public ItemListResponse(List<RefrigeratorItem> items) {
            this.items = items.stream()
                    .map(Response::new)
                    .collect(Collectors.toList());
            this.totalCount = items.size();
            this.expiringCount = (int) items.stream()
                    .filter(RefrigeratorItem::isExpirationSoon)
                    .count();
            this.expiredCount = (int) items.stream()
                    .filter(RefrigeratorItem::isExpired)
                    .count();
        }
    }

    /**
     * 일괄 추가 요청 DTO (REF-03, 04: OCR 결과 일괄 등록)
     */
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BulkCreateRequest {
        @NotNull(message = "식재료 목록은 필수입니다")
        @Size(min = 1, message = "최소 1개 이상의 식재료가 필요합니다")
        private List<CreateRequest> items;
    }

    /**
     * 일괄 추가 응답 DTO
     */
    @Getter
    public static class BulkCreateResponse {
        private final int successCount;
        private final int failCount;
        private final List<String> failedItems; // 실패한 식재료명 목록
        private final List<Response> addedItems;

        @Builder
        public BulkCreateResponse(List<RefrigeratorItem> addedItems, List<String> failedItems) {
            this.addedItems = addedItems.stream()
                    .map(Response::new)
                    .collect(Collectors.toList());
            this.successCount = addedItems.size();
            this.failedItems = failedItems;
            this.failCount = failedItems.size();
        }
    }

    /**
     * 영수증 스캔 응답 DTO (REF-03)
     * OCR 처리 결과를 사용자 확인용으로 반환
     */
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ScanReceiptResponse {
        private String extractedText; // OCR 추출 원문
        private List<ScannedItem> scannedItems; // 파싱된 식재료 목록
        private int totalItemsFound; // 추출된 총 항목 수

        @Getter
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class ScannedItem {
            private String name;
            private Integer quantity;
            private String unit;
            private Integer price; // 참고용 (저장 안 함)
        }
    }

    // REF-04: 구매 이력 OCR (CLOVA + GPT-5 Nano)
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ScanPurchaseHistoryResponse {
        private String store; // 매장명
        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate purchaseDate; // 구매 날짜
        private List<PurchasedItem> items; // 구매 항목 목록
        private Integer totalAmount; // 총 금액
        private String rawOcrText; // CLOVA OCR 원문 (디버깅/검증용)

        @Getter
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class PurchasedItem {
            private String name; // 상품명
            private Integer price; // 가격
            private Integer quantity; // 수량 (기본값 1)
        }
    }
}
