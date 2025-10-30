package com.capstone.web.diary.domain;

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

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "diary", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"member_id", "date", "meal_type"}))
@EntityListeners(AuditingEntityListener.class)
public class Diary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(name = "date", nullable = false)
    private LocalDate date;

    @Enumerated(EnumType.STRING)
    @Column(name = "meal_type", nullable = false, length = 20)
    private MealType mealType;

    @Column(name = "content", nullable = false, length = 500)
    private String content;

    @Column(name = "image_url", length = 255)
    private String imageUrl;

    @Column(name = "recipe_id")
    private Long recipeId;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Builder
    public Diary(Member member, LocalDate date, MealType mealType, String content, String imageUrl, Long recipeId) {
        this.member = member;
        this.date = date;
        this.mealType = mealType;
        this.content = content;
        this.imageUrl = imageUrl;
        this.recipeId = recipeId;
    }

    public void update(MealType mealType, String content, String imageUrl, Long recipeId) {
        this.mealType = mealType;
        this.content = content;
        this.imageUrl = imageUrl;
        this.recipeId = recipeId;
    }

    public enum MealType {
        BREAKFAST(0), LUNCH(1), DINNER(2), SNACK(3);

        private final int order;

        MealType(int order) {
            this.order = order;
        }

        public int getOrder() {
            return order;
        }
    }
}
