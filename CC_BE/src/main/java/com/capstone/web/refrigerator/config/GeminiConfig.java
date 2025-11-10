package com.capstone.web.refrigerator.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "gemini")
public class GeminiConfig {
    private String apiUrl; // base URL e.g. https://generativelanguage.googleapis.com/v1beta/models
    private String apiKey; // API key
    private String model;  // model name e.g. gemini-1.5-flash
    private Integer maxTokens; // optional max output tokens
}

