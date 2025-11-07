package com.capstone.web.health;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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

    @Value("${ocr.clova.api-url}")
    private String clovaApiUrl;

    @Value("${ocr.clova.secret-key}")
    private String clovaSecretKey;

    @Value("${openai.api-key}")
    private String openaiApiKey;

    @Value("${openai.model}")
    private String openaiModel;

    @GetMapping
    @Operation(summary = "기본 헬스체크", description = "서버가 정상 작동하는지 확인")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/ocr-config")
    @Operation(
            summary = "OCR 설정 확인 (REF-04)",
            description = "CLOVA OCR 및 OpenAI API 키가 설정되어 있는지 확인합니다. " +
                    "실제 키 값은 마스킹되어 표시됩니다."
    )
    public ResponseEntity<Map<String, Object>> checkOcrConfig() {
        Map<String, Object> response = new HashMap<>();
        
        // CLOVA OCR 설정 확인
        Map<String, Object> clovaConfig = new HashMap<>();
        clovaConfig.put("apiUrl", clovaApiUrl);
        clovaConfig.put("secretKeyConfigured", !clovaSecretKey.isEmpty());
        clovaConfig.put("secretKeyMasked", maskKey(clovaSecretKey));
        response.put("clova", clovaConfig);

        // OpenAI 설정 확인
        Map<String, Object> openaiConfig = new HashMap<>();
        openaiConfig.put("model", openaiModel);
        openaiConfig.put("apiKeyConfigured", !openaiApiKey.isEmpty());
        openaiConfig.put("apiKeyMasked", maskKey(openaiApiKey));
        response.put("openai", openaiConfig);

        // REF-04 사용 가능 여부
        boolean ref04Ready = !clovaSecretKey.isEmpty() && !openaiApiKey.isEmpty();
        response.put("ref04Ready", ref04Ready);
        response.put("message", ref04Ready 
                ? "REF-04 영수증 스캔 기능을 사용할 수 있습니다." 
                : "REF-04 사용을 위해 CLOVA_OCR_SECRET_KEY 및 OPENAI_API_KEY 환경 변수를 설정하세요.");

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
