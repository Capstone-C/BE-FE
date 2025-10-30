package com.capstone.web.ocr.controller;

import com.capstone.web.ocr.dto.OcrDto.ScanResponse;
import com.capstone.web.ocr.exception.InvalidImageFileException;
import com.capstone.web.ocr.exception.OcrProcessingException;
import com.capstone.web.ocr.service.OcrPipelineService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.sourceforge.tess4j.TesseractException;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * OCR API 컨트롤러
 * 영수증 이미지를 스캔하여 식재료를 자동으로 등록합니다.
 */
@Slf4j
@Tag(name = "OCR", description = "영수증 스캔 및 OCR API")
@RestController
@RequestMapping("/api/v1/ocr")
@RequiredArgsConstructor
@SecurityRequirement(name = "JWT")
public class OcrController {

    private final OcrPipelineService ocrPipelineService;
    
    // 허용할 이미지 파일 확장자
    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList("jpg", "jpeg", "png", "gif", "bmp");
    
    // 최대 파일 크기 (10MB)
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;

    @Operation(
        summary = "영수증 이미지 스캔",
        description = """
            영수증 이미지를 스캔하여 식재료를 자동으로 냉장고에 등록합니다.
            
            **처리 과정:**
            1. 이미지에서 텍스트 추출 (OCR)
            2. 추출된 텍스트에서 식재료 정보 파싱
            3. 파싱된 식재료를 냉장고에 자동 등록
            
            **사용 예시:**
            - 장 본 후 영수증을 촬영하여 업로드
            - 자동으로 구매한 식재료가 냉장고에 등록됨
            - 등록 실패한 항목은 수동으로 추가 가능
            
            **주의사항:**
            - 지원 형식: JPG, PNG, GIF, BMP
            - 최대 파일 크기: 10MB
            - OCR 정확도는 이미지 품질에 따라 달라질 수 있음
            - 소비기한은 OCR로 추출할 수 없어 수동 입력 필요
            """
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "스캔 성공",
            content = @Content(schema = @Schema(implementation = ScanResponse.class))
        ),
        @ApiResponse(responseCode = "400", description = "유효하지 않은 이미지 파일"),
        @ApiResponse(responseCode = "401", description = "인증 실패"),
        @ApiResponse(responseCode = "500", description = "OCR 처리 오류")
    })
    @PostMapping(value = "/scan", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ScanResponse> scanReceipt(
            @Parameter(description = "영수증 이미지 파일", required = true)
            @RequestParam("image") MultipartFile imageFile,
            Authentication authentication
    ) {
        Long memberId = extractMemberId(authentication);
        
        log.info("OCR scan request from member: {}, file: {}", 
                 memberId, imageFile.getOriginalFilename());
        
        // 파일 유효성 검증
        validateImageFile(imageFile);
        
        try {
            // OCR 파이프라인 실행
            ScanResponse response = ocrPipelineService.scanAndAddToRefrigerator(memberId, imageFile);
            
            log.info("OCR scan completed for member: {}. Added: {}, Failed: {}", 
                     memberId, response.getAddedCount(), response.getFailedCount());
            
            return ResponseEntity.ok(response);
            
        } catch (IOException e) {
            log.error("Failed to read image file: {}", imageFile.getOriginalFilename(), e);
            throw new InvalidImageFileException("이미지 파일을 읽을 수 없습니다: " + e.getMessage(), e);
            
        } catch (TesseractException e) {
            log.error("OCR processing failed for file: {}", imageFile.getOriginalFilename(), e);
            throw new OcrProcessingException("OCR 처리 중 오류가 발생했습니다: " + e.getMessage(), e);
        }
    }

    /**
     * 이미지 파일 유효성 검증
     */
    private void validateImageFile(MultipartFile file) {
        // 파일이 비어있는지 확인
        if (file.isEmpty()) {
            throw new InvalidImageFileException("이미지 파일이 비어있습니다");
        }
        
        // 파일 크기 확인
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new InvalidImageFileException(
                String.format("파일 크기가 너무 큽니다. 최대 크기: %dMB", MAX_FILE_SIZE / 1024 / 1024)
            );
        }
        
        // 파일 확장자 확인
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isEmpty()) {
            throw new InvalidImageFileException("파일 이름이 유효하지 않습니다");
        }
        
        String extension = getFileExtension(originalFilename).toLowerCase();
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new InvalidImageFileException(
                String.format("지원하지 않는 파일 형식입니다. 지원 형식: %s", String.join(", ", ALLOWED_EXTENSIONS))
            );
        }
    }

    /**
     * 파일 확장자 추출
     */
    private String getFileExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex == -1) {
            return "";
        }
        return filename.substring(lastDotIndex + 1);
    }

    /**
     * Authentication에서 회원 ID 추출
     */
    private Long extractMemberId(Authentication authentication) {
        return Long.parseLong(authentication.getName());
    }
}
