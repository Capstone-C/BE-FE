package com.capstone.web.posts.service;

import com.capstone.web.category.domain.Category;
import com.capstone.web.category.exception.CategoryNotFoundException;
import com.capstone.web.category.repository.CategoryRepository;
import com.capstone.web.member.domain.Member;
import com.capstone.web.member.exception.UserNotFoundException;
import com.capstone.web.posts.domain.Posts;
import com.capstone.web.posts.dto.PostDto;
import com.capstone.web.posts.exception.PostNotFoundException;
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
    public Long createPost(PostDto.CreateRequest request) {
        Member author = memberRepository.findById(request.getAuthorId())
                .orElseThrow(() -> new UserNotFoundException("작성자를 찾을 수 없습니다. ID: " + request.getAuthorId()));

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
        Posts post = postsRepository.findById(id)
                .orElseThrow(() -> new PostNotFoundException("게시글을 찾을 수 없습니다. ID: " + id));
        return new PostDto.Response(post);
    }

    public List<PostDto.Response> getAllPosts() {
        return postsRepository.findAll().stream()
                .map(PostDto.Response::new)
                .collect(Collectors.toList());
    }

    @Transactional
    public void updatePost(Long id, PostDto.UpdateRequest request) {
        Posts post = postsRepository.findById(id)
                .orElseThrow(() -> new PostNotFoundException("게시글을 찾을 수 없습니다. ID: " + id));

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new CategoryNotFoundException("카테고리를 찾을 수 없습니다. ID: " + request.getCategoryId()));

        post.update(request.getTitle(), request.getContent(), request.getStatus(), category, request.getIsRecipe());
    }

    @Transactional
    public void deletePost(Long id) {
        if (!postsRepository.existsById(id)) {
            throw new PostNotFoundException("삭제할 게시글을 찾을 수 없습니다. ID: " + id);
        }
        postsRepository.deleteById(id);
    }
}
