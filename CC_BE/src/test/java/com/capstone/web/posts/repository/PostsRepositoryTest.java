package com.capstone.web.posts.repository;

import static org.assertj.core.api.Assertions.*;

import com.capstone.web.category.domain.Category;
import com.capstone.web.category.repository.CategoryRepository;
import com.capstone.web.member.domain.Member;
import com.capstone.web.member.repository.MemberRepository;
import com.capstone.web.posts.domain.Posts;
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

@DataJpaTest // JPA 관련 컴포넌트만 로드하여 테스트
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE) // application-test.yml의 DB 설정을 그대로 사용
@Import(PostsRepositoryTestConfig.class) // JPA Auditing 기능을 활성화하기 위한 설정 클래스 임포트
@ActiveProfiles("test")
class PostsRepositoryTest {

    @Autowired private PostsRepository postsRepository;
    @Autowired private MemberRepository memberRepository;
    @Autowired private CategoryRepository categoryRepository;

    private Member author;
    private Category category;

    @BeforeEach
    void setup() {
        // 테스트 데이터 준비를 위해 연관된 리포지토리도 초기화
        postsRepository.deleteAll();
        memberRepository.deleteAll();
        categoryRepository.deleteAll();

        // 게시글 생성에 필요한 작성자와 카테고리를 미리 생성
        author = memberRepository.save(Member.builder().email("author@example.com").nickname("글쓴이").password("password").build());
        category = categoryRepository.save(Category.builder().name("자유게시판").type(Category.CategoryType.FREE).build());
    }

    @DisplayName("게시글 저장 시 기본값(조회수, 좋아요 등)과 Auditing 필드가 올바르게 설정된다")
    @Test
    void save_setsDefaultValuesAndAuditing() {
        // given
        Posts newPost = Posts.builder()
                .authorId(author)
                .category(category)
                .title("테스트 게시글")
                .content("내용입니다.")
                .status(Posts.PostStatus.PUBLISHED)
                .isRecipe(false)
                .build();

        // when
        Posts savedPost = postsRepository.save(newPost);

        // then
        assertThat(savedPost.getId()).isNotNull();
        assertThat(savedPost.getViewCount()).isZero();
        assertThat(savedPost.getLikeCount()).isZero();
        assertThat(savedPost.getCommentCount()).isZero();
        assertThat(savedPost.getCreatedAt()).isNotNull(); // @CreatedDate 동작 확인
        assertThat(savedPost.getUpdatedAt()).isNotNull(); // @LastModifiedDate 동작 확인
    }

    @DisplayName("update 메서드 호출 시 게시글 정보가 올바르게 변경된다")
    @Test
    void update_modifiesPostDetails() {
        // given
        Posts originalPost = postsRepository.save(Posts.builder()
                .authorId(author)
                .category(category)
                .title("원본 제목")
                .content("원본 내용")
                .status(Posts.PostStatus.PUBLISHED)
                .isRecipe(false)
                .build());

        Category newCategory = categoryRepository.save(Category.builder().name("Q&A").type(Category.CategoryType.QA).build());
        String newTitle = "수정된 제목";
        String newContent = "수정된 내용";
        Posts.PostStatus newStatus = Posts.PostStatus.ARCHIVED;
        boolean newIsRecipe = true;

        // when: 엔티티의 update 메서드 호출 (JPA의 더티 체킹으로 DB에 반영됨)
        originalPost.update(newTitle, newContent, newStatus, newCategory, newIsRecipe);

        // then: DB에서 다시 조회하여 변경 사항 확인
        Posts updatedPost = postsRepository.findById(originalPost.getId()).orElseThrow();

        assertThat(updatedPost.getTitle()).isEqualTo(newTitle);
        assertThat(updatedPost.getContent()).isEqualTo(newContent);
        assertThat(updatedPost.getStatus()).isEqualTo(newStatus);
        assertThat(updatedPost.getCategory().getName()).isEqualTo("Q&A");
        assertThat(updatedPost.isRecipe()).isEqualTo(newIsRecipe);
    }
}

// JPA Auditing 기능을 테스트에서 활성화하기 위한 별도의 설정 클래스
@TestConfiguration
@EnableJpaAuditing
class PostsRepositoryTestConfig {
    // PostsRepositoryTest에서는 PasswordEncoder가 필요 없으므로 비워둠
}