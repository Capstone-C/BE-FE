package com.capstone.web.posts.repository;

import com.capstone.web.category.domain.Category;
import com.capstone.web.category.repository.CategoryRepository;
import com.capstone.web.member.domain.Member;
import com.capstone.web.member.repository.MemberRepository;
import com.capstone.web.posts.domain.PostScrap;
import com.capstone.web.posts.domain.Posts;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(PostsRepositoryTestConfig.class) // Auditing 설정
@ActiveProfiles("test")
class PostScrapRepositoryTest {

    @Autowired private PostScrapRepository postScrapRepository;
    @Autowired private PostsRepository postsRepository;
    @Autowired private MemberRepository memberRepository;
    @Autowired private CategoryRepository categoryRepository;

    private Member member;
    private Posts post;

    @BeforeEach
    void setup() {
        postScrapRepository.deleteAll();
        postsRepository.deleteAll();
        memberRepository.deleteAll();
        categoryRepository.deleteAll();

        member = memberRepository.save(Member.builder().email("test@example.com").nickname("유저").password("pw").build());
        Category category = categoryRepository.save(Category.builder().name("레시피").type(Category.CategoryType.RECIPE).build());

        post = postsRepository.save(Posts.builder()
                .authorId(member)
                .category(category)
                .title("맛있는 레시피")
                .content("내용")
                .status(Posts.PostStatus.PUBLISHED)
                .isRecipe(true)
                .build());
    }

    @DisplayName("스크랩 저장 및 조회 성공")
    @Test
    void saveAndFindScrap() {
        // given
        PostScrap scrap = PostScrap.builder().member(member).post(post).build();

        // when
        postScrapRepository.save(scrap);

        // then
        boolean exists = postScrapRepository.findByMemberAndPost(member, post).isPresent();
        assertThat(exists).isTrue();
    }

    @DisplayName("내 스크랩 목록 조회 (페이징)")
    @Test
    void findByMember_Pagination() {
        // given
        for (int i = 0; i < 5; i++) {
            Posts p = postsRepository.save(Posts.builder()
                    .authorId(member)
                    .category(post.getCategory())
                    .title("레시피 " + i)
                    .content("내용")
                    .status(Posts.PostStatus.PUBLISHED)
                    .isRecipe(true)
                    .build());
            postScrapRepository.save(PostScrap.builder().member(member).post(p).build());
        }

        // when
        Page<PostScrap> result = postScrapRepository.findByMember(member, PageRequest.of(0, 3));

        // then
        assertThat(result.getContent()).hasSize(3); // 3개만 가져왔는지
        assertThat(result.getTotalElements()).isEqualTo(5); // 전체는 5개인지
    }
}