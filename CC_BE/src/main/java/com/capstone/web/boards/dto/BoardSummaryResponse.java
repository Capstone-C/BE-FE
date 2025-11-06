package com.capstone.web.boards.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BoardSummaryResponse {
    private Long id;              // board (category) id
    private String name;          // board name
    private String description;   // optional description
    private String imageUrl;      // optional representative image

    private long totalPosts;      // 총 게시글 수
    private long todayPosts;      // 오늘 등록된 새 글 수
    private String latestTitle;   // 가장 최근 게시글 제목
}

