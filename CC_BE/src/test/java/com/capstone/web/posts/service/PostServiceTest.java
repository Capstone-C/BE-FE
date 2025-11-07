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
import com.capstone.web.posts.exception.PostPermissionException; // (추가) 권한 예외 임포트
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
    private Member otherUser; // (추가) 권한 테스트를 위한 다른 사용자
    private Category category;

    @BeforeEach
    void setup() {
        postsRepository.deleteAll();
        memberRepository.deleteAll();
        categoryRepository.deleteAll();

        author = memberRepository.save(Member.builder().email("author@example.com").nickname("글쓴이").password("password").build());
        // (추가) 다른 사용자 생성
        otherUser = memberRepository.save(Member.builder().email("other@example.com").nickname("다른유저").password("password").build());

        category = categoryRepository.save(Category.builder()
                .name("자유게시판")
                .type(Category.CategoryType.FREE)
                .build());
    }

    @DisplayName("게시글 생성 성공")
    @Test
    void createPost_Success() {
        // given
        // (수정) CreateRequest에서 authorId 제거
        PostDto.CreateRequest request = new PostDto.CreateRequest(
                category.getId(), "테스트 제목", "테스트 내용", Posts.PostStatus.PUBLISHED, false
        );

        // when
        // (수정) createPost 호출 시 memberId를 파라미터로 전달
        Long postId = postService.createPost(author.getId(), request);

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
        // (수정) CreateRequest에서 authorId 제거
        PostDto.CreateRequest request = new PostDto.CreateRequest(
                category.getId(), "테스트 제목", "테스트 내용", Posts.PostStatus.PUBLISHED, false
        );

        // when & then
        // (수정) createPost 호출 시 존재하지 않는 authorId 전달
        assertThatThrownBy(() -> postService.createPost(nonExistentAuthorId, request))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("작성자를 찾을 수 없습니다.");
    }

    @DisplayName("존재하지 않는 카테고리 ID로 게시글 생성 시 예외 발생")
    @Test
    void createPost_CategoryNotFound() {
        // given
        Long nonExistentCategoryId = 999L;
        // (수정) CreateRequest에서 authorId 제거
        PostDto.CreateRequest request = new PostDto.CreateRequest(
                nonExistentCategoryId, "테스트 제목", "테스트 내용", Posts.PostStatus.PUBLISHED, false
        );

        // when & then
        // (수정) createPost 호출 시 authorId 전달
        assertThatThrownBy(() -> postService.createPost(author.getId(), request))
                .isInstanceOf(CategoryNotFoundException.class)
                .hasMessageContaining("카테고리를 찾을 수 없습니다.");
    }

    // --- (getPostById_Success, getPostById_PostNotFound는 변경 없음) ---
    @DisplayName("ID로 게시글 단건 조회 성공")
    @Test
    void getPostById_Success() {
        Posts savedPost = postsRepository.save(Posts.builder().authorId(author).category(category).title("조회용 제목").content("내용").status(Posts.PostStatus.PUBLISHED).build());
        PostDto.Response response = postService.getPostById(savedPost.getId());
        assertThat(response.getTitle()).isEqualTo("조회용 제목");
        assertThat(response.getAuthorId()).isEqualTo(author.getId());
    }

    @DisplayName("존재하지 않는 ID로 게시글 조회 시 예외 발생")
    @Test
    void getPostById_PostNotFound() {
        Long nonExistentPostId = 999L;
        assertThatThrownBy(() -> postService.getPostById(nonExistentPostId))
                .isInstanceOf(PostNotFoundException.class);
    }
    // --- (여기까지 변경 없음) ---


    @DisplayName("게시글 수정 성공")
    @Test
    void updatePost_Success() {
        // given
        Posts originalPost = postsRepository.save(Posts.builder().authorId(author).category(category).title("원본 제목").content("원본 내용").status(Posts.PostStatus.PUBLISHED).isRecipe(false).build());
        Category newCategory = categoryRepository.save(Category.builder().name("공지사항").type(Category.CategoryType.QA).build());
        PostDto.UpdateRequest request = new PostDto.UpdateRequest(
                "수정된 제목", "수정된 내용", newCategory.getId(), Posts.PostStatus.ARCHIVED, true
        );

        // when
        // (수정) updatePost 호출 시 memberId(author.getId()) 전달
        postService.updatePost(originalPost.getId(), author.getId(), request);

        // then
        Posts updatedPost = postsRepository.findById(originalPost.getId()).orElseThrow();
        assertThat(updatedPost.getTitle()).isEqualTo("수정된 제목");
        assertThat(updatedPost.getCategory().getName()).isEqualTo("공지사항");
    }

    @DisplayName("존재하지 않는 게시글 수정 시 예외 발생")
    @Test
    void updatePost_PostNotFound() {
        // given
        Long nonExistentPostId = 999L;
        PostDto.UpdateRequest request = new PostDto.UpdateRequest("수정", "수정", category.getId(), Posts.PostStatus.PUBLISHED, false);

        // when & then
        // (수정) updatePost 호출 시 memberId(author.getId()) 전달
        assertThatThrownBy(() -> postService.updatePost(nonExistentPostId, author.getId(), request))
                .isInstanceOf(PostNotFoundException.class);
    }

    @DisplayName("다른 사람의 게시글 수정 시 예외 발생 (권한 없음)") // 👇 [추가된 테스트]
    @Test
    void updatePost_Fail_PermissionDenied() {
        // given
        Posts originalPost = postsRepository.save(Posts.builder().authorId(author).category(category).title("원본 제목").content("원본 내용").build());
        PostDto.UpdateRequest request = new PostDto.UpdateRequest("해킹 시도", "해킹 시도", category.getId(), Posts.PostStatus.PUBLISHED, false);

        // when & then
        // 'otherUser'의 ID로 수정을 시도
        assertThatThrownBy(() -> postService.updatePost(originalPost.getId(), otherUser.getId(), request))
                .isInstanceOf(PostPermissionException.class)
                .hasMessageContaining("게시글을 수정할 권한이 없습니다.");
    }


    @DisplayName("게시글 삭제 성공")
    @Test
    void deletePost_Success() {
        // given
        Posts postToDelete = postsRepository.save(Posts.builder().authorId(author).category(category).title("삭제될 게시글").content("내용").build());

        // when
        // (수정) deletePost 호출 시 memberId(author.getId()) 전달
        postService.deletePost(postToDelete.getId(), author.getId());

        // then
        assertThat(postsRepository.existsById(postToDelete.getId())).isFalse();
    }

    @DisplayName("존재하지 않는 게시글 삭제 시 예외 발생")
    @Test
    void deletePost_PostNotFound() {
        // given
        Long nonExistentPostId = 999L;

        // when & then
        // (수정) deletePost 호출 시 memberId(author.getId()) 전달
        assertThatThrownBy(() -> postService.deletePost(nonExistentPostId, author.getId()))
                .isInstanceOf(PostNotFoundException.class);
    }

    @DisplayName("다른 사람의 게시글 삭제 시 예외 발생 (권한 없음)") // 👇 [추가된 테스트]
    @Test
    void deletePost_Fail_PermissionDenied() {
        // given
        Posts postToDelete = postsRepository.save(Posts.builder().authorId(author).category(category).title("삭제될 게시글").content("내용").build());

        // when & then
        // 'otherUser'의 ID로 삭제를 시도
        assertThatThrownBy(() -> postService.deletePost(postToDelete.getId(), otherUser.getId()))
                .isInstanceOf(PostPermissionException.class)
                .hasMessageContaining("게시글을 삭제할 권한이 없습니다.");
    }
}