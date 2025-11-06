# AI OCR 전환 가이드

현재 시스템은 **Tesseract** 기반 로컬 OCR을 사용하고 있습니다.  
이 문서는 **AI API 기반 OCR**로 전환하는 방법을 설명합니다.

---

## 📋 현재 아키텍처

```
[Controller] 
    ↓
[OcrService 인터페이스]  ← Strategy Pattern
    ↓
[TesseractOcrService]  ← 현재 구현체
    ↓
[ImagePreprocessor + Tesseract]
```

---

## 🔄 전환 방법

### 1️⃣ AI OCR Service 구현체 생성

`CC_BE/src/main/java/com/capstone/web/ocr/service/AiOcrService.java` 파일 생성:

```java
package com.capstone.web.ocr.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.sourceforge.tess4j.TesseractException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;

/**
 * AI API 기반 OCR 서비스 구현체
 * <p>외부 AI API (예: OpenAI Vision, Naver Clova OCR 등)를 사용하여 텍스트 추출
 */
@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "ocr.type", havingValue = "ai")  // ocr.type=ai 일 때만 활성화
public class AiOcrService implements OcrService {

    // TODO: AI API 클라이언트 주입 (예: RestTemplate, WebClient, OpenAI SDK 등)
    // private final RestTemplate restTemplate;
    // private final String aiApiUrl;
    // private final String aiApiKey;

    @Override
    public String extractText(MultipartFile imageFile) throws IOException, TesseractException {
        log.info("Starting AI OCR text extraction for file: {}", imageFile.getOriginalFilename());
        
        // TODO: AI API 호출
        // 1. 이미지를 Base64로 인코딩
        // String base64Image = encodeImageToBase64(imageFile);
        
        // 2. AI API 요청
        // AiOcrRequest request = AiOcrRequest.builder()
        //     .image(base64Image)
        //     .format("receipt")  // 영수증 전용 포맷
        //     .build();
        
        // 3. AI API 응답 받기
        // AiOcrResponse response = restTemplate.postForObject(aiApiUrl, request, AiOcrResponse.class);
        
        // 4. 파싱된 데이터 반환
        // return response.getExtractedText();
        
        throw new UnsupportedOperationException("AI OCR is not implemented yet");
    }

    @Override
    public String extractText(BufferedImage image) throws TesseractException {
        log.info("Starting AI OCR text extraction from BufferedImage");
        
        // TODO: BufferedImage를 MultipartFile 또는 Base64로 변환 후 처리
        
        throw new UnsupportedOperationException("AI OCR is not implemented yet");
    }
}
```

---

### 2️⃣ application.yml 설정 추가

```yaml
ocr:
  # OCR 타입 선택: tesseract (로컬), ai (API)
  type: tesseract  # 기본값: Tesseract 사용
  
  # Tesseract 설정 (type=tesseract 일 때)
  tesseract:
    datapath: ${TESSDATA_PREFIX:/opt/homebrew/share/tessdata}
    language: kor+eng
  
  # AI API 설정 (type=ai 일 때)
  ai:
    provider: openai  # openai, naver-clova, google-vision 등
    api-url: https://api.openai.com/v1/vision
    api-key: ${AI_OCR_API_KEY}  # 환경 변수로 관리
    timeout: 10000  # 타임아웃 (ms)
```

---

### 3️⃣ TesseractOcrService에 조건부 활성화 추가

```java
@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "ocr.type", havingValue = "tesseract", matchIfMissing = true)
// ↑ ocr.type=tesseract 이거나 설정이 없을 때 활성화 (기본값)
public class TesseractOcrService implements OcrService {
    // 기존 코드 유지
}
```

---

### 4️⃣ AI OCR 전환 테스트

#### 개발 환경에서 테스트:

```bash
# application-local.yml 또는 환境변수 설정
export AI_OCR_API_KEY="your-api-key"

# application-local.yml
ocr:
  type: ai  # AI OCR 활성화
```

#### 프로덕션 배포:

```yaml
# application-prod.yml
ocr:
  type: ai
  ai:
    provider: naver-clova  # 또는 openai
    api-url: ${AI_OCR_API_URL}
    api-key: ${AI_OCR_API_KEY}
```

---

## 🎯 핵심 변경 포인트

### ✅ 변경할 파일 (1개)

| 파일 | 변경 내용 |
|------|----------|
| `application.yml` | `ocr.type: tesseract` → `ocr.type: ai` |

### ✅ 추가할 파일 (1개)

| 파일 | 내용 |
|------|------|
| `AiOcrService.java` | AI API 호출 로직 구현 |

### ❌ 변경 불필요한 파일

- `OcrController.java` - 인터페이스로 주입받으므로 수정 불필요
- `ReceiptParserService.java` - OCR 결과를 파싱하는 로직은 동일
- 모든 테스트 파일 - `OcrService` 인터페이스로 주입받으므로 수정 불필요

---

## 📊 AI OCR 응답 형식 예시

### Option 1: 텍스트만 반환 (현재와 동일)

AI API가 단순 텍스트만 반환하는 경우, 현재 `ReceiptParserService`로 파싱:

```json
{
  "extractedText": "사과 2개 5,000원\n바나나 3개 3,500원"
}
```

### Option 2: 구조화된 데이터 반환 (추천)

AI API가 이미 파싱된 데이터를 반환하는 경우:

```json
{
  "items": [
    {
      "name": "사과",
      "quantity": 2,
      "unit": "개",
      "price": 5000
    },
    {
      "name": "바나나",
      "quantity": 3,
      "unit": "개",
      "price": 3500
    }
  ]
}
```

**→ 이 경우 `ReceiptParserService` 호출 생략 가능!**

---

## 🔍 테스트 전략

### 단위 테스트

```java
@SpringBootTest
@TestPropertySource(properties = "ocr.type=ai")
class AiOcrServiceTest {
    
    @Autowired
    private OcrService ocrService;  // AiOcrService가 주입됨
    
    @Test
    void shouldInjectAiOcrService() {
        assertThat(ocrService).isInstanceOf(AiOcrService.class);
    }
}
```

### 통합 테스트

```java
@Test
void parseReceiptFromImage_WithAiOcr() throws Exception {
    // given
    MockMultipartFile imageFile = new MockMultipartFile(...);
    
    // when
    String ocrText = ocrService.extractText(imageFile);
    List<ParsedItem> items = receiptParserService.parseReceipt(ocrText);
    
    // then
    assertThat(items).isNotEmpty();
}
```

---

## 🚀 AI API 후보

### 1. OpenAI GPT-4 Vision

```java
// 예시 코드
String prompt = """
    다음 영수증 이미지에서 식재료 정보를 추출하세요:
    - 품목명
    - 수량
    - 단위
    - 가격
    JSON 형식으로 반환하세요.
    """;

ChatCompletionRequest request = ChatCompletionRequest.builder()
    .model("gpt-4-vision-preview")
    .messages(List.of(
        new ChatMessage("user", List.of(
            new ImageContent(base64Image),
            new TextContent(prompt)
        ))
    ))
    .build();
```

### 2. Naver Clova OCR

```java
// General OCR 또는 Document OCR 사용
// https://api.ncloud-docs.com/docs/ai-naver-clovaocr
```

### 3. Google Cloud Vision API

```java
// Document Text Detection
// https://cloud.google.com/vision/docs/ocr
```

---

## ⚠️ 주의사항

1. **비용**: AI API는 요청당 과금되므로 호출 횟수 모니터링 필요
2. **속도**: 네트워크 지연으로 Tesseract보다 느릴 수 있음
3. **프라이버시**: 영수증 이미지를 외부 API로 전송하므로 개인정보 처리 방침 검토 필요
4. **Fallback**: AI API 장애 시 Tesseract로 자동 전환하는 로직 고려

---

## 📚 참고 자료

- [Tesseract OCR 설치 가이드](./TESSERACT_SETUP.md)
- [OcrService 인터페이스](./src/main/java/com/capstone/web/ocr/service/OcrService.java)
- [TesseractOcrService 구현체](./src/main/java/com/capstone/web/ocr/service/TesseractOcrService.java)
