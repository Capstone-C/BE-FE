package com.capstone.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Capstone Project API")
                        .version("1.0")
                        .description("냉장고 관리 및 레시피 추천 API\n\n" +
                                "## 주요 기능\n" +
                                "- **REF-04**: 영수증 스캔 (CLOVA OCR + GPT-5 Nano)\n" +
                                "- 냉장고 아이템 관리\n" +
                                "- 레시피 추천\n" +
                                "- 일기 관리\n" +
                                "- 회원 인증/인가"))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("JWT 토큰을 입력하세요 (Bearer 제외)")));
    }
}
