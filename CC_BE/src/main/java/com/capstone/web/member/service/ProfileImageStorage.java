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

    private final Path baseDir;

    public ProfileImageStorage(@Value("${app.profile-image-dir:uploads/profile}") String dir) throws IOException {
        this.baseDir = Path.of(dir);
        if (!Files.exists(baseDir)) {
            Files.createDirectories(baseDir);
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
        Path target = baseDir.resolve(filename);
        Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
        return "/static/profile/" + filename; // TODO: 실서비스 경로 매핑 필요
    }
}
