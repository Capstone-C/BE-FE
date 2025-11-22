package com.capstone.web.posts.controller;

import com.capstone.web.auth.jwt.JwtAuthenticationFilter.MemberPrincipal;
import com.capstone.web.posts.dto.PostScrapDto;
import com.capstone.web.posts.service.PostScrapService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class PostScrapController {

    private final PostScrapService postScrapService;

    // REC-08: 내 스크랩 목록 조회
    @GetMapping("/users/me/scraps")
    public ResponseEntity<Page<PostScrapDto.Response>> getMyScraps(
            @AuthenticationPrincipal MemberPrincipal userPrincipal,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(defaultValue = "scrappedAt_desc") String sortBy,
            @RequestParam(required = false) String keyword
    ) {
        Page<PostScrapDto.Response> response = postScrapService.getMyScraps(
                userPrincipal.id(), page, size, sortBy, keyword
        );
        return ResponseEntity.ok(response);
    }

    // REC-09: 스크랩 토글
    @PostMapping("/posts/{postId}/scrap")
    public ResponseEntity<Map<String, Boolean>> toggleScrap(
            @PathVariable Long postId,
            @AuthenticationPrincipal MemberPrincipal userPrincipal
    ) {
        boolean isScrapped = postScrapService.toggleScrap(userPrincipal.id(), postId);
        return ResponseEntity.ok(Map.of("scrapped", isScrapped));
    }
}