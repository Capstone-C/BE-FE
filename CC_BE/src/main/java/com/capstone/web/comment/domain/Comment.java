package com.capstone.web.comment.domain;

import com.capstone.web.member.domain.Member;
import com.capstone.web.posts.domain.Posts;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "comment")
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Posts post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private Member author;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false)
    @ColumnDefault("0")
    private int likeCount = 0;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @Enumerated(EnumType.STRING)
    @ColumnDefault("'FALSE'")
    private TruthValue file = TruthValue.FALSE;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @ColumnDefault("'ACTIVE'")
    private CommentStatus status = CommentStatus.ACTIVE;

    @Column(nullable = false)
    @ColumnDefault("0")
    private int depth = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Comment parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> children = new ArrayList<>();

    @Builder
    public Comment(Posts post, Member author, String content, int depth, Comment parent) {
        this.post = post;
        this.author = author;
        this.content = content;
        this.depth = depth;
        this.parent = parent;
    }

    //== 비즈니스 로직 ==//
    public void updateContent(String newContent) {
        this.content = newContent;
    }

    public void softDelete() {
        this.status = CommentStatus.DELETED;
        this.content = "삭제된 댓글입니다.";
    }

    public enum CommentStatus {
        ACTIVE, DELETED
    }

    public enum TruthValue {
        TRUE, FALSE
    }
}