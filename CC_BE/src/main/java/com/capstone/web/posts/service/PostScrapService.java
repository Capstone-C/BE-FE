package com.capstone.web.posts.service;

import com.capstone.web.member.domain.Member;
import com.capstone.web.member.exception.UserNotFoundException;
import com.capstone.web.member.repository.MemberRepository;
import com.capstone.web.posts.domain.PostScrap;
import com.capstone.web.posts.domain.Posts;
import com.capstone.web.posts.dto.PostScrapDto;
import com.capstone.web.posts.exception.PostNotFoundException;
import com.capstone.web.posts.repository.PostScrapRepository;
import com.capstone.web.posts.repository.PostsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostScrapService {

    private final PostScrapRepository postScrapRepository;
    private final PostsRepository postsRepository;
    private final MemberRepository memberRepository;

    // REC-08: 내 스크랩 목록 조회
    public Page<PostScrapDto.Response> getMyScraps(Long memberId, int page, int size, String sortBy, String keyword) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다."));

        Sort sort = Sort.by(Sort.Direction.DESC, "scrappedAt");
        if ("scrappedAt_asc".equals(sortBy)) {
            sort = Sort.by(Sort.Direction.ASC, "scrappedAt");
        } else if ("likes_desc".equals(sortBy)) {
            sort = Sort.by(Sort.Direction.DESC, "post.likeCount");
        }

        Pageable pageable = PageRequest.of(page - 1, size, sort);

        Page<PostScrap> scrapPage;
        if (keyword != null && !keyword.isBlank()) {
            scrapPage = postScrapRepository.findByMemberAndKeyword(member, keyword, pageable);
        } else {
            scrapPage = postScrapRepository.findByMember(member, pageable);
        }

        List<PostScrapDto.Response> content = scrapPage.getContent().stream()
                .map(PostScrapDto.Response::new)
                .collect(Collectors.toList());

        return new PageImpl<>(content, pageable, scrapPage.getTotalElements());
    }

    // REC-09: 스크랩 토글 (추가/삭제)
    @Transactional
    public boolean toggleScrap(Long memberId, Long postId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다."));
        Posts post = postsRepository.findById(postId)
                .orElseThrow(() -> new PostNotFoundException("게시글을 찾을 수 없습니다."));

        var existingScrap = postScrapRepository.findByMemberAndPost(member, post);

        if (existingScrap.isPresent()) {
            postScrapRepository.delete(existingScrap.get());
            return false; // 취소됨
        } else {
            PostScrap newScrap = PostScrap.builder().member(member).post(post).build();
            postScrapRepository.save(newScrap);
            return true; // 저장됨
        }
    }
}