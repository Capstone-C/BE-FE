package com.capstone.web.member.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "password_reset_token", indexes = {
        @Index(name = "idx_password_reset_token_value", columnList = "token", unique = true),
        @Index(name = "idx_password_reset_token_member", columnList = "member_id")
})
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PasswordResetToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Member member;

    @Column(nullable = false, length = 128, unique = true)
    private String token;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    private LocalDateTime usedAt;
    private LocalDateTime invalidatedAt;
    @Column(nullable = false)
    private LocalDateTime createdAt;

    public boolean isExpired() { return LocalDateTime.now().isAfter(expiresAt); }
    public boolean isUsed() { return usedAt != null; }
    public boolean isInvalidated() { return invalidatedAt != null; }
    public boolean isActive() { return !isExpired() && !isUsed() && !isInvalidated(); }

    public void markUsed() { this.usedAt = LocalDateTime.now(); }
    public void invalidate() { this.invalidatedAt = LocalDateTime.now(); }
}
