package com.capstone.web.boards.service;

import com.capstone.web.boards.dto.BoardSummaryResponse;
import com.capstone.web.category.domain.Category;
import com.capstone.web.category.repository.CategoryRepository;
import com.capstone.web.posts.domain.Posts;
import com.capstone.web.posts.repository.PostsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BoardsService {

    private final CategoryRepository categoryRepository;
    private final PostsRepository postsRepository;

    public List<BoardSummaryResponse> getAllBoards() {
        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.atTime(LocalTime.MAX);

        return categoryRepository.findAll().stream()
                .map(category -> buildSummary(category, startOfDay, endOfDay))
                .collect(Collectors.toList());
    }

    private BoardSummaryResponse buildSummary(Category category, LocalDateTime startOfDay, LocalDateTime endOfDay) {
        long totalPosts = postsRepository.countByCategory_Id(category.getId());
        long todayPosts = postsRepository.countByCategory_IdAndCreatedAtBetween(category.getId(), startOfDay, endOfDay);
        Posts latest = postsRepository.findTop1ByCategory_IdOrderByCreatedAtDesc(category.getId());

        return BoardSummaryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .description(null) // 확장 시 Category에 description 컬럼을 추가
                .imageUrl(null)    // 확장 시 별도 매핑 테이블 또는 CDN 경로 보강
                .totalPosts(totalPosts)
                .todayPosts(todayPosts)
                .latestTitle(latest != null ? latest.getTitle() : null)
                .build();
    }
}

