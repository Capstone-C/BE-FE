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
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

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
                        // 냉장고 관련 보호 API
                        .requestMatchers("/api/v1/refrigerator/**").authenticated()
                        .anyRequest().permitAll()
                )
                .addFilterBefore(authenticationFilter, org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(blacklistFilter, org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    // @Bean
    // public WebMvcConfigurer corsConfigurer() {
    //     return new WebMvcConfigurer() {
    //         @Override
    //         public void addCorsMappings(CorsRegistry registry) {
    //             registry.addMapping("/api/**") // 1. CORS를 적용할 API 경로 패턴 (전체 API)
    //                     // 2. [핵심] 허용할 CloudFront 도메인 (HTTPS 프로토콜 포함)
    //                     .allowedOrigins("https://d3hpujisowtim.cloudfront.net", "http://localhost:3000") 
    //                     // 3. 허용할 HTTP 메서드 (GET, POST, PUT, DELETE 등)
    //                     .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
    //                     // 4. 허용할 모든 헤더 (Authorization 헤더 포함)
    //                     .allowedHeaders("*")
    //                     // 5. 자격증명(쿠키, Auth 헤더) 허용
    //                     .allowCredentials(true)
    //                     // 6. Pre-flight 요청 캐시 시간 (1시간)
    //                     .maxAge(3600); 
    //         }
    //     };
    // }
}
