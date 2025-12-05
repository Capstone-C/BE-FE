package com.capstone.web.common;

import io.awspring.cloud.s3.S3Template;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Service
public class S3UploadService {

    private final S3Template s3Template;
    private final String bucket;

    // Constructor injection for both S3Template and the bucket name
    public S3UploadService(S3Template s3Template, @Value("${spring.cloud.aws.s3.bucket}") String bucket) {
        this.s3Template = s3Template;
        this.bucket = bucket;
    }

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