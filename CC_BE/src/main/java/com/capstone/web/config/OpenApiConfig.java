package com.capstone.web.config;

import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@SecurityScheme(
    name = "JWT",
    type = SecuritySchemeType.HTTP,
    scheme = "bearer",
    bearerFormat = "JWT",
    description = "로그인 후 발급받은 JWT를 'Bearer {token}' 형태로 입력하세요."
)
public class OpenApiConfig {
    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("캡스톤 API 문서")
                .description("""
                    ## 캡스톤 프로젝트 API
                    
                    ### 인증 방법
                    1. `POST /api/v1/auth/signup` - 회원가입
                    2. `POST /api/v1/auth/login` - 로그인 (JWT 토큰 발급)
                    3. 우측 상단 **🔓 Authorize** 버튼 클릭
                    4. Value 입력: `Bearer {토큰}` (Bearer 접두사 필수)
                    
                    ### 주요 기능
                    - **Auth**: 회원가입, 로그인, 로그아웃, 비밀번호 관리
                    - **Member**: 프로필 조회/수정, 차단 관리, 회원 탈퇴
                    - **Refrigerator**: 식재료 관리, OCR 영수증 스캔, 레시피 추천
                    - **Diary**: 식단 다이어리 기록 및 조회
                    - **Health**: 서버 상태 및 설정 확인
                    
                    ### REF-04: 영수증 OCR 스캔
                    - **CLOVA OCR** (네이버) + **GPT-5 Nano** (OpenAI) 조합
                    - 영수증 이미지 → 구조화된 구매 이력 자동 추출
                    - 평균 비용: ~$0.03/영수증 (매우 저렴!)
                    """)
                .version("v1.0.0"))
            .servers(List.of(
                new Server().url("http://localhost:8080").description("로컬 개발 서버"),
                new Server().url("https://api.example.com").description("프로덕션 서버 (예시)")
            ));
    }
}
