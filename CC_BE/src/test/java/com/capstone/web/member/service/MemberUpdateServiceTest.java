package com.capstone.web.member.service;

import static org.assertj.core.api.Assertions.*;

import com.capstone.web.member.domain.Member;
import com.capstone.web.member.exception.DuplicateNicknameException;
import com.capstone.web.member.exception.InvalidNicknameException;
import com.capstone.web.member.exception.InvalidProfileImageSizeException;
import com.capstone.web.member.exception.InvalidProfileImageTypeException;
import com.capstone.web.member.repository.MemberRepository;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class MemberUpdateServiceTest {

    @Autowired
    private MemberUpdateService memberUpdateService;

    @Autowired
    private MemberRepository memberRepository;

    @BeforeEach
    void setup() { memberRepository.deleteAll(); }

    private Member create(String email, String nickname) {
        Member m = Member.builder()
                .email(email)
                .password("encoded")
                .nickname(nickname)
                .build();
        return memberRepository.save(m);
    }

    @DisplayName("닉네임만 수정 성공")
    @Test
    void updateNicknameOnly() {
        Member m = create("n1@example.com", "닉1");
        var resp = memberUpdateService.update(m, "새닉", null);
        assertThat(resp.nickname()).isEqualTo("새닉");
    }

    @DisplayName("프로필 이미지 업로드 성공")
    @Test
    void updateProfileImageOnly() throws IOException {
        Member m = create("p1@example.com", "닉2");
        MockMultipartFile img = new MockMultipartFile("profileImage", "test.png", "image/png", new byte[]{1,2,3});
        var resp = memberUpdateService.update(m, null, img);
        assertThat(resp.profile()).contains("/static/profile/");
    }

    @DisplayName("닉네임 패턴 위반 시 예외")
    @Test
    void invalidNicknamePattern() {
        Member m = create("inv@example.com", "정상닉");
        assertThatThrownBy(() -> memberUpdateService.update(m, "a", null))
                .isInstanceOf(InvalidNicknameException.class);
    }

    @DisplayName("닉네임 중복 시 예외")
    @Test
    void duplicateNickname() {
        create("a@example.com", "중복닉");
        Member self = create("b@example.com", "다른닉");
        assertThatThrownBy(() -> memberUpdateService.update(self, "중복닉", null))
                .isInstanceOf(DuplicateNicknameException.class);
    }

    @DisplayName("지원하지 않는 확장자")
    @Test
    void invalidImageType() {
        Member m = create("type@example.com", "닉3");
        MockMultipartFile img = new MockMultipartFile("profileImage", "test.txt", "text/plain", "abc".getBytes(StandardCharsets.UTF_8));
        assertThatThrownBy(() -> memberUpdateService.update(m, null, img))
                .isInstanceOf(InvalidProfileImageTypeException.class);
    }

    @DisplayName("파일 크기 초과")
    @Test
    void invalidImageSize() {
        Member m = create("size@example.com", "닉4");
        byte[] big = new byte[(int)(5 * 1024 * 1024) + 1];
        MockMultipartFile img = new MockMultipartFile("profileImage", "big.png", "image/png", big);
        assertThatThrownBy(() -> memberUpdateService.update(m, null, img))
                .isInstanceOf(InvalidProfileImageSizeException.class);
    }
}
