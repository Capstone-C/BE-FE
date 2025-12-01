package com.capstone.web.posts.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.capstone.web.category.domain.Category;
import com.capstone.web.category.exception.CategoryNotFoundException;
import com.capstone.web.category.repository.CategoryRepository;
import com.capstone.web.common.S3UploadService;
import com.capstone.web.media.repository.MediaRepository;
import com.capstone.web.member.domain.Member;
import com.capstone.web.member.exception.UserNotFoundException;
import com.capstone.web.member.repository.MemberRepository;
import com.capstone.web.posts.domain.Posts;
import com.capstone.web.posts.dto.PostDto;
import com.capstone.web.posts.dto.PostIngredientDto;
import com.capstone.web.posts.exception.PostPermissionException;
import com.capstone.web.posts.repository.PostsRepository;
import com.capstone.web.refrigerator.repository.RefrigeratorItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean; // Spring Boot 3.4+
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class PostServiceTest {

    @Autowired private PostService postService;
    @Autowired private PostsRepository postsRepository;
    @Autowired private MemberRepository memberRepository;
    @Autowired private CategoryRepository categoryRepository;

    @MockitoBean private S3UploadService s3UploadService;
    @MockitoBean private MediaRepository mediaRepository;
    @MockitoBean private RefrigeratorItemRepository refrigeratorItemRepository;

    private Member author;
    private Member otherUser;
    private Category category;

    private final MultipartFile mockThumbnail = mock(MultipartFile.class);
    private final MultipartFile mockExtraImg = mock(MultipartFile.class);

    private final Posts.DietType DEFAULT_DIET_TYPE = Posts.DietType.GENERAL;
    private final Integer DEFAULT_COOK_TIME = 30;
    private final Integer DEFAULT_SERVINGS = 2;
    private final Posts.Difficulty DEFAULT_DIFFICULTY = Posts.Difficulty.MEDIUM;
    private final List<PostIngredientDto.Request> DEFAULT_INGREDIENTS = Collections.emptyList();

    @BeforeEach
    void setup() throws IOException {
        postsRepository.deleteAll();
        memberRepository.deleteAll();
        categoryRepository.deleteAll();

        author = memberRepository.save(Member.builder().email("author@example.com").nickname("글쓴이").password("password").build());
        otherUser = memberRepository.save(Member.builder().email("other@example.com").nickname("다른유저").password("password").build());
        category = categoryRepository.save(Category.builder().name("자유게시판").type(Category.CategoryType.FREE).build());

        when(s3UploadService.uploadFile(any(MultipartFile.class))).thenReturn("https://s3-url.com/test.jpg");
        when(mockThumbnail.isEmpty()).thenReturn(false);
        when(mockExtraImg.isEmpty()).thenReturn(false);
    }

    @DisplayName("게시글 생성 성공")
    @Test
    void createPost_Success() {
        // [수정] 11개 인자 전달 (null 추가)
        PostDto.CreateRequest request = new PostDto.CreateRequest(
                category.getId(), "테스트 제목", "테스트 내용", Posts.PostStatus.PUBLISHED, false,
                null,
                DEFAULT_DIET_TYPE, DEFAULT_COOK_TIME, DEFAULT_SERVINGS, DEFAULT_DIFFICULTY, DEFAULT_INGREDIENTS
        );

        List<MultipartFile> extraFiles = Arrays.asList(mockExtraImg, mockExtraImg);
        Long postId = postService.createPost(author.getId(), request, mockThumbnail, extraFiles);

        assertThat(postId).isNotNull();
        Posts foundPost = postsRepository.findById(postId).orElseThrow();
        assertThat(foundPost.getTitle()).isEqualTo("테스트 제목");
        assertThat(foundPost.getMedia()).hasSizeGreaterThanOrEqualTo(1);
    }

    @DisplayName("존재하지 않는 작성자로 게시글 생성 시 예외")
    @Test
    void createPost_UserNotFound() {
        Long nonExistentId = 999L;
        PostDto.CreateRequest request = new PostDto.CreateRequest(
                category.getId(), "제목", "내용", Posts.PostStatus.PUBLISHED, false,
                null, DEFAULT_DIET_TYPE, DEFAULT_COOK_TIME, DEFAULT_SERVINGS, DEFAULT_DIFFICULTY, DEFAULT_INGREDIENTS
        );

        assertThatThrownBy(() -> postService.createPost(nonExistentId, request, null, null))
                .isInstanceOf(UserNotFoundException.class);
    }

    @DisplayName("존재하지 않는 카테고리로 게시글 생성 시 예외")
    @Test
    void createPost_CategoryNotFound() {
        Long nonExistentId = 999L;
        PostDto.CreateRequest request = new PostDto.CreateRequest(
                nonExistentId, "제목", "내용", Posts.PostStatus.PUBLISHED, false,
                null, DEFAULT_DIET_TYPE, DEFAULT_COOK_TIME, DEFAULT_SERVINGS, DEFAULT_DIFFICULTY, DEFAULT_INGREDIENTS
        );

        assertThatThrownBy(() -> postService.createPost(author.getId(), request, null, null))
                .isInstanceOf(CategoryNotFoundException.class);
    }

    @DisplayName("게시글 수정 성공")
    @Test
    void updatePost_Success() {
        Posts originalPost = postsRepository.save(Posts.builder().authorId(author).category(category).title("원본").content("내용").build());

        // [수정] 11개 인자 전달
        PostDto.UpdateRequest request = new PostDto.UpdateRequest(
                "수정 제목", "수정 내용", category.getId(), Posts.PostStatus.ARCHIVED, true,
                null,
                Posts.DietType.VEGAN, 45, 4, Posts.Difficulty.HIGH, DEFAULT_INGREDIENTS
        );

        postService.updatePost(originalPost.getId(), author.getId(), request, mockThumbnail, null);

        Posts updatedPost = postsRepository.findById(originalPost.getId()).orElseThrow();
        assertThat(updatedPost.getTitle()).isEqualTo("수정 제목");
        assertThat(updatedPost.isRecipe()).isTrue();
    }

    @DisplayName("다른 사람의 게시글 삭제 시 예외 발생")
    @Test
    void deletePost_Fail_PermissionDenied() {
        Posts post = postsRepository.save(Posts.builder().authorId(author).category(category).title("삭제").content("내용").build());

        assertThatThrownBy(() -> postService.deletePost(post.getId(), otherUser.getId()))
                .isInstanceOf(PostPermissionException.class);
    }
}