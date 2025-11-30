package com.capstone.web.common.controller;

import com.capstone.web.common.S3UploadService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

import java.util.Optional;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Tag(name = "Common", description = "공통 기능 API")
@RestController
@RequestMapping("/api/v1/images")
@RequiredArgsConstructor
public class ImageController {

    private final Optional<S3UploadService> s3UploadService;

    @Operation(summary = "이미지 업로드", description = "이미지 파일을 업로드하고 S3 URL을 반환합니다.")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, String>> uploadImage(@RequestPart("file") MultipartFile file) {
        if (s3UploadService.isEmpty()) {
            return ResponseEntity.internalServerError().body(Map.of("error", "S3 서비스가 설정되지 않았습니다."));
        }
        try {
            String url = s3UploadService.get().uploadFile(file);
            return ResponseEntity.ok(Map.of("url", url));
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "이미지 업로드 실패: " + e.getMessage()));
        }
    }
}