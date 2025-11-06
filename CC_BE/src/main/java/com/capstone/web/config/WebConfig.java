package com.capstone.web.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    // application.yml에 설정한 값을 주입받습니다.
    @Value("${app.upload-dir}")
    private String uploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // '/static/**' 형태의 URL 요청이 오면
        registry.addResourceHandler("/static/**")
                // 로컬 파일 시스템의 'uploads/' 디렉토리에서 파일을 찾아서 제공합니다.
                // 'file:' 접두사는 클래스패스가 아닌 파일 시스템 경로임을 명시합니다.
                // 마지막에 '/'를 붙여 디렉토리임을 나타냅니다.
                .addResourceLocations("file:" + uploadDir);
    }

    /**
     * RestTemplate Bean (REF-04: CLOVA OCR, OpenAI API 호출용)
     */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}