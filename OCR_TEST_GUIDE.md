# OCR 기능 테스트 가이드 (REF-04)

## 준비사항

### 1. API 키 발급

#### CLOVA OCR (Naver Cloud Platform)
1. [Naver Cloud Platform](https://www.ncloud.com/) 가입
2. [OCR 서비스](https://www.ncloud.com/product/aiService/ocr) 신청
3. Console > AI·NAVER API > OCR > General OCR 선택
4. 도메인 생성 및 Secret Key 복사

#### OpenAI API
1. [OpenAI Platform](https://platform.openai.com/) 가입
2. [API Keys](https://platform.openai.com/api-keys) 페이지에서 키 생성
3. API 키 복사 (sk-proj-로 시작)

### 2. 환경 변수 설정

#### 로컬 개발 (Mac/Linux)
```bash
# ~/.zshrc 또는 ~/.bashrc에 추가
export CLOVA_OCR_API_URL="https://your-domain.apigw.ntruss.com/custom/v1/YOUR_DOMAIN/general"
export CLOVA_OCR_SECRET_KEY="your-clova-secret-key"
export OPENAI_API_KEY="sk-proj-your-openai-api-key"

# 적용
source ~/.zshrc
```

#### Docker Compose 사용 시
`compose.yaml` 파일의 backend 서비스에 환경 변수가 이미 설정되어 있습니다:
```yaml
environment:
  CLOVA_OCR_API_URL: ${CLOVA_OCR_API_URL:-}
  CLOVA_OCR_SECRET_KEY: ${CLOVA_OCR_SECRET_KEY:-}
  OPENAI_API_KEY: ${OPENAI_API_KEY:-}
```

---

## 테스트 방법

### 1. 컨테이너 실행

```bash
# 프로젝트 루트에서
cd /Users/pilt/project-collection/capstone

# 환경 변수 확인
echo $CLOVA_OCR_API_URL
echo $CLOVA_OCR_SECRET_KEY
echo $OPENAI_API_KEY

# Docker Compose로 전체 스택 실행
docker compose up --build

# 또는 백그라운드 실행
docker compose up -d --build
```

### 2. 서버 헬스체크

브라우저에서 접속:
- **Swagger UI**: http://localhost:8080/swagger-ui/index.html
- **헬스체크**: http://localhost:8080/api/v1/health
- **OCR 설정 확인**: http://localhost:8080/api/v1/health/ocr-config

또는 curl:
```bash
# 기본 헬스체크
curl http://localhost:8080/api/v1/health

# OCR 설정 확인
curl http://localhost:8080/api/v1/health/ocr-config | jq
```

**예상 응답 (설정 완료 시)**:
```json
{
  "clova": {
    "apiUrl": "https://your-domain.apigw.ntruss.com/...",
    "secretKeyConfigured": true,
    "secretKeyMasked": "abcd****efgh"
  },
  "openai": {
    "model": "gpt-5-nano",
    "apiKeyConfigured": true,
    "apiKeyMasked": "sk-p****s4A"
  },
  "ref04Ready": true,
  "message": "REF-04 영수증 스캔 기능을 사용할 수 있습니다."
}
```

### 3. 회원 가입 및 로그인

#### 3-1. 회원 가입
```bash
curl -X POST http://localhost:8080/api/v1/auth/signup \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "test1234!",
    "email": "test@example.com",
    "nickname": "테스트유저"
  }'
```

#### 3-2. 로그인
```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "test1234!"
  }'
```

**응답에서 JWT 토큰 복사**:
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
  "message": "로그인 성공"
}
```

### 4. 영수증 스캔 테스트 (REF-04)

#### 테스트용 영수증 이미지 준비
편의점 영수증 사진을 찍거나 인터넷에서 샘플 이미지를 다운로드하세요.

#### curl로 테스트
```bash
# JWT 토큰을 환경 변수로 설정
export JWT_TOKEN="eyJhbGciOiJIUzI1NiJ9..."

# 영수증 스캔 요청
curl -X POST http://localhost:8080/api/v1/refrigerator/scan/purchase-history \
  -H "Authorization: Bearer $JWT_TOKEN" \
  -F "imageFile=@/path/to/receipt.jpg"
```

#### Swagger UI로 테스트 (추천)
1. http://localhost:8080/swagger-ui/index.html 접속
2. 우측 상단 **Authorize** 버튼 클릭
3. JWT 토큰 입력 (Bearer 제외)
4. **Refrigerator** 섹션 확장
5. **POST /api/v1/refrigerator/scan/purchase-history** 선택
6. **Try it out** 클릭
7. 영수증 이미지 파일 업로드
8. **Execute** 클릭

**예상 응답**:
```json
{
  "store": "CU 편의점",
  "purchaseDate": "2025-11-06",
  "items": [
    {
      "name": "삼각김밥 참치마요",
      "quantity": 2,
      "price": 1500,
      "category": "READY_TO_EAT",
      "expirationDate": "2025-11-08"
    },
    {
      "name": "바나나우유",
      "quantity": 1,
      "price": 1200,
      "category": "BEVERAGE",
      "expirationDate": "2025-11-13"
    }
  ],
  "totalAmount": 4200,
  "rawOcrText": "CU 편의점\n2025-11-06 14:30\n삼각김밥 참치마요 1,500원 x 2\n..."
}
```

---

## 트러블슈팅

### 1. "REF-04 기능을 사용할 수 없습니다"
- **원인**: API 키 미설정
- **해결**: 환경 변수 확인 및 컨테이너 재시작
```bash
docker compose down
# 환경 변수 재설정
docker compose up -d --build
```

### 2. CLOVA OCR 401 Unauthorized
- **원인**: Secret Key 오류
- **해결**: Naver Cloud Console에서 키 재확인

### 3. OpenAI 429 Too Many Requests
- **원인**: API 사용량 초과
- **해결**: [OpenAI Usage](https://platform.openai.com/usage) 페이지에서 한도 확인

### 4. Swagger UI가 안 보임
- **원인**: 서버 미실행 또는 포트 충돌
- **해결**: 
```bash
# 로그 확인
docker compose logs backend

# 포트 확인
lsof -i :8080
```

### 5. JWT 토큰 인증 실패
- **원인**: 토큰 만료 (기본 1시간)
- **해결**: 재로그인하여 새 토큰 발급

---

## API 엔드포인트 요약

| Method | Endpoint | 설명 | 인증 필요 |
|--------|----------|------|-----------|
| GET | `/api/v1/health` | 헬스체크 | ❌ |
| GET | `/api/v1/health/ocr-config` | OCR 설정 확인 | ❌ |
| POST | `/api/v1/auth/signup` | 회원가입 | ❌ |
| POST | `/api/v1/auth/login` | 로그인 | ❌ |
| POST | `/api/v1/refrigerator/scan/purchase-history` | 영수증 스캔 (REF-04) | ✅ |
| GET | `/api/v1/refrigerator/items` | 냉장고 아이템 조회 | ✅ |

---

## 다음 단계

1. ✅ API 키 발급 및 설정
2. ✅ Docker Compose로 실행
3. ✅ Swagger UI 접속 확인
4. ✅ 회원 가입/로그인
5. ✅ 영수증 스캔 테스트
6. 🔄 냉장고 아이템 관리 테스트
7. 🔄 레시피 추천 기능 테스트
