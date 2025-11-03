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
 * 블랙리스트 토큰/유효하지 않은 토큰은 401을 반환.
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
        String path = request.getRequestURI();
        String method = request.getMethod();
        // 공개 엔드포인트: 로그인, 로그아웃, Swagger 문서 -> 인증 시도 생략
        if (isPublic(path, method)) {
            filterChain.doFilter(request, response);
            return;
        }

        String header = request.getHeader("Authorization");
        if (!StringUtils.hasText(header) || !header.startsWith("Bearer ")) {
            unauthorized(response, "AUTH_MISSING_TOKEN", "인증 토큰이 필요합니다.");
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

    private boolean isPublic(String path, String method) {
        // 회원가입: POST /api/v1/members 허용
        boolean isSignup = "/api/v1/members".equals(path) && "POST".equalsIgnoreCase(method);
        return isSignup
                || path.startsWith("/api/v1/auth/login")
                || path.startsWith("/api/v1/auth/logout")
                // 비밀번호 재설정 (요청 & 확인) 공개 엔드포인트
                || path.startsWith("/api/v1/auth/password-reset")
                || path.startsWith("/v3/api-docs")
                || path.startsWith("/swagger-ui")
                // JwtAuthenticationFilter가 정적 리소스 요청을 인증 검사 대상에서 제외하도록 /static 경로 추가
                || path.startsWith("/static");
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
