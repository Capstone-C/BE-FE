package com.capstone.web.auth.service;

import com.capstone.web.auth.dto.LoginRequest;
import com.capstone.web.auth.dto.LoginResponse;
import com.capstone.web.auth.exception.InvalidCredentialsException;
import com.capstone.web.auth.exception.WithdrawnMemberException;
import com.capstone.web.auth.jwt.JwtTokenProvider;
import com.capstone.web.member.domain.Member;
import com.capstone.web.member.repository.MemberRepository;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional
    public LoginResponse login(LoginRequest request) {
        Member member = memberRepository.findByEmail(request.email())
                .orElseThrow(InvalidCredentialsException::new);

        if (member.isDeleted()) {
            throw new WithdrawnMemberException();
        }

        if (!passwordEncoder.matches(request.password(), member.getPassword())) {
            throw new InvalidCredentialsException();
        }

        member.markLoggedIn(LocalDateTime.now());

        String token = jwtTokenProvider.createToken(member.getId(), member.getRole());
        return LoginResponse.of(token, member);
    }
}
