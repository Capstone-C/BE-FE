package com.capstone.web.member.service;


import com.capstone.web.member.domain.Member;
import com.capstone.web.member.dto.MemberRegisterRequest;
import com.capstone.web.member.dto.MemberRegisterResponse;
import com.capstone.web.member.dto.MemberPasswordChangeRequest;
import com.capstone.web.member.exception.InvalidOldPasswordException;
import com.capstone.web.member.exception.SameAsOldPasswordException;
import com.capstone.web.member.exception.DuplicateEmailException;
import com.capstone.web.member.exception.DuplicateNicknameException;
import com.capstone.web.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    public void changePassword(Long memberId, MemberPasswordChangeRequest request) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));

        // 기존 비밀번호 불일치
        if (!passwordEncoder.matches(request.oldPassword(), member.getPassword())) {
            throw new InvalidOldPasswordException();
        }
        // 새 비밀번호가 기존과 동일
        if (passwordEncoder.matches(request.newPassword(), member.getPassword())) {
            throw new SameAsOldPasswordException();
        }
        // 비밀번호 변경
        member.changePassword(passwordEncoder.encode(request.newPassword()));
    }

    public MemberRegisterResponse register(MemberRegisterRequest request) {
        if (memberRepository.existsByEmail(request.email())) {
            throw new DuplicateEmailException();
        }
        if (memberRepository.existsByNickname(request.nickname())) {
            throw new DuplicateNicknameException();
        }

        Member member = Member.builder()
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .nickname(request.nickname())
                .build();

        Member saved = memberRepository.save(member);
        return MemberRegisterResponse.of(saved.getId(), saved.getEmail(), saved.getNickname());
    }
}
