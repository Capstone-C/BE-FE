package com.capstone.web.member.service;

import com.capstone.web.member.domain.Member;
import com.capstone.web.member.dto.MemberRegisterRequest;
import com.capstone.web.member.dto.MemberRegisterResponse;
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
