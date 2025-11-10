package com.capstone.web.shopping.config;

import org.springframework.context.annotation.Configuration;

/**
 * Shopping 모듈 설정
 * 
 * Note: RestTemplate은 WebConfig에서 전역 빈으로 정의되어 있어
 * Shopping 모듈에서도 의존성 주입을 통해 사용 가능합니다.
 */
@Configuration
public class ShoppingConfig {
    // RestTemplate Bean removed - using global bean from WebConfig
}
