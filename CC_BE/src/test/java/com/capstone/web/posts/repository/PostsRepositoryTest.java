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

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(PostsRepositoryTestConfig.class)
@ActiveProfiles("test")
class PostsRepositoryTest {

    @Autowired private PostsRepository postsRepository;
    @Autowired private MemberRepository memberRepository;
    @Autowired private CategoryRepository categoryRepository;

    private Member author;
    private Category category;

    // (추가) 레시피 필드를 위한 기본값
    private final Posts.DietType DEFAULT_DIET_TYPE = Posts.DietType.GENERAL;
    private final Integer DEFAULT_COOK_TIME = 30;
    private final Integer DEFAULT_SERVINGS = 2;
    private final Posts.Difficulty DEFAULT_DIFFICULTY = Posts.Difficulty.MEDIUM;


    @BeforeEach
    void setup() {
        postsRepository.deleteAll();
        memberRepository.deleteAll();
        categoryRepository.deleteAll();

        author = memberRepository.save(Member.builder().email("author@example.com").nickname("글쓴이").password("password").build());
        category = categoryRepository.save(Category.builder().name("자유게시판").type(Category.CategoryType.FREE).build());
    }

    @DisplayName("게시글 저장 시 기본값(조회수, 좋아요 등)과 Auditing 필드가 올바르게 설정된다")
    @Test
    void save_setsDefaultValuesAndAuditing() {
        // given
        // (수정) 10개 인자 Builder에 전달
        Posts newPost = Posts.builder()
                .authorId(author)
                .category(category)
                .title("테스트 게시글")
                .content("내용입니다.")
                .status(Posts.PostStatus.PUBLISHED)
                .isRecipe(false)
                .dietType(DEFAULT_DIET_TYPE) // (추가)
                .cookTimeInMinutes(DEFAULT_COOK_TIME) // (추가)
                .servings(DEFAULT_SERVINGS) // (추가)
                .difficulty(DEFAULT_DIFFICULTY) // (추가)
                .build();

        // when
        Posts savedPost = postsRepository.save(newPost);

        // then
        assertThat(savedPost.getId()).isNotNull();
        assertThat(savedPost.getViewCount()).isZero();
        assertThat(savedPost.getLikeCount()).isZero();
        assertThat(savedPost.getCommentCount()).isZero();
        assertThat(savedPost.getCreatedAt()).isNotNull();
        assertThat(savedPost.getUpdatedAt()).isNotNull();
        assertThat(savedPost.getCookTimeInMinutes()).isEqualTo(DEFAULT_COOK_TIME); // 레시피 필드 확인
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
                .dietType(null) // (추가)
                .cookTimeInMinutes(null) // (추가)
                .servings(null) // (추가)
                .difficulty(null) // (추가)
                .build());

        Category newCategory = categoryRepository.save(Category.builder().name("Q&A").type(Category.CategoryType.QA).build());
        String newTitle = "수정된 제목";
        String newContent = "수정된 내용";
        Posts.PostStatus newStatus = Posts.PostStatus.ARCHIVED;
        boolean newIsRecipe = true;
        Posts.DietType newDietType = Posts.DietType.VEGAN;
        Integer newCookTime = 60;
        Integer newServings = 3;
        Posts.Difficulty newDifficulty = Posts.Difficulty.HIGH;


        // when: 엔티티의 update 메서드 호출
        // (수정) 9개 인자 모두 전달
        originalPost.update(
                newTitle,
                newContent,
                newStatus,
                newCategory,
                newIsRecipe,
                newDietType, // (추가)
                newCookTime, // (추가)
                newServings, // (추가)
                newDifficulty // (추가)
        );

        // then: DB에서 다시 조회하여 변경 사항 확인
        Posts updatedPost = postsRepository.findById(originalPost.getId()).orElseThrow();

        assertThat(updatedPost.getTitle()).isEqualTo(newTitle);
        assertThat(updatedPost.getContent()).isEqualTo(newContent);
        assertThat(updatedPost.getStatus()).isEqualTo(newStatus);
        assertThat(updatedPost.getCategory().getName()).isEqualTo("Q&A");
        assertThat(updatedPost.isRecipe()).isEqualTo(newIsRecipe);
        assertThat(updatedPost.getCookTimeInMinutes()).isEqualTo(newCookTime); // 레시피 필드 확인
    }
}

// JPA Auditing 기능을 테스트에서 활성화하기 위한 별도의 설정 클래스
@TestConfiguration
@EnableJpaAuditing
class PostsRepositoryTestConfig {
    // PostsRepositoryTest에서는 PasswordEncoder가 필요 없으므로 비워둠
}