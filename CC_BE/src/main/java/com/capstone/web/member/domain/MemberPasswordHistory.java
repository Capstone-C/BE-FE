package com.capstone.web.member.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "member_password_history")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class MemberPasswordHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(nullable = false, length = 255)
    private String password;

    @Column(nullable = false)
    private java.time.LocalDateTime changedAt;
}
