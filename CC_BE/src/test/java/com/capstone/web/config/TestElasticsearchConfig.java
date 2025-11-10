package com.capstone.web.config;

import com.capstone.web.shopping.repository.ProductSearchRepository;
import com.capstone.web.shopping.scheduler.ProductCollectorScheduler;
import com.capstone.web.shopping.service.ProductIndexingService;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.data.elasticsearch.ElasticsearchDataAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import static org.mockito.Mockito.mock;

/**
 * Spring Boot Test 환경에서 Elasticsearch 의존성 제거를 위한 설정
 * 
 * Spring Boot 3.4 이후 @MockBean deprecated 대응: @Bean으로 Mock 객체 직접 제공
 * @Configuration + @Profile("test")로 자동 로드되며, 모든 테스트에서 Elasticsearch Mock 빈 사용
 */
@Configuration
@Profile("test")
@EnableAutoConfiguration(exclude = {ElasticsearchDataAutoConfiguration.class})
public class TestElasticsearchConfig {

    @Bean
    @Primary
    public ProductSearchRepository productSearchRepository() {
        return mock(ProductSearchRepository.class);
    }

    @Bean
    @Primary
    public ProductIndexingService productIndexingService() {
        return mock(ProductIndexingService.class);
    }

    @Bean
    @Primary
    public ProductCollectorScheduler productCollectorScheduler() {
        return mock(ProductCollectorScheduler.class);
    }
}
