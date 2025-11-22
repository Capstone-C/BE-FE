package com.capstone.web.posts.service;

import com.capstone.web.member.domain.Member;
import com.capstone.web.member.repository.MemberRepository;
import com.capstone.web.posts.domain.PostScrap;
import com.capstone.web.posts.domain.Posts;
import com.capstone.web.posts.repository.PostScrapRepository;
import com.capstone.web.posts.repository.PostsRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PostScrapServiceTest {

    @InjectMocks private PostScrapService postScrapService;
    @Mock private PostScrapRepository postScrapRepository;
    @Mock private PostsRepository postsRepository;
    @Mock private MemberRepository memberRepository;

    @DisplayName("스크랩 토글 - 없을 땐 생성(true)")
    @Test
    void toggleScrap_Create() {
        // given
        Long memberId = 1L;
        Long postId = 1L;
        Member member = mock(Member.class);
        Posts post = mock(Posts.class);

        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));
        when(postsRepository.findById(postId)).thenReturn(Optional.of(post));
        when(postScrapRepository.findByMemberAndPost(member, post)).thenReturn(Optional.empty()); // 스크랩 없음

        // when
        boolean result = postScrapService.toggleScrap(memberId, postId);

        // then
        assertThat(result).isTrue(); // 생성됨
        verify(postScrapRepository, times(1)).save(any(PostScrap.class));
    }

    @DisplayName("스크랩 토글 - 있을 땐 삭제(false)")
    @Test
    void toggleScrap_Delete() {
        // given
        Long memberId = 1L;
        Long postId = 1L;
        Member member = mock(Member.class);
        Posts post = mock(Posts.class);
        PostScrap existingScrap = mock(PostScrap.class);

        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));
        when(postsRepository.findById(postId)).thenReturn(Optional.of(post));
        when(postScrapRepository.findByMemberAndPost(member, post)).thenReturn(Optional.of(existingScrap)); // 스크랩 있음

        // when
        boolean result = postScrapService.toggleScrap(memberId, postId);

        // then
        assertThat(result).isFalse(); // 삭제됨
        verify(postScrapRepository, times(1)).delete(existingScrap);
    }
}