package com.capstone.web.auth.controller;

import com.capstone.web.auth.dto.LoginRequest;
import com.capstone.web.auth.dto.LoginResponse;
import com.capstone.web.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Auth", description = "인증 관련 API")
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(
        summary = "로그인",
        description = """
            사용자 인증 후 JWT 토큰을 발급합니다.
            
            **응답**:
            - accessToken: API 요청에 사용할 JWT 토큰
            - refreshToken: Access Token 갱신용 토큰
            
            **다음 단계**:
            1. 응답의 accessToken 복사
            2. Swagger 우측 상단 🔓 Authorize 클릭
            3. `Bearer {accessToken}` 형태로 입력
            """
    )
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }
}
