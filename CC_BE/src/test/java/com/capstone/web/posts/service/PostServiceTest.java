package com.capstone.web.posts.service;

import static org.assertj.core.api.Assertions.*;

import com.capstone.web.category.domain.Category;
import com.capstone.web.category.exception.CategoryNotFoundException;
import com.capstone.web.category.repository.CategoryRepository;
import com.capstone.web.member.domain.Member;
import com.capstone.web.member.exception.UserNotFoundException;
import com.capstone.web.member.repository.MemberRepository;
import com.capstone.web.posts.domain.Posts;
import com.capstone.web.posts.dto.PostDto;
import com.capstone.web.posts.exception.PostNotFoundException;
import com.capstone.web.posts.repository.PostsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class PostServiceTest {

    @Autowired
    private PostService postService;

    @Autowired
    private PostsRepository postsRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    private Member author;
    private Category category;

    @BeforeEach
    void setup() {
        postsRepository.deleteAll();
        memberRepository.deleteAll();
        categoryRepository.deleteAll();

        author = memberRepository.save(Member.builder().email("author@example.com").nickname("글쓴이").password("password").build());
        category = categoryRepository.save(Category.builder()
                .name("자유게시판")

                .type(Category.CategoryType.FREE) // 예시: type 필드에 적절한 값을 추가
                .build());    }

    @DisplayName("게시글 생성 성공")
    @Test
    void createPost_Success() {
        // given
        PostDto.CreateRequest request = new PostDto.CreateRequest(
                author.getId(), category.getId(), "테스트 제목", "테스트 내용", Posts.PostStatus.PUBLISHED, false // ACTIVE -> PUBLISHED
        );

        // when
        Long postId = postService.createPost(request);

        // then
        assertThat(postId).isNotNull();
        Posts foundPost = postsRepository.findById(postId).orElseThrow();
        assertThat(foundPost.getTitle()).isEqualTo("테스트 제목");
        assertThat(foundPost.getAuthorId().getId()).isEqualTo(author.getId());
    }

    @DisplayName("존재하지 않는 작성자 ID로 게시글 생성 시 예외 발생")
    @Test
    void createPost_UserNotFound() {
        // given
        Long nonExistentAuthorId = 999L;
        PostDto.CreateRequest request = new PostDto.CreateRequest(
                nonExistentAuthorId, category.getId(), "테스트 제목", "테스트 내용", Posts.PostStatus.PUBLISHED, false // ACTIVE -> PUBLISHED
        );

        // when & then
        assertThatThrownBy(() -> postService.createPost(request))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("작성자를 찾을 수 없습니다.");
    }

    @DisplayName("존재하지 않는 카테고리 ID로 게시글 생성 시 예외 발생")
    @Test
    void createPost_CategoryNotFound() {
        // given
        Long nonExistentCategoryId = 999L;
        PostDto.CreateRequest request = new PostDto.CreateRequest(
                author.getId(), nonExistentCategoryId, "테스트 제목", "테스트 내용", Posts.PostStatus.PUBLISHED, false // ACTIVE -> PUBLISHED
        );

        // when & then
        assertThatThrownBy(() -> postService.createPost(request))
                .isInstanceOf(CategoryNotFoundException.class)
                .hasMessageContaining("카테고리를 찾을 수 없습니다.");
    }

    @DisplayName("ID로 게시글 단건 조회 성공")
    @Test
    void getPostById_Success() {
        // given
        Posts savedPost = postsRepository.save(Posts.builder().authorId(author).category(category).title("조회용 제목").content("내용").status(Posts.PostStatus.PUBLISHED).build());

        // when
        PostDto.Response response = postService.getPostById(savedPost.getId());

        // then
        assertThat(response.getTitle()).isEqualTo("조회용 제목");
        assertThat(response.getAuthorId()).isEqualTo(author.getId());
        assertThat(response.getCategoryName()).isEqualTo(category.getName());
    }

    @DisplayName("존재하지 않는 ID로 게시글 조회 시 예외 발생")
    @Test
    void getPostById_PostNotFound() {
        // given
        Long nonExistentPostId = 999L;

        // when & then
        assertThatThrownBy(() -> postService.getPostById(nonExistentPostId))
                .isInstanceOf(PostNotFoundException.class)
                .hasMessageContaining("게시글을 찾을 수 없습니다.");
    }

    @DisplayName("게시글 수정 성공")
    @Test
    void updatePost_Success() {
        // given
        Posts originalPost = postsRepository.save(Posts.builder().authorId(author).category(category).title("원본 제목").content("원본 내용").status(Posts.PostStatus.PUBLISHED).isRecipe(false).build()); // ACTIVE -> PUBLISHED
        Category newCategory = categoryRepository.save(Category.builder()
                .name("공지사항")
                .type(Category.CategoryType.QA) // 예시로 QA 타입 지정
                .build());        PostDto.UpdateRequest request = new PostDto.UpdateRequest(
                "수정된 제목", "수정된 내용", newCategory.getId(), Posts.PostStatus.ARCHIVED, true // INACTIVE -> ARCHIVED
        );

        // when
        postService.updatePost(originalPost.getId(), request);

        // then
        Posts updatedPost = postsRepository.findById(originalPost.getId()).orElseThrow();
        assertThat(updatedPost.getTitle()).isEqualTo("수정된 제목");
        assertThat(updatedPost.getContent()).isEqualTo("수정된 내용");
        assertThat(updatedPost.getStatus()).isEqualTo(Posts.PostStatus.ARCHIVED);
        assertThat(updatedPost.getCategory().getName()).isEqualTo("공지사항");
        assertThat(updatedPost.isRecipe()).isTrue();
    }

    @DisplayName("존재하지 않는 게시글 수정 시 예외 발생")
    @Test
    void updatePost_PostNotFound() {
        // given
        Long nonExistentPostId = 999L;
        PostDto.UpdateRequest request = new PostDto.UpdateRequest("수정", "수정", category.getId(), Posts.PostStatus.PUBLISHED, false); // ACTIVE -> PUBLISHED

        // when & then
        assertThatThrownBy(() -> postService.updatePost(nonExistentPostId, request))
                .isInstanceOf(PostNotFoundException.class);
    }

    @DisplayName("게시글 삭제 성공")
    @Test
    void deletePost_Success() {
        // given
        Posts postToDelete = postsRepository.save(Posts.builder().authorId(author).category(category).title("삭제될 게시글").content("내용").build());

        // when
        postService.deletePost(postToDelete.getId());

        // then
        assertThat(postsRepository.existsById(postToDelete.getId())).isFalse();
    }

    @DisplayName("존재하지 않는 게시글 삭제 시 예외 발생")
    @Test
    void deletePost_PostNotFound() {
        // given
        Long nonExistentPostId = 999L;

        // when & then
        assertThatThrownBy(() -> postService.deletePost(nonExistentPostId))
                .isInstanceOf(PostNotFoundException.class)
                .hasMessageContaining("삭제할 게시글을 찾을 수 없습니다.");
    }
}