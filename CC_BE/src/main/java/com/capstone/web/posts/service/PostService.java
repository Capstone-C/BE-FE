package com.capstone.web.posts.service;

import com.capstone.web.category.domain.Category;
import com.capstone.web.category.exception.CategoryNotFoundException;
import com.capstone.web.category.repository.CategoryRepository;
import com.capstone.web.member.domain.Member;
import com.capstone.web.member.exception.UserNotFoundException;
import com.capstone.web.member.repository.MemberRepository;
import com.capstone.web.posts.domain.Posts;
import com.capstone.web.posts.dto.PostDto;
import com.capstone.web.posts.exception.PostNotFoundException;
import com.capstone.web.posts.exception.PostPermissionException; // (추가) 권한 예외 임포트
import com.capstone.web.posts.repository.PostsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.capstone.web.member.repository.MemberRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostService {

    private final PostsRepository postsRepository;
    private final CategoryRepository categoryRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public Long createPost(Long memberId, PostDto.CreateRequest request) { // (수정) memberId 받기
        // (수정) DTO가 아닌 파라미터로 받은 memberId 사용
        Member author = memberRepository.findById(memberId)
                .orElseThrow(() -> new UserNotFoundException("작성자를 찾을 수 없습니다. ID: " + memberId));

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new CategoryNotFoundException("카테고리를 찾을 수 없습니다. ID: " + request.getCategoryId()));

        Posts post = Posts.builder()
                .authorId(author)
                .category(category)
                .title(request.getTitle())
                .content(request.getContent())
                .status(request.getStatus())
                .isRecipe(request.getIsRecipe())
                .build();

        Posts savedPost = postsRepository.save(post);
        return savedPost.getId();
    }

    public PostDto.Response getPostById(Long id) {
        Posts post = postsNextPage(id);
        return new PostDto.Response(post);
    }

    // N+1 문제 해결을 위해 findAllWithAuthor() 사용 (PostsRepository에 @Query 필요)
    public List<PostDto.Response> getAllPosts() {
        return postsRepository.findAllWithAuthor().stream()
                .map(PostDto.Response::new)
                .collect(Collectors.toList());
    }

    @Transactional
    public void updatePost(Long id, Long memberId, PostDto.UpdateRequest request) { // (수정) memberId 받기
        Posts post = postsNextPage(id);

        // (추가) 작성자 본인 확인 로직
        if (!post.getAuthorId().getId().equals(memberId)) {
            throw new PostPermissionException("게시글을 수정할 권한이 없습니다.");
        }

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new CategoryNotFoundException("카테고리를 찾을 수 없습니다. ID: " + request.getCategoryId()));

        post.update(request.getTitle(), request.getContent(), request.getStatus(), category, request.getIsRecipe());
    }

    @Transactional
    public void deletePost(Long id, Long memberId) { // (수정) memberId 받기
        Posts post = postsNextPage(id);

        // (추가) 작성자 본인 확인 로직
        if (!post.getAuthorId().getId().equals(memberId)) {
            throw new PostPermissionException("게시글을 삭제할 권한이 없습니다.");
        }

        postsRepository.delete(post);
    }

    // (추가) 중복되는 findById 로직을 위한 private 메서드
    private Posts postsNextPage(Long id) {
        return postsRepository.findById(id)
                .orElseThrow(() -> new PostNotFoundException("게시글을 찾을 수 없습니다. ID: " + id));
    }
}