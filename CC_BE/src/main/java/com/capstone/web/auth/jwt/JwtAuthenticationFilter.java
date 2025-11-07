package com.capstone.web.auth.jwt;

import com.capstone.web.auth.logout.TokenBlacklist;
import com.capstone.web.member.domain.Member;
import com.capstone.web.member.repository.MemberRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * JWT를 파싱하여 인증 정보를 SecurityContext에 저장하는 필터.
 * 토큰이 없는 요청은 통과시켜 공개 엔드포인트에서 401이 발생하지 않도록 한다.
 * 블랙리스트/유효하지 않은 토큰이 제공된 경우에만 401 반환.
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final TokenBlacklist tokenBlacklist;
    private final MemberRepository memberRepository;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain)
            throws ServletException, IOException {
        String header = request.getHeader("Authorization");

        // 토큰이 없으면 공개 요청으로 간주하고 다음 필터로 넘김
        if (!StringUtils.hasText(header) || !header.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = header.substring(7);
        if (tokenBlacklist.isBlacklisted(token)) {
            unauthorized(response, "AUTH_TOKEN_BLACKLISTED", "로그아웃된 토큰입니다.");
            return;
        }
        if (!jwtTokenProvider.isValid(token)) {
            unauthorized(response, "AUTH_INVALID_TOKEN", "유효하지 않은 토큰입니다.");
            return;
        }
        Long memberId = jwtTokenProvider.extractMemberId(token);
        Member member = memberRepository.findById(memberId).orElse(null);
        if (member == null) {
            unauthorized(response, "AUTH_MEMBER_NOT_FOUND", "회원 정보를 찾을 수 없습니다.");
            return;
        }

        boolean withdrawn = member.isDeleted();

        // 권한 부여 - 단순 Role -> ROLE_ 접두사
        Collection<? extends GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_" + member.getRole().name()));
        Authentication auth = new UsernamePasswordAuthenticationToken(new MemberPrincipal(member), token, authorities);
        SecurityContextHolder.getContext().setAuthentication(auth);
        if (withdrawn) {
            request.setAttribute("WITHDRAWN_MEMBER", true);
        }
        filterChain.doFilter(request, response);
    }

    private void unauthorized(HttpServletResponse response, String code, String message) throws IOException {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"code\":\"" + code + "\",\"message\":\"" + message + "\"}");
    }

    /**
     * 인증된 회원 정보를 보관하는 최소 Principal
     */
    public record MemberPrincipal(Long id, String email, String nickname, String role) {
        public MemberPrincipal(Member m) {
            this(m.getId(), m.getEmail(), m.getNickname(), m.getRole().name());
        }
    }
}
