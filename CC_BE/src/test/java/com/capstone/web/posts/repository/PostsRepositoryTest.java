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

    @BeforeEach
    void setup() {
        postsRepository.deleteAll();
        memberRepository.deleteAll();
        categoryRepository.deleteAll();

        author = memberRepository.save(Member.builder().email("author@example.com").nickname("글쓴이").password("pw").build());
        category = categoryRepository.save(Category.builder().name("자유").type(Category.CategoryType.FREE).build());
    }

    @DisplayName("게시글 저장 (레시피 정보 포함)")
    @Test
    void save_Success() {
        // given
        Posts newPost = Posts.builder()
                .authorId(author).category(category).title("제목").content("내용")
                .status(Posts.PostStatus.PUBLISHED).isRecipe(true)
                .dietType(Posts.DietType.VEGAN)
                .cookTimeInMinutes(60)
                .servings(2)
                .difficulty(Posts.Difficulty.HIGH)
                .build();

        // when
        Posts savedPost = postsRepository.save(newPost);

        // then
        assertThat(savedPost.getId()).isNotNull();
        assertThat(savedPost.getCookTimeInMinutes()).isEqualTo(60); // 레시피 필드 검증
        assertThat(savedPost.getDifficulty()).isEqualTo(Posts.Difficulty.HIGH);
    }

    @DisplayName("게시글 업데이트")
    @Test
    void update_Success() {
        // given
        Posts post = postsRepository.save(Posts.builder().authorId(author).category(category).title("원제목").content("내용").build());

        // when: 엔티티 update 메서드 호출 (9개 인자)
        post.update(
                "새제목", "새내용", Posts.PostStatus.ARCHIVED, category, true,
                Posts.DietType.KETO, 20, 1, Posts.Difficulty.LOW
        );

        // then
        Posts updated = postsRepository.findById(post.getId()).orElseThrow();
        assertThat(updated.getTitle()).isEqualTo("새제목");
        assertThat(updated.getDietType()).isEqualTo(Posts.DietType.KETO);
    }
}

@TestConfiguration
@EnableJpaAuditing
class PostsRepositoryTestConfig {}