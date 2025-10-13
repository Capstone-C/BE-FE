package com.capstone.web.member.domain;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "members")
@EntityListeners(AuditingEntityListener.class)
public class Member {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(nullable = false, length = 255)
    private String password;

    @Column(nullable = false, unique = true, length = 50)
    private String nickname;

    @Column(length = 255)
    private String profile;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MemberRole role = MemberRole.USER;

    @CreatedDate
    @Column(name = "joined_at", nullable = false, updatable = false)
    private LocalDateTime joinedAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    @Column(name = "export_score", nullable = false)
    private Long exportScore = 0L;

    @Column(name = "representative_badge_id")
    private Long representativeBadgeId;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    // 주문(Order) 엔티티가 생성되면, 
    // Order에서 Member를 참조하는 단방향 연관관계만 두는 것이 실용적

    @Builder
    public Member(String email, String password, String nickname, String profile, MemberRole role) {
        this.email = email;
        this.password = password;
        this.nickname = nickname;
        this.profile = profile;
        this.role = role != null ? role : MemberRole.USER;
    }

    // Soft delete
    public void softDelete() {
        this.deletedAt = LocalDateTime.now();
    }
    public boolean isDeleted() {
        return this.deletedAt != null;
    }
}

enum MemberRole {
    USER, MODERATOR, SUB_MODERATOR, ADMIN
}
