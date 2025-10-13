package com.capstone.web.config;

import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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
                .title("캡스톤 회원 API 문서")
                .description("모든 회원 관련 API. JWT 인증 필요시 우측 Authorize 버튼 클릭 후 토큰 입력. 모든 응답/요청 예시는 실제 값 기반입니다.")
                .version("v1.0.0"));
    }
}
