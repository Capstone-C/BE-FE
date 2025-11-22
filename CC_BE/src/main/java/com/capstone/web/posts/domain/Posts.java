package com.capstone.web.posts.domain;

import com.capstone.web.category.domain.Category;
import com.capstone.web.media.domain.Media; // (추가) Media 엔티티 임포트
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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional; // (추가)

// (추가) Media도 N+1 문제 없이 가져오기 위한 EntityGraph
@NamedEntityGraph(name = "Posts.withIngredients", attributeNodes = {
        @NamedAttributeNode("ingredients")
})
@NamedEntityGraph(name = "Posts.withMedia", attributeNodes = {
        @NamedAttributeNode("media")
})
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

    // --- 레시피 기본 정보 ---
    @Enumerated(EnumType.STRING)
    @Column(name = "diet_type")
    private DietType dietType; // 식단 타입

    @Column(name = "cook_time_in_minutes")
    private Integer cookTimeInMinutes; // 조리 시간(분)

    @Column(name = "servings")
    private Integer servings; // 분량(인분)

    @Enumerated(EnumType.STRING)
    @Column(name = "difficulty")
    private Difficulty difficulty; // 난이도
    // ----------------------------

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PostIngredient> ingredients = new ArrayList<>();

    // (추가) Media와의 일대다 관계 설정
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Media> media = new ArrayList<>();

    @Builder
    public Posts(Member authorId, Category category, String title, String content, PostStatus status, boolean isRecipe,
                 DietType dietType, Integer cookTimeInMinutes, Integer servings, Difficulty difficulty) {
        this.authorId = authorId;
        this.category = category;
        this.title = title;
        this.content = content;
        this.status = status != null ? status : PostStatus.DRAFT;
        this.isRecipe = isRecipe;
        this.dietType = dietType;
        this.cookTimeInMinutes = cookTimeInMinutes;
        this.servings = servings;
        this.difficulty = difficulty;
    }

    public void update(String title, String content, PostStatus status, Category category, boolean isRecipe,
                       DietType dietType, Integer cookTimeInMinutes, Integer servings, Difficulty difficulty) {
        this.title = title;
        this.content = content;
        this.status = status;
        this.category = category;
        this.isRecipe = isRecipe;
        this.dietType = dietType;
        this.cookTimeInMinutes = cookTimeInMinutes;
        this.servings = servings;
        this.difficulty = difficulty;
    }

    // (추가) Media 연관관계 편의 메서드
    public void addMedia(Media mediaItem) {
        this.media.add(mediaItem);
        mediaItem.setPost(this);
    }

    // (추가) 썸네일 URL(순서 0번)을 가져오는 헬퍼 메서드
    public Optional<String> getThumbnailUrl() {
        return this.media.stream()
                .filter(m -> m.getOwnerType() == Media.OwnerType.post && m.getOrderNum() == 0)
                .map(Media::getUrl)
                .findFirst();
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

    // --- Enum 정의 ---
    public enum DietType {
        VEGAN,
        VEGETARIAN,
        KETO,
        PALEO,
        MEDITERRANEAN,
        LOW_CARB,
        HIGH_PROTEIN,
        GENERAL
    }

    public enum Difficulty {
        VERY_HIGH,
        HIGH,
        MEDIUM,
        LOW,
        VERY_LOW
    }

    public enum PostStatus {
        DRAFT, PUBLISHED, ARCHIVED
    }

    public enum TruthValue {
        TRUE, FALSE
    }
}