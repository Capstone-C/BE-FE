# 개발 환경 초기 데이터 정보

> **목적**: 프론트엔드 개발 및 API 테스트를 위한 샘플 데이터 제공  
> **생성 시점**: 애플리케이션 시작 시 자동 생성 (dev 프로파일)  
> **관련 파일**: `DevDataInitializer.java`

---

## 📋 목차
1. [활성화 방법](#활성화-방법)
2. [테스트 계정 정보](#테스트-계정-정보)
3. [생성되는 데이터 상세](#생성되는-데이터-상세)
4. [API 테스트 시나리오](#api-테스트-시나리오)

---

## 🚀 활성화 방법

### IntelliJ IDEA
1. Run/Debug Configurations 열기
2. Active profiles에 `dev` 입력
3. 애플리케이션 실행

### application.yml 설정
```yaml
spring:
  profiles:
    active: dev
```

### 명령줄 실행
```bash
./gradlew bootRun --args='--spring.profiles.active=dev'
```

---

## 👤 테스트 계정 정보

| 구분 | 이메일 | 비밀번호 | 닉네임 | 용도 |
|------|--------|----------|--------|------|
| 일반 사용자 1 | `test1@test.com` | `Test1234!` | 김철수 | 메인 테스트 계정 (모든 샘플 데이터 포함) |
| 일반 사용자 2 | `test2@test.com` | `Test1234!` | 이영희 | 다중 사용자 테스트용 (빈 계정) |
| 관리자 | `admin@test.com` | `Admin1234!` | 관리자 | 관리자 권한 테스트용 (빈 계정) |

### 로그인 API 예제
```bash
# test1@test.com 로그인
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test1@test.com",
    "password": "Test1234!"
  }'

# 응답 예시
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "member": {
    "id": 1,
    "email": "test1@test.com",
    "nickname": "김철수",
    "role": "USER"
  }
}
```

---

## 📊 생성되는 데이터 상세

### 1. 회원 (Members)
- **총 3명** 생성
- 모든 계정의 비밀번호는 BCrypt로 암호화되어 저장됨
- `test1@test.com` 계정만 냉장고 및 다이어리 데이터 포함

---

### 2. 카테고리 (Categories)

#### 2.1 채식 (VEGAN)
```
채식 (parent)
├── 샐러드 (child)
└── 과일 (child)
```

#### 2.2 육식 (CARNIVORE)
```
육식 (parent)
├── 소고기 (child)
└── 닭고기 (child)
```

#### 2.3 기타 카테고리
- 레시피 (RECIPE)
- 자유게시판 (FREE)
- 질문과답변 (QA)

**API 테스트**: `GET /api/v1/categories`

---

### 3. 냉장고 식재료 (Refrigerator Items)

> **소유자**: `test1@test.com` (김철수)  
> **총 개수**: 10개

#### 3.1 소비기한 임박 (3일 이내)
| 식재료명 | 수량 | 단위 | 소비기한 | 메모 |
|---------|------|------|----------|------|
| 우유 | 1 | 개 | D-2 | 개봉 후 3일 이내 섭취 |
| 요구르트 | 4 | 개 | D-3 | 딸기맛 |

#### 3.2 정상 소비기한
| 식재료명 | 수량 | 단위 | 소비기한 | 메모 |
|---------|------|------|----------|------|
| 계란 | 10 | 개 | D-14 | - |
| 당근 | 3 | 개 | D-7 | - |
| 양파 | 5 | 개 | D-30 | - |
| 두부 | 1 | 모 | D-5 | 찌개용 |
| 고구마 | 4 | 개 | D-20 | - |

#### 3.3 소비기한 없음
| 식재료명 | 수량 | 단위 | 메모 |
|---------|------|------|------|
| 쌀 | 5 | kg | 2024년산 햅쌀 |
| 간장 | 1 | 병 | - |
| 참기름 | 1 | 병 | - |

**API 테스트**:
```bash
# JWT 토큰으로 인증 필요
GET /api/v1/refrigerator/items?sortBy=expirationDate
Authorization: Bearer {access_token}
```

---

### 4. 다이어리 식단 기록 (Diary)

> **소유자**: `test1@test.com` (김철수)  
> **총 개수**: 8개 (오늘 2개, 어제 4개, 2일 전 1개)

#### 4.1 오늘 (LocalDate.now())
| 시간 | 식사 타입 | 내용 |
|------|-----------|------|
| 아침 | BREAKFAST | 계란후라이 2개, 토스트 2장, 우유 1잔 |
| 점심 | LUNCH | 김치찌개, 밥, 계란말이 |

#### 4.2 어제 (LocalDate.now() - 1일)
| 시간 | 식사 타입 | 내용 |
|------|-----------|------|
| 아침 | BREAKFAST | 시리얼, 바나나 1개 |
| 점심 | LUNCH | 된장찌개, 밥, 김치 |
| 저녁 | DINNER | 삼겹살구이, 상추쌈, 소주 2병 |
| 간식 | SNACK | 아이스크림 1개 |

#### 4.3 2일 전 (LocalDate.now() - 2일)
| 시간 | 식사 타입 | 내용 |
|------|-----------|------|
| 점심 | LUNCH | 햄버거 세트 |

**API 테스트**:
```bash
# 특정 날짜 조회
GET /api/v1/diary?date=2024-12-01
Authorization: Bearer {access_token}

# 월간 조회
GET /api/v1/diary/monthly?year=2024&month=12
Authorization: Bearer {access_token}
```

---

## 🧪 API 테스트 시나리오

### 시나리오 1: 로그인 → 냉장고 조회
```bash
# 1. 로그인
TOKEN=$(curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email": "test1@test.com", "password": "Test1234!"}' \
  | jq -r '.accessToken')

# 2. 냉장고 식재료 조회 (소비기한 임박순)
curl -X GET http://localhost:8080/api/v1/refrigerator/items \
  -H "Authorization: Bearer $TOKEN"

# 3. 냉장고 식재료 추가
curl -X POST http://localhost:8080/api/v1/refrigerator/items \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "토마토",
    "quantity": 5,
    "unit": "개",
    "expirationDate": "2024-12-15",
    "memo": "샐러드용"
  }'
```

### 시나리오 2: 다이어리 CRUD
```bash
# 1. 오늘의 식단 조회
curl -X GET "http://localhost:8080/api/v1/diary?date=$(date +%Y-%m-%d)" \
  -H "Authorization: Bearer $TOKEN"

# 2. 저녁 식단 추가
curl -X POST http://localhost:8080/api/v1/diary \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "date": "'$(date +%Y-%m-%d)'",
    "mealType": "DINNER",
    "content": "스테이크, 샐러드"
  }'

# 3. 월간 조회 (이번 달)
curl -X GET "http://localhost:8080/api/v1/diary/monthly?year=$(date +%Y)&month=$(date +%m)" \
  -H "Authorization: Bearer $TOKEN"
```

### 시나리오 3: OCR 영수증 스캔
```bash
# 영수증 이미지 업로드 및 자동 등록
curl -X POST http://localhost:8080/api/v1/ocr/scan \
  -H "Authorization: Bearer $TOKEN" \
  -F "image=@receipt.jpg"

# 응답 예시
{
  "extractedText": "사과 2개 3,000원\n바나나 1봉 2,500원...",
  "parsedItems": [
    {"name": "사과", "quantity": 2, "unit": "개", "price": 3000},
    {"name": "바나나", "quantity": 1, "unit": "봉", "price": 2500}
  ],
  "addedCount": 2,
  "failedCount": 0,
  "failedItems": []
}
```

### 시나리오 4: 다중 사용자 테스트
```bash
# test2@test.com으로 로그인 (빈 계정)
TOKEN2=$(curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email": "test2@test.com", "password": "Test1234!"}' \
  | jq -r '.accessToken')

# test2 계정의 냉장고 조회 (비어있음)
curl -X GET http://localhost:8080/api/v1/refrigerator/items \
  -H "Authorization: Bearer $TOKEN2"

# test2 계정으로 식재료 추가
curl -X POST http://localhost:8080/api/v1/refrigerator/items \
  -H "Authorization: Bearer $TOKEN2" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "양상추",
    "quantity": 1,
    "unit": "포기",
    "expirationDate": "2024-12-10"
  }'
```

---

## 🔍 데이터 초기화 확인

### 애플리케이션 로그
```log
========================================
개발용 초기 데이터 생성 시작
========================================
✓ 테스트 회원 3명 생성 완료
✓ 카테고리 생성 완료
✓ 냉장고 식재료 생성 완료 (test1@test.com)
✓ 다이어리 식단 기록 생성 완료 (test1@test.com)
========================================
개발용 초기 데이터 생성 완료!
========================================
테스트 계정 정보:
  - 일반 사용자 1: test1@test.com / Test1234!
  - 일반 사용자 2: test2@test.com / Test1234!
  - 관리자: admin@test.com / Admin1234!
========================================
```

### 데이터베이스 확인
```sql
-- 회원 확인
SELECT email, nickname FROM members;

-- 냉장고 식재료 확인 (test1@test.com)
SELECT m.email, r.name, r.quantity, r.unit, r.expiration_date
FROM refrigerator_items r
JOIN members m ON r.member_id = m.id
WHERE m.email = 'test1@test.com';

-- 다이어리 확인 (test1@test.com)
SELECT m.email, d.date, d.meal_type, d.content
FROM diary d
JOIN members m ON d.member_id = m.id
WHERE m.email = 'test1@test.com'
ORDER BY d.date DESC, d.meal_type;
```

---

## ⚠️ 주의사항

1. **프로덕션 환경에서 비활성화 필수**
   - `@Profile("dev")` 어노테이션으로 dev 프로파일에서만 실행됨
   - 프로덕션 배포 시 `spring.profiles.active=prod` 설정 권장

2. **데이터 중복 방지**
   - 애플리케이션 재시작 시 기존 데이터가 있으면 초기화하지 않음
   - `memberRepository.count() > 0` 체크로 중복 방지

3. **초기 데이터 재생성 방법**
   ```bash
   # H2 인메모리 DB 사용 시: 애플리케이션 재시작
   # MySQL 사용 시: 테이블 삭제 후 재시작
   DROP TABLE IF EXISTS diary;
   DROP TABLE IF EXISTS refrigerator_items;
   DROP TABLE IF EXISTS member_password_history;
   DROP TABLE IF EXISTS password_reset_tokens;
   DROP TABLE IF EXISTS member_blocks;
   DROP TABLE IF EXISTS members;
   DROP TABLE IF EXISTS category;
   ```

4. **비밀번호 정책**
   - 모든 테스트 계정 비밀번호는 동일한 정책 적용
   - 최소 8자, 대문자, 소문자, 숫자, 특수문자 포함

---

## 📝 커스터마이징

초기 데이터를 수정하려면 `DevDataInitializer.java` 파일을 편집하세요.

### 예시: 식재료 추가
```java
refrigeratorItemRepository.save(RefrigeratorItem.builder()
        .member(member)
        .name("새로운 식재료")
        .quantity(1)
        .unit("개")
        .expirationDate(LocalDate.now().plusDays(7))
        .memo("커스텀 메모")
        .build());
```

### 예시: 회원 추가
```java
createTestMember("test3@test.com", "박민수", "Test1234!");
```

---

## 📚 관련 문서
- [Refrigerator API 명세](../docs/API_REFRIGERATOR.md)
- [Diary API 명세](../docs/API_DIARY.md)
- [Auth API 명세](../docs/API_AUTH.md)
- [OCR API 명세](../docs/API_OCR.md)
