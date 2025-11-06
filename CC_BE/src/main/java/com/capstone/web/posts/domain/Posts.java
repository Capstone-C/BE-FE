package com.capstone.web.posts.domain;

import com.capstone.web.category.domain.Category;
import com.capstone.web.member.domain.Member;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Posts {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private Member authorId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Lob
    @Column(name = "content", nullable = false)
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @ColumnDefault("'DRAFT'")
    private PostStatus status;

    @Column(name = "view_count", nullable = false)
    @ColumnDefault("0")
    private int viewCount = 0;

    @Column(name = "like_count", nullable = false)
    @ColumnDefault("0")
    private int likeCount = 0;

    @Column(name = "comment_count", nullable = false)
    @ColumnDefault("0")
    private final int commentCount = 0;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "selected")
    @ColumnDefault("'FALSE'")
    private TruthValue selected;

    @Enumerated(EnumType.STRING)
    @Column(name = "file")
    @ColumnDefault("'FALSE'")
    private TruthValue file;

    @Column(name = "is_recipe", nullable = false)
    private boolean isRecipe;


    @Builder
    public Posts(Member authorId, Category category, String title, String content, PostStatus status, boolean isRecipe) {
        this.authorId = authorId;
        this.category = category;
        this.title = title;
        this.content = content;
        this.status = status != null ? status : PostStatus.DRAFT;
        this.isRecipe = isRecipe;
    }

    public void update(String title, String content, PostStatus status, Category category, boolean isRecipe) {
        this.title = title;
        this.content = content;
        this.status = status;
        this.category = category;
        this.isRecipe = isRecipe;
    }

    public void increaseViewCount() {
        this.viewCount = this.viewCount + 1;
    }

    public void increaseLikeCount() {
        this.likeCount = this.likeCount + 1;
    }

    public void decreaseLikeCount() {
        this.likeCount = Math.max(0, this.likeCount - 1);
    }

    public enum PostStatus {
        DRAFT, PUBLISHED, ARCHIVED
    }

    public enum TruthValue {
        TRUE, FALSE
    }
}
