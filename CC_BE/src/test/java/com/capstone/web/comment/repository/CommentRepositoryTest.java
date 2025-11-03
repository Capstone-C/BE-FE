package com.capstone.web.comment.repository;

import com.capstone.web.category.domain.Category;
import com.capstone.web.category.repository.CategoryRepository;
import com.capstone.web.comment.domain.Comment;
import com.capstone.web.member.domain.Member;
import com.capstone.web.member.repository.MemberRepository;
import com.capstone.web.posts.domain.Posts;
import com.capstone.web.posts.repository.PostsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(CommentRepositoryTestConfig.class)
@ActiveProfiles("test")
class CommentRepositoryTest {

    @Autowired private CommentRepository commentRepository;
    @Autowired private PostsRepository postsRepository;
    @Autowired private MemberRepository memberRepository;
    @Autowired private CategoryRepository categoryRepository;

    private Member author;
    private Posts post;
    private Category category; // 멤버 변수로 선언

    @BeforeEach
    void setup() {
        commentRepository.deleteAll();
        postsRepository.deleteAll();
        memberRepository.deleteAll();
        categoryRepository.deleteAll();

        author = memberRepository.save(Member.builder().email("author@example.com").nickname("글쓴이").password("password").build());
        // 멤버 변수에 값 할당
        category = categoryRepository.save(Category.builder().name("자유").type(Category.CategoryType.FREE).build());
        post = postsRepository.save(Posts.builder().authorId(author).category(category).title("테스트 게시글").content("내용").build());
    }

    @DisplayName("댓글을 성공적으로 저장한다")
    @Test
    void save_Success() {
        // given
        Comment comment = Comment.builder().post(post).author(author).content("첫 댓글!").build();

        // when
        Comment savedComment = commentRepository.save(comment);

        // then
        assertThat(savedComment.getId()).isNotNull();
        assertThat(savedComment.getContent()).isEqualTo("첫 댓글!");
        assertThat(savedComment.getPost().getId()).isEqualTo(post.getId());
    }

    @DisplayName("대댓글(자식 댓글)을 부모와 함께 저장한다")
    @Test
    void save_WithParent() {
        // given
        Comment parentComment = commentRepository.save(Comment.builder().post(post).author(author).content("부모 댓글").build());
        Comment childComment = Comment.builder().post(post).author(author).content("자식 댓글").parent(parentComment).build();

        // when
        Comment savedChild = commentRepository.save(childComment);

        // then
        assertThat(savedChild.getParent()).isNotNull();
        assertThat(savedChild.getParent().getId()).isEqualTo(parentComment.getId());
    }

    @DisplayName("findAllByPostId는 특정 게시글의 댓글만 올바른 순서로 가져온다")
    @Test
    void findAllByPostId_ReturnsCorrectly() {
        // given
        // 이제 category 변수를 여기서도 사용할 수 있습니다.
        Posts anotherPost = postsRepository.save(Posts.builder().authorId(author).category(category).title("다른 게시글").content("내용").build());
        Comment parent = commentRepository.save(Comment.builder().post(post).author(author).content("부모").build());
        Comment child = commentRepository.save(Comment.builder().post(post).author(author).content("자식").parent(parent).build());
        commentRepository.save(Comment.builder().post(anotherPost).author(author).content("다른 게시글의 댓글").build()); // 관계 없는 댓글

        // when
        List<Comment> comments = commentRepository.findAllByPostId(post.getId());

        // then
        assertThat(comments).hasSize(2);
        assertThat(comments.get(0).getId()).isEqualTo(parent.getId()); // 부모가 먼저 오는지 확인
        assertThat(comments.get(1).getId()).isEqualTo(child.getId());  // 자식이 나중에 오는지 확인
    }
}


@TestConfiguration
@EnableJpaAuditing
class CommentRepositoryTestConfig {
    // 내용은 비워둠
}