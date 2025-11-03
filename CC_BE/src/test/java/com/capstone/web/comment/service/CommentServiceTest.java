package com.capstone.web.comment.service;

import com.capstone.web.category.domain.Category;
import com.capstone.web.category.repository.CategoryRepository;
import com.capstone.web.comment.domain.Comment;
import com.capstone.web.comment.dto.CommentDto;
import com.capstone.web.comment.dto.CommentQueryDto;
import com.capstone.web.comment.exception.CommentPermissionException;
import com.capstone.web.comment.repository.CommentRepository;
import com.capstone.web.member.domain.Member;
import com.capstone.web.member.exception.UserNotFoundException;
import com.capstone.web.member.repository.MemberRepository;
import com.capstone.web.posts.domain.Posts;
import com.capstone.web.posts.repository.PostsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class CommentServiceTest {

    @Autowired private CommentService commentService;
    @Autowired private CommentRepository commentRepository;
    @Autowired private PostsRepository postsRepository;
    @Autowired private MemberRepository memberRepository;
    @Autowired private CategoryRepository categoryRepository;

    private Member author;
    private Member otherUser;
    private Posts post;
    private Category category; // 👈 1. 여기에 멤버 변수로 선언

    @BeforeEach
    void setup() {
        commentRepository.deleteAll();
        postsRepository.deleteAll();
        memberRepository.deleteAll();
        categoryRepository.deleteAll();

        author = memberRepository.save(Member.builder().email("author@example.com").nickname("글쓴이").password("pass").build());
        otherUser = memberRepository.save(Member.builder().email("other@example.com").nickname("다른사용자").password("pass").build());

        // 👇 2. 'Category' 타입을 빼고, 위에서 선언한 멤버 변수에 값을 할당
        category = categoryRepository.save(Category.builder().name("자유").type(Category.CategoryType.FREE).build());

        post = postsRepository.save(Posts.builder().authorId(author).category(category).title("테스트 글").content("내용").build());
    }

    // --- (이하 모든 테스트 코드는 변경할 필요 없습니다) ---

    @DisplayName("댓글을 성공적으로 생성한다")
    @Test
    void createComment_Success() {
        // given
        CommentDto.CreateRequest request = new CommentDto.CreateRequest(null, "서비스 테스트 댓글");

        // when
        Long commentId = commentService.createComment(post.getId(), author.getId(), request);

        // then
        Comment found = commentRepository.findById(commentId).orElseThrow();
        assertThat(found.getContent()).isEqualTo("서비스 테스트 댓글");
        assertThat(found.getDepth()).isZero();
    }

    @DisplayName("대댓글을 성공적으로 생성한다")
    @Test
    void createReplyComment_Success() {
        // given
        Comment parent = commentRepository.save(Comment.builder().post(post).author(author).content("부모 댓글").depth(0).build());
        CommentDto.CreateRequest request = new CommentDto.CreateRequest(parent.getId(), "대댓글입니다.");

        // when
        Long childId = commentService.createComment(post.getId(), author.getId(), request);

        // then
        Comment foundChild = commentRepository.findById(childId).orElseThrow();
        assertThat(foundChild.getParent().getId()).isEqualTo(parent.getId());
        assertThat(foundChild.getDepth()).isEqualTo(1);
    }

    @DisplayName("게시글의 댓글 목록을 계층 구조로 조회한다")
    @Test
    void getCommentsByPost_ReturnsHierarchicalStructure() {
        // given
        Comment parent = commentRepository.save(Comment.builder().post(post).author(author).content("부모1").depth(0).build());
        commentRepository.save(Comment.builder().post(post).author(author).content("자식1-1").parent(parent).depth(1).build());
        commentRepository.save(Comment.builder().post(post).author(author).content("자식1-2").parent(parent).depth(1).build());
        commentRepository.save(Comment.builder().post(post).author(author).content("부모2").depth(0).build());

        // when
        List<CommentDto.Response> comments = commentService.getCommentsByPost(post.getId());

        // then
        assertThat(comments).hasSize(2);
        assertThat(comments.get(0).getContent()).isEqualTo("부모1");
        assertThat(comments.get(0).getChildren()).hasSize(2);
        assertThat(comments.get(1).getContent()).isEqualTo("부모2");
        assertThat(comments.get(1).getChildren()).isEmpty();
    }

    @DisplayName("자신의 댓글을 성공적으로 수정한다")
    @Test
    void updateComment_Success() {
        // given
        Comment comment = commentRepository.save(Comment.builder().post(post).author(author).content("원본 내용").build());
        CommentDto.UpdateRequest request = new CommentDto.UpdateRequest("수정된 내용");

        // when
        commentService.updateComment(comment.getId(), author.getId(), request);

        // then
        Comment updated = commentRepository.findById(comment.getId()).orElseThrow();
        assertThat(updated.getContent()).isEqualTo("수정된 내용");
    }

    @DisplayName("다른 사람의 댓글을 수정하려 하면 예외가 발생한다")
    @Test
    void updateComment_Fail_PermissionDenied() {
        // given
        Comment comment = commentRepository.save(Comment.builder().post(post).author(author).content("남의 댓글").build());
        CommentDto.UpdateRequest request = new CommentDto.UpdateRequest("수정 시도");

        // when & then
        assertThatThrownBy(() -> commentService.updateComment(comment.getId(), otherUser.getId(), request))
                .isInstanceOf(CommentPermissionException.class);
    }

    @DisplayName("자식이 없는 댓글 삭제 시 DB에서 완전히 삭제된다 (하드 삭제)")
    @Test
    void deleteComment_HardDelete() {
        // given
        Comment comment = commentRepository.save(Comment.builder().post(post).author(author).content("삭제될 댓글").build());

        // when
        commentService.deleteComment(comment.getId(), author.getId());

        // then
        assertThat(commentRepository.existsById(comment.getId())).isFalse();
    }

    @DisplayName("자식이 있는 댓글 삭제 시 내용은 변경되고 DB에 유지된다 (소프트 삭제)")
    @Test
    void deleteComment_SoftDelete() {
        // given
        Comment parent = commentRepository.save(Comment.builder().post(post).author(author).content("부모 댓글").build());
        parent.getChildren().add(Comment.builder().post(post).author(otherUser).content("자식 댓글").parent(parent).build());
        commentRepository.save(parent);

        // when
        commentService.deleteComment(parent.getId(), author.getId());

        // then
        assertThat(commentRepository.existsById(parent.getId())).isTrue();
        Comment deleted = commentRepository.findById(parent.getId()).orElseThrow();
        assertThat(deleted.getStatus()).isEqualTo(Comment.CommentStatus.DELETED);
        assertThat(deleted.getContent()).isEqualTo("삭제된 댓글입니다.");
    }

    @DisplayName("특정 작성자가 작성한 댓글 목록을 성공적으로 조회한다")
    @Test
    void getCommentsByAuthor_Success() {
        // given
        Posts anotherPost = postsRepository.save(Posts.builder().authorId(otherUser).category(category).title("다른 글").content("내용").build());
        commentRepository.save(Comment.builder().post(post).author(author).content("내 댓글 1").build());
        commentRepository.save(Comment.builder().post(anotherPost).author(author).content("내 댓글 2").build());
        commentRepository.save(Comment.builder().post(post).author(otherUser).content("남의 댓글").build());

        // when
        List<CommentQueryDto> result = commentService.getCommentsByAuthor(author.getId());

        // then
        assertThat(result).hasSize(2);
        assertThat(result).extracting(CommentQueryDto::getContent)
                .containsExactlyInAnyOrder("내 댓글 1", "내 댓글 2");
        assertThat(result.get(0).getPostId()).isNotNull();
        assertThat(result.get(0).getPostTitle()).isNotNull();
    }

    @DisplayName("존재하지 않는 작성자 ID로 댓글 목록 조회 시 예외가 발생한다")
    @Test
    void getCommentsByAuthor_Fail_UserNotFound() {
        // given
        Long nonExistentAuthorId = 999L;

        // when & then
        assertThatThrownBy(() -> commentService.getCommentsByAuthor(nonExistentAuthorId))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("사용자를 찾을 수 없습니다.");
    }
}