package com.capstone.web.posts.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "PostIngredient") // (이름 변경) DDL 스키마 테이블명
public class PostIngredient { // (이름 변경)

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipeId", nullable = false) // DDL의 'recipeId' 컬럼명은 유지
    private Posts post; // Posts 엔티티 참조

    @Column(name = "expirationDate")
    private LocalDateTime expirationDate;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "quantity")
    private Long quantity;

    @Column(name = "unit", length = 50)
    private String unit;

    @Column(name = "memo")
    private String memo;

    @Builder
    public PostIngredient(Posts post, LocalDateTime expirationDate, String name, Long quantity, String unit, String memo) {
        this.post = post;
        this.expirationDate = expirationDate;
        this.name = name;
        this.quantity = quantity;
        this.unit = unit;
        this.memo = memo;
    }
}