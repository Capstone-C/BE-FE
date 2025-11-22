package com.capstone.web.posts.domain;

import com.capstone.web.member.domain.Member;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "post_scrap", uniqueConstraints = {
        @UniqueConstraint(name = "uk_post_scrap_member_post", columnNames = {"member_id", "post_id"})
})
public class PostScrap {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Posts post;

    @CreationTimestamp
    @Column(name = "scrapped_at", nullable = false, updatable = false)
    private LocalDateTime scrappedAt;

    @Builder
    public PostScrap(Member member, Posts post) {
        this.member = member;
        this.post = post;
    }
}