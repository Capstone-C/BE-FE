package com.capstone.web.config;

import com.capstone.web.auth.jwt.JwtProperties;
import com.capstone.web.auth.jwt.JwtAuthenticationFilter;
import com.capstone.web.auth.logout.JwtBlacklistFilter;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
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
                        .requestMatchers("/static/**").permitAll()
                        // 공개 조회 (GET)
                        .requestMatchers(HttpMethod.GET, "/api/v1/posts", "/api/v1/posts/*", "/api/boards").permitAll()
                        // 추천 토글/작성/수정/삭제는 인증 필요
                        .requestMatchers(HttpMethod.POST, "/api/v1/posts/*/like").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/v1/posts").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/api/v1/posts/*").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/posts/*").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/v1/boards/*/posts").authenticated()
                        // 멤버 관련 보호 API
                        .requestMatchers("/api/v1/members/me").authenticated()
                        .requestMatchers("/api/v1/members/blocks/**").authenticated()
                        .anyRequest().permitAll()
                )
                .addFilterBefore(authenticationFilter, org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(blacklistFilter, org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
