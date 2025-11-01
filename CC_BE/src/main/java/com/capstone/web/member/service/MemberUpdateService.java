package com.capstone.web.member.service;

import com.capstone.web.member.domain.Member;
import com.capstone.web.member.dto.MemberProfileResponse;
import com.capstone.web.member.exception.*;
import com.capstone.web.member.repository.MemberRepository;
import java.io.IOException;
import java.util.Set;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Transactional
public class MemberUpdateService {

    private static final Pattern NICKNAME_PATTERN = Pattern.compile("^[A-Za-z0-9가-힣]{2,10}$");
    private static final long MAX_BYTES = 5L * 1024 * 1024; // 5MB
    private static final Set<String> ALLOWED_EXT = Set.of(".jpg", ".jpeg", ".png", ".gif");

    private final MemberRepository memberRepository;
    private final ProfileImageStorage imageStorage;

    public MemberProfileResponse update(Member member, String nickname, MultipartFile profileImage) {
        // 닉네임 처리
        if (StringUtils.hasText(nickname) && !nickname.equals(member.getNickname())) {
            validateNickname(nickname, member.getId());
            memberChangeNickname(member, nickname);
        }

        // 이미지 처리
        if (profileImage != null && !profileImage.isEmpty()) {
            validateImage(profileImage);
            try {
                String url = imageStorage.store(member.getId(), profileImage);
                setProfile(member, url);
            } catch (IOException e) {
                throw new RuntimeException("프로필 이미지 저장 실패", e);
            }
        }

        Member saved = memberRepository.save(member);
        return new MemberProfileResponse(saved.getId(), saved.getEmail(), saved.getNickname(), saved.getRole(), saved.getProfile(), saved.getExportScore(), saved.getRepresentativeBadgeId(), saved.getJoinedAt(), saved.getLastLoginAt());
    }

    private void validateNickname(String nickname, Long selfId) {
        if (!NICKNAME_PATTERN.matcher(nickname).matches()) {
            throw new InvalidNicknameException();
        }
        // 닉네임 중복 검증 (자기 자신 제외)
        memberRepository.findByNickname(nickname).ifPresent(existingMember -> {
            if (!existingMember.getId().equals(selfId)) {
                throw new DuplicateNicknameException();
            }
        });
    }

    private void validateImage(MultipartFile file) {
        if (file.getSize() > MAX_BYTES) {
            throw new InvalidProfileImageSizeException();
        }
        String original = file.getOriginalFilename();
        if (original == null || !original.contains(".")) {
            throw new InvalidProfileImageTypeException();
        }
        String ext = original.substring(original.lastIndexOf('.')).toLowerCase();
        if (!ALLOWED_EXT.contains(ext)) {
            throw new InvalidProfileImageTypeException();
        }
    }

    private void memberChangeNickname(Member member, String nickname) {
        member.changeNickname(nickname);
    }

    private void setProfile(Member member, String url) {
        member.changeProfile(url);
    }
}
