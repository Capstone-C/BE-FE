package com.capstone.web.member.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
public class ProfileImageStorage {

    private final Path profileImageDir;

    // 생성자를 수정하여 profile 이미지만을 위한 특정 경로를 설정합니다.
    public ProfileImageStorage(@Value("${app.upload-dir}") String uploadDir) throws IOException {
        // 'uploads/' + 'profile/' = 'uploads/profile/'
        this.profileImageDir = Path.of(uploadDir, "profile");
        if (!Files.exists(profileImageDir)) {
            Files.createDirectories(profileImageDir);
        }
    }

    public String store(Long memberId, MultipartFile file) throws IOException {
        String original = file.getOriginalFilename();
        String ext = "";
        if (original != null && original.contains(".")) {
            ext = original.substring(original.lastIndexOf('.'));
        }
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String filename = memberId + "_" + timestamp + ext;

        // 저장 경로를 profileImageDir 기준으로 변경합니다.
        Path target = profileImageDir.resolve(filename);

        Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

        // 반환되는 URL 경로는 기존과 동일하게 유지합니다.
        return "/static/profile/" + filename;
    }
}