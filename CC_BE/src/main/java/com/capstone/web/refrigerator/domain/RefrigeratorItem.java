package com.capstone.web.refrigerator.domain;

import com.capstone.web.member.domain.Member;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 냉장고 식재료 엔티티
 * REF-01: 내 냉장고 식재료 목록 조회
 * REF-02: 수동으로 식재료 추가
 * REF-05: 식재료 정보 수정
 * REF-06: 식재료 삭제
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
@Table(
        name = "refrigerator_items",
        // 변경: 동일 회원 + 동일 이름 + 동일 소비기한만 유니크. 소비기한이 다르면 별도 항목 허용.
        uniqueConstraints = @UniqueConstraint(columnNames = {"member_id", "name", "expiration_date"})
)
public class RefrigeratorItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(nullable = false)
    private Integer quantity = 1;

    @Column(length = 10)
    private String unit;

    @Column
    private LocalDate expirationDate;

    @Column(length = 200)
    private String memo;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Builder
    public RefrigeratorItem(Member member, String name, Integer quantity, String unit,
                            LocalDate expirationDate, String memo) {
        this.member = member;
        this.name = name;
        this.quantity = quantity != null ? quantity : 1;
        this.unit = unit;
        this.expirationDate = expirationDate;
        this.memo = memo;
    }

    /**
     * 식재료 정보 수정 (REF-05)
     * 식재료명은 수정 불가 (삭제 후 재등록 유도)
     */
    public void update(Integer quantity, String unit, LocalDate expirationDate, String memo) {
        this.quantity = quantity != null ? quantity : this.quantity;
        this.unit = unit;
        this.expirationDate = expirationDate;
        this.memo = memo;
    }

    /**
     * REF-08: 수량 업데이트
     */
    public void updateQuantity(Integer newQuantity) {
        this.quantity = newQuantity;
    }

    /**
     * 소비기한까지 남은 일수 계산 (D-day)
     */
    public Long getDaysUntilExpiration() {
        if (expirationDate == null) {
            return null;
        }
        return java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), expirationDate);
    }

    /**
     * 소비기한 임박 여부 (3일 이내)
     */
    public boolean isExpirationSoon() {
        Long days = getDaysUntilExpiration();
        return days != null && days >= 0 && days <= 3;
    }

    /**
     * 소비기한 경과 여부
     */
    public boolean isExpired() {
        Long days = getDaysUntilExpiration();
        return days != null && days < 0;
    }
}
