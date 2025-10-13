package com.capstone.web.member.repository;

import com.capstone.web.member.domain.Member;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(MemberRepositoryTestConfig.class)
@ActiveProfiles("test")
class MemberRepositoryTest {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @DisplayName("Member 저장 시 기본 role, exportScore, auditing 컬럼이 설정된다")
    @Test
    void save_setsDefaultValuesAndAuditing() {
        Member member = Member.builder()
                .email("audit@example.com")
                .password(passwordEncoder.encode("Abcd1234!"))
                .nickname("감사테스트")
                .build();

        Member saved = memberRepository.save(member);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getRole()).isNotNull();
        assertThat(saved.getExportScore()).isZero();
        assertThat(saved.getJoinedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();
    }

    @DisplayName("softDelete 호출 시 deletedAt 이 설정된다")
    @Test
    void softDelete_setsDeletedAt() {
        Member member = Member.builder()
                .email("soft@example.com")
                .password(passwordEncoder.encode("Abcd1234!"))
                .nickname("소프트")
                .build();

        Member saved = memberRepository.save(member);
        saved.softDelete();

        assertThat(saved.isDeleted()).isTrue();
        assertThat(saved.getDeletedAt()).isNotNull();
    }
}

@TestConfiguration
@EnableJpaAuditing
class MemberRepositoryTestConfig {
    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
