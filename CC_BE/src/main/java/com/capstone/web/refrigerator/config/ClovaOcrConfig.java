package com.capstone.web.refrigerator.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "ocr.clova")
public class ClovaOcrConfig {
    private String apiUrl;
    private String secretKey;
}
