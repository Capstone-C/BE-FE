# 🔍 Tesseract OCR 설치 가이드

## 📌 Eclipse Temurin이란?

**Eclipse Temurin** = OpenJDK의 프로덕션 레디 배포판 (구 AdoptOpenJDK)
- 공식 JDK 17 런타임
- Dockerfile에서 사용: `FROM eclipse-temurin:17-jdk`
- Debian 기반이라 `apt-get`으로 패키지 설치 가능

---

## 🎯 설치가 필요한 3가지 환경

### 1️⃣ **로컬 개발 환경** (macOS) ✅ 완료

```bash
# Homebrew로 설치
brew install tesseract tesseract-lang

# 설치 확인
tesseract --version
# Output: tesseract 5.5.1

# 한국어 데이터 확인
ls /opt/homebrew/share/tessdata/kor.traineddata
# Output: /opt/homebrew/share/tessdata/kor.traineddata
```

**설치 위치**:
- 실행 파일: `/opt/homebrew/bin/tesseract`
- 언어 데이터: `/opt/homebrew/share/tessdata/`

**용도**:
- ✅ IntelliJ/VSCode에서 Spring Boot 로컬 실행
- ✅ 단위 테스트 실행 (`./gradlew test`)
- ✅ 디버깅 및 개발

**설정 파일**:
```yaml
# CC_BE/src/main/resources/application.yml
ocr:
  tesseract:
    datapath: ${TESSDATA_PREFIX:/opt/homebrew/share/tessdata}
    language: kor+eng
```

---

### 2️⃣ **Docker Compose 환경** ✅ 완료

```dockerfile
# CC_BE/Dockerfile
FROM eclipse-temurin:17-jdk

# Tesseract 설치
RUN apt-get update && \
    apt-get install -y \
    tesseract-ocr \
    tesseract-ocr-kor \
    libtesseract-dev \
    && apt-get clean \
    && rm -rf /var/lib/apt/lists/*

# 환경변수 설정
ENV TESSDATA_PREFIX=/usr/share/tesseract-ocr/5/tessdata
```

**설정 파일**:
```yaml
# compose.yaml
services:
  backend:
    environment:
      TESSDATA_PREFIX: /usr/share/tesseract-ocr/5/tessdata
```

**실행 방법**:
```bash
# MySQL만 실행 (로컬 개발 시)
docker-compose up -d mysql

# 전체 스택 실행 (통합 테스트 시)
docker-compose up --build
```

**자동 설치**: 
- ✅ `docker-compose up --build` 시 Tesseract 자동 설치
- ✅ 한국어 언어 데이터 자동 다운로드

---

### 3️⃣ **GitHub Actions CI/CD** ✅ 완료

```yaml
# .github/workflows/backend-ci.yml
steps:
  # Tesseract 설치 단계
  - name: Install Tesseract OCR
    run: |
      sudo apt-get update
      sudo apt-get install -y tesseract-ocr tesseract-ocr-kor
      tesseract --version
      echo "TESSDATA_PREFIX=/usr/share/tesseract-ocr/5/tessdata" >> $GITHUB_ENV
  
  # 테스트 설정 파일 생성
  - name: Create test application.yml
    run: |
      cat > src/test/resources/application-test.yml << EOF
      ocr:
        tesseract:
          datapath: /usr/share/tesseract-ocr/5/tessdata
          language: kor+eng
      EOF
```

**실행 조건**:
- ✅ Pull Request 생성/업데이트 시
- ✅ main, develop 브랜치 push 시
- ✅ `CC_BE/` 폴더 변경 시에만 실행

**자동 테스트**:
- 모든 단위 테스트 자동 실행
- OCR 파싱 테스트 포함
- 테스트 결과 자동 업로드

---

## 🧪 테스트 실행 방법

### 로컬에서 OCR 테스트

```bash
cd CC_BE

# 냉장고 기능 테스트
./gradlew test --tests RefrigeratorControllerTest
./gradlew test --tests RefrigeratorServiceTest

# OCR 파싱 테스트 (현재 @Disabled 상태)
./gradlew test --tests ReceiptParserServiceTest

# 전체 테스트
./gradlew test
```

### Docker Compose로 통합 테스트

```bash
# MySQL 컨테이너만 실행 (로컬 개발용)
docker-compose up -d mysql

# Spring Boot 로컬 실행 (IntelliJ)
# → MySQL 컨테이너에 자동 연결

# 전체 스택 실행 (통합 테스트용)
docker-compose up --build

# 백엔드 로그 확인
docker-compose logs -f backend

# 종료
docker-compose down
```

---

## 📊 Tesseract 경로 정리

| 환경 | Tesseract 실행 파일 | 언어 데이터 경로 |
|------|---------------------|------------------|
| **macOS (Homebrew)** | `/opt/homebrew/bin/tesseract` | `/opt/homebrew/share/tessdata/` |
| **Docker (Debian)** | `/usr/bin/tesseract` | `/usr/share/tesseract-ocr/5/tessdata/` |
| **GitHub Actions (Ubuntu)** | `/usr/bin/tesseract` | `/usr/share/tesseract-ocr/5/tessdata/` |

---

## 🔧 환경변수 우선순위

```yaml
# OcrConfig.java에서 사용하는 우선순위
1. TESSDATA_PREFIX 환경변수 (Docker Compose, GitHub Actions)
2. ocr.tesseract.datapath (application.yml)
3. 시스템 기본 경로 (Homebrew 설치 시 자동 감지)
```

**설정 예시**:
```yaml
# application.yml (로컬 개발용)
ocr:
  tesseract:
    datapath: ${TESSDATA_PREFIX:/opt/homebrew/share/tessdata}
    language: kor+eng
```

```yaml
# Docker Compose
environment:
  TESSDATA_PREFIX: /usr/share/tesseract-ocr/5/tessdata
```

---

## ✅ 설치 확인 체크리스트

### 로컬 macOS
- [x] Homebrew 설치 완료
- [x] Tesseract 5.5.1 설치 완료
- [x] 한국어 언어 데이터 설치 완료
- [x] `application.yml`에 OCR 설정 추가
- [x] `application-test.yml`에 OCR 설정 추가

### Docker Compose
- [x] `Dockerfile`에 Tesseract 설치 스크립트 추가
- [x] `compose.yaml`에 TESSDATA_PREFIX 환경변수 설정
- [x] eclipse-temurin:17-jdk 베이스 이미지 사용

### GitHub Actions
- [x] `backend-ci.yml`에 Tesseract 설치 단계 추가
- [x] 테스트용 application.yml 자동 생성
- [x] TESSDATA_PREFIX 환경변수 설정

---

## 🚀 다음 단계

### 1. OCR 테스트 활성화
```java
// ReceiptParserServiceTest.java
@Disabled  // ← 이 줄 제거
@DisplayName("영수증 OCR + 파싱 통합 테스트")
class ReceiptParserServiceTest {
```

### 2. 테스트 이미지 준비
```bash
mkdir -p CC_BE/test-image
# 영수증 이미지 5개 추가:
# - image.png
# - image2.png
# - image3.png
# - image4.png
# - image5.png
```

### 3. 전체 테스트 실행
```bash
./gradlew clean test
```

### 4. GitHub에 푸시하여 CI 테스트
```bash
git add .
git commit -m "feat: Tesseract OCR 설정 완료"
git push origin feat/refrigerator
```

---

## 💡 트러블슈팅

### 문제 1: "Error loading datafiles" 에러
**원인**: TESSDATA_PREFIX 경로가 잘못됨

**해결**:
```bash
# macOS에서 경로 확인
ls /opt/homebrew/share/tessdata/kor.traineddata

# 환경변수 확인
echo $TESSDATA_PREFIX

# application.yml 확인
grep -A 3 "ocr:" src/main/resources/application.yml
```

### 문제 2: Docker에서 한국어 인식 안 됨
**원인**: tesseract-ocr-kor 미설치

**해결**:
```dockerfile
# Dockerfile에 언어팩 확인
RUN apt-get install -y tesseract-ocr-kor
RUN tesseract --list-langs  # kor 있는지 확인
```

### 문제 3: GitHub Actions에서 테스트 실패
**원인**: Tesseract 설치 단계 누락

**해결**:
```yaml
# backend-ci.yml 확인
- name: Install Tesseract OCR
  run: |
    sudo apt-get update
    sudo apt-get install -y tesseract-ocr tesseract-ocr-kor
```

---

## 📚 참고 문서

- [Tesseract 공식 문서](https://tesseract-ocr.github.io/)
- [Eclipse Temurin 공식 사이트](https://adoptium.net/)
- [Tess4J GitHub](https://github.com/nguyenq/tess4j)
- [Homebrew Tesseract](https://formulae.brew.sh/formula/tesseract)

---

**작성일**: 2025-11-03  
**환경**: macOS (Apple Silicon), Docker Compose, GitHub Actions  
**Tesseract 버전**: 5.5.1  
**JDK**: Eclipse Temurin 17
