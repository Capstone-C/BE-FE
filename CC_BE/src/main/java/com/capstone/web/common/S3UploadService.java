package com.capstone.web.common;

import io.awspring.cloud.s3.S3Template;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@ConditionalOnBean(S3Template.class)
public class S3UploadService {

    private final S3Template s3Template; // Spring Cloud AWS 3.0의 핵심

    @Value("${spring.cloud.aws.s3.bucket}")
    private String bucket;

    public String uploadFile(MultipartFile file) throws IOException {
        // 1. 파일명 중복 방지 (UUID)
        String originalFilename = file.getOriginalFilename();
        String uuid = UUID.randomUUID().toString();
        String key = uuid + "_" + originalFilename;

        // 2. S3에 업로드
        s3Template.upload(bucket, key, file.getInputStream());

        // 3. 업로드된 URL 반환
        return s3Template.download(bucket, key).getURL().toString();
    }
}