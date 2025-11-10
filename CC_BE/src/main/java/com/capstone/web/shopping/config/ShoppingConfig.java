package com.capstone.web.shopping.config;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * Shopping 모듈 설정
 * 외부 API 호출을 위한 RestTemplate 등 Bean 설정
 */
@Configuration
public class ShoppingConfig {

    /**
     * 외부 쇼핑몰 API 호출용 RestTemplate
     */
    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder.build();
    }
}
