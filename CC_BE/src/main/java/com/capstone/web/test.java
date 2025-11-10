package com.capstone.web;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;

/**
 * Gemini AI 이미지 인식 테스트
 * 
 * 사용 방법:
 * 1. Google AI Studio에서 API 키 발급: https://makersuite.google.com/app/apikey
 * 2. 환경변수 설정: export GEMINI_API_KEY='your-api-key'
 * 3. 테스트 이미지를 uploads/profile/test-receipt.jpg에 저장
 * 4. 이 파일을 실행: java com.capstone.web.test
 */
public class test {
    private static final String API_ENDPOINT = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent";
    
    public static void main(String[] args) {
        // 1. API 키 확인
        String apiKey = System.getenv("GEMINI_API_KEY");
        if (apiKey == null || apiKey.isEmpty()) {
            System.err.println("❌ GEMINI_API_KEY 환경변수를 설정해주세요!");
            System.err.println("터미널에서: export GEMINI_API_KEY='your-api-key'");
            System.err.println("API 키 발급: https://makersuite.google.com/app/apikey");
            return;
        }

        // 2. 테스트 이미지 경로
        String imagePath = "uploads/profile/image2.jpg";
        Path path = Path.of(imagePath);
        
        if (!Files.exists(path)) {
            System.err.println("❌ 이미지 파일을 찾을 수 없습니다: " + imagePath);
            System.err.println("테스트할 영수증 이미지를 해당 경로에 저장해주세요.");
            return;
        }

        try {
            System.out.println("🔍 Gemini AI 이미지 분석 시작...");
            System.out.println("📁 파일: " + imagePath);
            
            // 3. 이미지를 Base64로 인코딩
            byte[] imageBytes = Files.readAllBytes(path);
            String base64Image = Base64.getEncoder().encodeToString(imageBytes);
            
            // 4. MIME 타입 결정
            String mimeType = imagePath.toLowerCase().endsWith(".png") ? "image/png" : "image/jpeg";
            
            // 5. JSON 요청 바디 생성
            String requestBody = String.format("""
                {
                  "contents": [{
                    "parts": [
                      {
                        "text": "이 이미지는 영수증입니다. 영수증에서 식재료로 보이는 품목들을 추출해주세요. 각 품목은 한 줄에 하나씩 출력하고, 가격이나 할인 정보는 제외해주세요. 예시:\\n- 양파\\n- 계란\\n- 우유"
                      },
                      {
                        "inline_data": {
                          "mime_type": "%s",
                          "data": "%s"
                        }
                      }
                    ]
                  }]
                }
                """, mimeType, base64Image);
            
            // 6. HTTP 요청 생성
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_ENDPOINT + "?key=" + apiKey))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();
            
            // 7. API 호출
            System.out.println("⏳ API 호출 중...");
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            
            // 8. 응답 확인
            if (response.statusCode() == 200) {
                System.out.println("\n✅ 분석 완료!\n");
                System.out.println("=== API 응답 ===");
                
                // JSON 파싱 (간단한 방법)
                String responseBody = response.body();
                System.out.println(responseBody);
                
                // text 부분 추출
                int textStart = responseBody.indexOf("\"text\":") + 9;
                if (textStart > 8) {
                    int textEnd = responseBody.indexOf("\"", textStart + 1);
                    while (textEnd > 0 && responseBody.charAt(textEnd - 1) == '\\') {
                        textEnd = responseBody.indexOf("\"", textEnd + 1);
                    }
                    
                    if (textEnd > textStart) {
                        String extractedText = responseBody.substring(textStart, textEnd);
                        // 이스케이프 문자 처리
                        extractedText = extractedText.replace("\\n", "\n");
                        
                        System.out.println("\n=== 추출된 식재료 ===");
                        System.out.println(extractedText);
                        System.out.println("===================\n");
                    }
                }
                
            } else {
                System.err.println("❌ API 호출 실패!");
                System.err.println("상태 코드: " + response.statusCode());
                System.err.println("응답: " + response.body());
            }
            
        } catch (IOException e) {
            System.err.println("❌ 파일 읽기 실패: " + e.getMessage());
            e.printStackTrace();
        } catch (InterruptedException e) {
            System.err.println("❌ API 호출 중단: " + e.getMessage());
            e.printStackTrace();
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            System.err.println("❌ 예상치 못한 오류: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
