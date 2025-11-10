package com.capstone.web.health;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Tag(name = "Health", description = "헬스체크 및 환경 설정 확인")
@RestController
@RequestMapping("/api/v1/health")
@RequiredArgsConstructor
public class HealthController {

    @Value("${gemini.api-key:}")
    private String geminiApiKey;

    @Value("${gemini.model:}")
    private String geminiModel;

    @GetMapping
    @Operation(summary = "기본 헬스체크", description = "서버가 정상 작동하는지 확인")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/ai-config")
    @Operation(
            summary = "AI 설정 확인",
            description = "Gemini API 키가 설정되어 있는지 확인합니다. " +
                    "실제 키 값은 마스킹되어 표시됩니다."
    )
    public ResponseEntity<Map<String, Object>> checkAiConfig() {
        Map<String, Object> response = new HashMap<>();
        
        // Gemini 설정 확인
        Map<String, Object> geminiConfig = new HashMap<>();
        geminiConfig.put("model", geminiModel);
        geminiConfig.put("apiKeyConfigured", !geminiApiKey.isEmpty());
        geminiConfig.put("apiKeyMasked", maskKey(geminiApiKey));
        response.put("gemini", geminiConfig);

        // AI 기능 사용 가능 여부
        boolean aiReady = !geminiApiKey.isEmpty();
        response.put("aiReady", aiReady);
        response.put("message", aiReady 
                ? "Gemini AI 기능을 사용할 수 있습니다." 
                : "AI 기능 사용을 위해 GEMINI_API_KEY 환경 변수를 설정하세요.");

        return ResponseEntity.ok(response);
    }

    /**
     * API 키를 마스킹하여 보안을 유지합니다
     */
    private String maskKey(String key) {
        if (key == null || key.isEmpty()) {
            return "(설정 안됨)";
        }
        if (key.length() <= 8) {
            return "****";
        }
        return key.substring(0, 4) + "****" + key.substring(key.length() - 4);
    }
}

