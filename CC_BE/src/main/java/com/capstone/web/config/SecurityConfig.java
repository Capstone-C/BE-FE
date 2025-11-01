package com.capstone.web.config;

import com.capstone.web.auth.jwt.JwtProperties;
import com.capstone.web.auth.jwt.JwtAuthenticationFilter;
import com.capstone.web.auth.logout.JwtBlacklistFilter;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableConfigurationProperties(JwtProperties.class)
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtBlacklistFilter blacklistFilter, JwtAuthenticationFilter authenticationFilter) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorize -> authorize
                    .requestMatchers("/api/v1/auth/login", "/api/v1/auth/logout", "/api/v1/auth/password-reset", "/api/v1/auth/password-reset/confirm", "/v3/api-docs/**", "/swagger-ui/**").permitAll()
                    .requestMatchers("/static/**").permitAll() // 정적 리소스 접근 허용 (프로필 이미지 등)
                    .requestMatchers("/api/v1/members/me").authenticated()
                    .requestMatchers("/api/v1/members/blocks/**").authenticated()
                    .anyRequest().permitAll()
                )
        // 인증 -> 블랙리스트 순서 유지 (이미 블랙리스트 필터는 공개 엔드포인트에서 스킵)
        .addFilterBefore(authenticationFilter, org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter.class)
        .addFilterBefore(blacklistFilter, org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
