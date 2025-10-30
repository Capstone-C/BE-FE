package com.capstone.web.ocr.service;

import com.capstone.web.ocr.dto.OcrDto.ParsedItem;
import com.capstone.web.ocr.dto.OcrDto.ScanResponse;
import com.capstone.web.refrigerator.dto.RefrigeratorDto.BulkCreateRequest;
import com.capstone.web.refrigerator.dto.RefrigeratorDto.BulkCreateResponse;
import com.capstone.web.refrigerator.dto.RefrigeratorDto.CreateRequest;
import com.capstone.web.refrigerator.service.RefrigeratorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.sourceforge.tess4j.TesseractException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * OCR 파이프라인 서비스
 * 이미지 → OCR → 파싱 → 냉장고 등록의 전체 플로우를 처리합니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OcrPipelineService {

    private final TesseractOcrService tesseractOcrService;
    private final ReceiptParserService receiptParserService;
    private final RefrigeratorService refrigeratorService;

    /**
     * 영수증 이미지를 스캔하여 식재료를 자동으로 냉장고에 등록
     * 
     * @param memberId 회원 ID
     * @param imageFile 영수증 이미지 파일
     * @return 스캔 결과 (추출된 텍스트, 파싱된 식재료, 등록 결과)
     * @throws IOException 파일 읽기 오류
     * @throws TesseractException OCR 처리 오류
     */
    @Transactional
    public ScanResponse scanAndAddToRefrigerator(Long memberId, MultipartFile imageFile) 
            throws IOException, TesseractException {
        
        log.info("Starting OCR pipeline for member: {}, file: {}", 
                 memberId, imageFile.getOriginalFilename());
        
        // 1단계: OCR로 텍스트 추출
        String extractedText = tesseractOcrService.extractText(imageFile);
        log.info("Extracted text length: {}", extractedText.length());
        
        // 2단계: 텍스트에서 식재료 정보 파싱
        List<ParsedItem> parsedItems = receiptParserService.parseReceipt(extractedText);
        log.info("Parsed {} items from receipt", parsedItems.size());
        
        // 3단계: 파싱된 식재료를 냉장고에 일괄 등록
        BulkCreateResponse bulkResult = addParsedItemsToRefrigerator(memberId, parsedItems);
        
        // 4단계: 최종 결과 반환
        ScanResponse response = ScanResponse.builder()
                .extractedText(extractedText)
                .parsedItems(parsedItems)
                .addedCount(bulkResult.getSuccessCount())
                .failedCount(bulkResult.getFailCount())
                .failedItems(bulkResult.getFailedItems())
                .build();
        
        log.info("OCR pipeline completed. Added: {}, Failed: {}", 
                 response.getAddedCount(), response.getFailedCount());
        
        return response;
    }

    /**
     * 파싱된 식재료를 냉장고에 일괄 등록
     */
    private BulkCreateResponse addParsedItemsToRefrigerator(Long memberId, List<ParsedItem> parsedItems) {
        // ParsedItem을 CreateRequest로 변환
        List<CreateRequest> createRequests = parsedItems.stream()
                .map(item -> CreateRequest.builder()
                        .name(item.getName())
                        .quantity(item.getQuantity() != null ? item.getQuantity() : 1)
                        .unit(item.getUnit())
                        .expirationDate(null) // OCR에서는 소비기한 추출 불가
                        .memo("영수증 스캔으로 자동 등록")
                        .build())
                .collect(Collectors.toList());
        
        // 일괄 등록 요청 생성
        BulkCreateRequest bulkRequest = BulkCreateRequest.builder()
                .items(createRequests)
                .build();
        
        // 냉장고 서비스를 통해 일괄 등록
        return refrigeratorService.addItemsBulk(memberId, bulkRequest);
    }
}
