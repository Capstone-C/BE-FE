package com.capstone.web.media.domain;

import com.capstone.web.posts.domain.Posts;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "Media")
public class Media {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "owner_type", nullable = false)
    private OwnerType ownerType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private Posts post;

    @Enumerated(EnumType.STRING)
    @Column(name = "media_type", nullable = false)
    private MediaType mediaType;

    @Column(nullable = false)
    private String url;

    @Column(name = "order_num")
    private Integer orderNum = 0;

    public enum OwnerType { post, recipe, profile }
    public enum MediaType { image, video }

    @Builder
    public Media(OwnerType ownerType, Posts post, MediaType mediaType, String url, Integer orderNum) {
        this.ownerType = ownerType;
        this.post = post;
        this.mediaType = mediaType;
        this.url = url;
        this.orderNum = orderNum;
    }

    public void setPost(Posts post) {
        this.post = post;
    }
}