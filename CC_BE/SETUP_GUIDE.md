# OCR 개선사항 적용 가이드

## 📋 변경 사항 요약

### 1. Gradle 테스트 설정 (`build.gradle`)
- ✅ OpenCV 네이티브 라이브러리 자동 추출 및 경로 설정
- ✅ Tesseract 데이터 경로 환경변수 설정
- ✅ 테스트 로그 상세 출력 설정

### 2. Docker 배포 설정
- ✅ `Dockerfile`: Tesseract OCR + 한글 언어팩 설치 추가
- ✅ `compose.yaml`: TESSDATA_PREFIX 환경변수 추가, 업로드 볼륨 마운트

### 3. 커밋 메시지 문서 (`COMMIT_MESSAGES.md`)
- ✅ 3개 개선사항에 대한 상세한 커밋 메시지 작성
- ✅ 각 개선사항의 배경, 구현 내용, 효과 문서화

---

## 🚀 로컬 개발 환경 설정

### 1단계: Tesseract 설치

#### macOS (Homebrew 필요)
```bash
# Homebrew가 설치되지 않은 경우
/bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)"

# Tesseract 설치
brew install tesseract tesseract-lang

# 설치 확인
tesseract --version
# Tesseract Open Source OCR Engine v5.x.x

# 한글 언어 데이터 확인
ls /opt/homebrew/share/tessdata/kor.*
# /opt/homebrew/share/tessdata/kor.traineddata
```

#### macOS (Homebrew 없이 수동 설치)
```bash
# 1. Tesseract 바이너리 다운로드
# https://github.com/UB-Mannheim/tesseract/wiki 에서 macOS 버전 다운로드

# 2. 한글 언어 데이터 다운로드
mkdir -p ~/tessdata
cd ~/tessdata
curl -LO https://github.com/tesseract-ocr/tessdata/raw/main/kor.traineddata
curl -LO https://github.com/tesseract-ocr/tessdata/raw/main/eng.traineddata

# 3. 환경변수 설정 (~/.zshrc에 추가)
echo 'export TESSDATA_PREFIX=~/tessdata' >> ~/.zshrc
source ~/.zshrc
```

#### Linux (Ubuntu/Debian)
```bash
sudo apt-get update
sudo apt-get install -y tesseract-ocr tesseract-ocr-kor

# 설치 확인
tesseract --version
ls /usr/share/tesseract-ocr/5.00/tessdata/kor.*
```

### 2단계: Gradle 테스트 설정 수정 (이미 완료됨)

`build.gradle`의 `test` 블록에서 TESSDATA_PREFIX를 실제 경로로 수정:

```gradle
test {
    // ... (자동 추출 로직은 그대로)
    
    // ⚠️ 이 부분을 실제 tessdata 경로로 수정하세요
    environment 'TESSDATA_PREFIX', '/opt/homebrew/share/tessdata'  // macOS Homebrew
    // environment 'TESSDATA_PREFIX', '~/tessdata'  // 수동 설치
    // environment 'TESSDATA_PREFIX', '/usr/share/tesseract-ocr/5.00/tessdata'  // Linux
}
```

### 3단계: 테스트 이미지 파일 확인

테스트 이미지가 올바른 위치에 있는지 확인:

```bash
ls -la src/test/resources/test-image/
# image.png
# image2.png
# image3.png
# image4.png
# image5.png
```

만약 없다면:
```bash
mkdir -p src/test/resources/test-image
# 영수증 이미지를 이 폴더에 복사
```

### 4단계: 테스트 실행

```bash
# 전체 테스트 실행
./gradlew test --no-daemon

# 특정 테스트만 실행
./gradlew test --tests ReceiptParserServiceTest --no-daemon

# 테스트 결과 HTML 리포트 확인
open build/reports/tests/test/index.html
```

#### 예상 결과
```
✅ 기본 파싱 테스트 (5/5): 정규식 패턴 검증
✅ 실제 영수증 이미지 테스트 (5/5): OCR + 파싱 통합 검증

BUILD SUCCESSFUL in 15s
11 tests completed
```

---

## 🐳 Docker 환경 설정

### 1단계: Docker 이미지 빌드

```bash
# 프로젝트 루트에서 실행
cd /Users/pilt/project-collection/capstone

# Docker Compose로 빌드 (Tesseract 자동 설치)
docker-compose build backend

# 빌드 확인
docker images | grep cc_backend
```

### 2단계: 컨테이너 실행

```bash
# 전체 스택 실행 (MySQL + Backend + Frontend)
docker-compose up -d

# Backend 로그 확인
docker-compose logs -f backend

# Tesseract 설치 확인
docker-compose exec backend tesseract --version
docker-compose exec backend ls /usr/share/tesseract-ocr/5/tessdata/kor.traineddata
```

### 3단계: 개발 모드로 실행 (테스트 데이터 포함)

#### docker-compose.yaml 수정
```yaml
services:
  backend:
    environment:
      SPRING_PROFILES_ACTIVE: dev  # local → dev로 변경
      # ... 기존 환경변수 유지
```

#### 실행
```bash
docker-compose down
docker-compose up -d

# 로그에서 테스트 데이터 생성 확인
docker-compose logs backend | grep "개발 환경 테스트 데이터"
```

---

## 📝 Git 커밋 가이드

### 커밋 1: 정규식 개선
```bash
cd /Users/pilt/project-collection/capstone/CC_BE

git add src/main/java/com/capstone/web/ocr/service/ReceiptParserService.java

git commit -m "feat(ocr): 영수증 파싱 정규식 패턴 개선" \
           -m "" \
           -m "## 변경 목적" \
           -m "한국 영수증의 다양한 형식(₩ 기호, 소수점 수량, 다양한 단위)을 더 정확하게 파싱" \
           -m "" \
           -m "## 주요 변경 사항" \
           -m "- PRICE_PATTERN: ₩ 기호 및 유연한 구분자(쉼표/마침표) 지원" \
           -m "- QUANTITY_PATTERN: 소수점 + 20개 이상 한글 단위 지원" \
           -m "- IGNORE_KEYWORDS: 17개 → 33개 이상 확장 (영어 포함)" \
           -m "- normalizeUnit(): 단위 정규화 메서드 추가" \
           -m "" \
           -m "## 예상 효과" \
           -m "파싱 성공률 15-20% 향상, 데이터 일관성 개선" \
           -m "" \
           -m "상세 내용: CC_BE/COMMIT_MESSAGES.md 참고"
```

### 커밋 2: 테스트 데이터 생성기
```bash
git add src/main/java/com/capstone/web/common/DevDataInitializer.java
git add DEV_DATA_INFO.md

git commit -m "feat(dev): 개발 환경 테스트 데이터 자동 생성기 추가" \
           -m "" \
           -m "## 배경" \
           -m "프론트엔드 개발 시 매번 수동으로 테스트 데이터 입력하는 불편함 해소" \
           -m "" \
           -m "## 구현" \
           -m "- @Profile('dev') 적용으로 개발 환경에서만 실행" \
           -m "- CommandLineRunner로 앱 시작 시 자동 실행" \
           -m "- 중복 생성 방지 로직 포함" \
           -m "" \
           -m "## 생성 데이터" \
           -m "- 테스트 회원 3명 (test1@test.com, test2@test.com, admin@test.com)" \
           -m "- 카테고리 9개 (계층 구조 포함)" \
           -m "- 냉장고 아이템 10개 (다양한 유통기한)" \
           -m "- 다이어리 7개 (최근 3일간 식단 기록)" \
           -m "" \
           -m "## 사용 방법" \
           -m "./gradlew bootRun --args='--spring.profiles.active=dev'" \
           -m "" \
           -m "상세 내용: CC_BE/COMMIT_MESSAGES.md, DEV_DATA_INFO.md 참고"
```

### 커밋 3: OpenCV 통합
```bash
git add build.gradle
git add src/main/java/com/capstone/web/ocr/service/ImagePreprocessorService.java
git add src/main/java/com/capstone/web/ocr/service/TesseractOcrService.java
git add src/test/java/com/capstone/web/ocr/service/ReceiptParserServiceTest.java
git add Dockerfile
git add ../compose.yaml
git add OPENCV_INTEGRATION.md
git add IMPROVEMENTS_SUMMARY.md
git add COMMIT_MESSAGES.md
git add SETUP_GUIDE.md

git commit -m "feat(ocr): OpenCV 이미지 전처리 통합으로 OCR 정확도 개선" \
           -m "" \
           -m "## 목적" \
           -m "저품질 영수증 이미지 인식률 향상 (예상 30-40% 개선)" \
           -m "" \
           -m "## 구현" \
           -m "- ImagePreprocessorService: 7단계 전처리 파이프라인" \
           -m "  1. 그레이스케일 변환" \
           -m "  2. 가우시안 블러 노이즈 제거" \
           -m "  3. 적응형 이진화" \
           -m "  4. 형태학적 연산" \
           -m "  5. 300 DPI 리사이즈" \
           -m "- TesseractOcrService: 전처리 통합 + 폴백 메커니즘" \
           -m "- ReceiptParserServiceTest: 실제 영수증 이미지 테스트" \
           -m "" \
           -m "## 배포 설정" \
           -m "- build.gradle: OpenCV 네이티브 라이브러리 경로 자동 설정" \
           -m "- Dockerfile: Tesseract + 한글 언어팩 설치" \
           -m "- compose.yaml: TESSDATA_PREFIX 환경변수 추가" \
           -m "" \
           -m "## 문서" \
           -m "- OPENCV_INTEGRATION.md: 기술 상세 문서" \
           -m "- IMPROVEMENTS_SUMMARY.md: 전체 개선사항 요약" \
           -m "- COMMIT_MESSAGES.md: 상세 커밋 메시지" \
           -m "- SETUP_GUIDE.md: 설치 및 설정 가이드" \
           -m "" \
           -m "상세 내용: CC_BE/COMMIT_MESSAGES.md 참고"
```

---

## ✅ 체크리스트

### 개발 환경 설정
- [ ] Tesseract 설치 완료 (`tesseract --version` 확인)
- [ ] 한글 언어 데이터 확인 (`ls $TESSDATA_PREFIX/kor.traineddata`)
- [ ] build.gradle의 TESSDATA_PREFIX 경로 수정
- [ ] 테스트 이미지 파일 존재 확인 (`src/test/resources/test-image/*.png`)
- [ ] 테스트 실행 성공 (`./gradlew test`)

### Docker 환경 설정
- [ ] Docker 이미지 빌드 성공 (`docker-compose build backend`)
- [ ] 컨테이너 실행 성공 (`docker-compose up -d`)
- [ ] Tesseract 설치 확인 (`docker-compose exec backend tesseract --version`)
- [ ] 한글 언어 데이터 확인 (컨테이너 내부)

### Git 커밋
- [ ] 커밋 1: 정규식 개선
- [ ] 커밋 2: 테스트 데이터 생성기
- [ ] 커밋 3: OpenCV 통합 (설정 파일 포함)

### 문서 확인
- [ ] OPENCV_INTEGRATION.md: OpenCV 기술 문서
- [ ] IMPROVEMENTS_SUMMARY.md: 개선사항 요약
- [ ] COMMIT_MESSAGES.md: 상세 커밋 메시지
- [ ] DEV_DATA_INFO.md: 테스트 데이터 정보
- [ ] SETUP_GUIDE.md: 설치 가이드 (본 파일)

---

## 🔧 트러블슈팅

### 문제 1: `UnsatisfiedLinkError: no opencv_java470`
**원인**: OpenCV 네이티브 라이브러리를 찾을 수 없음

**해결**:
```bash
# Gradle 캐시 정리 후 재빌드
./gradlew clean
./gradlew build --refresh-dependencies

# 테스트 재실행
./gradlew test --rerun-tasks
```

### 문제 2: `TesseractException: Tesseract not installed`
**원인**: Tesseract가 설치되지 않았거나 경로를 찾을 수 없음

**해결**:
```bash
# macOS
which tesseract
# /opt/homebrew/bin/tesseract

# 환경변수 확인
echo $TESSDATA_PREFIX
# /opt/homebrew/share/tessdata

# build.gradle의 environment 설정 확인
```

### 문제 3: 테스트 이미지 파일을 찾을 수 없음
**원인**: `src/test/resources/test-image/` 경로에 이미지 없음

**해결**:
```bash
# 디렉토리 생성
mkdir -p src/test/resources/test-image

# 이미지 파일 복사
cp /path/to/receipt-images/*.png src/test/resources/test-image/

# 확인
ls src/test/resources/test-image/
```

### 문제 4: Docker 컨테이너에서 Tesseract 실행 안 됨
**원인**: Dockerfile 빌드 중 Tesseract 설치 실패

**해결**:
```bash
# 이미지 재빌드 (캐시 무시)
docker-compose build --no-cache backend

# 컨테이너 내부 확인
docker-compose exec backend bash
tesseract --version
ls /usr/share/tesseract-ocr/5/tessdata/
```

---

## 📚 참고 자료

### Tesseract OCR
- 공식 GitHub: https://github.com/tesseract-ocr/tesseract
- 언어 데이터: https://github.com/tesseract-ocr/tessdata
- Tess4J (Java 바인딩): https://github.com/nguyenq/tess4j

### OpenCV
- 공식 문서: https://docs.opencv.org/4.x/
- Java 바인딩: https://github.com/openpnp/opencv

### Spring Boot
- Profile 설정: https://docs.spring.io/spring-boot/reference/features/profiles.html
- Docker Compose 지원: https://docs.spring.io/spring-boot/reference/features/docker-compose.html

---

## 📞 문의

문제가 발생하거나 추가 설명이 필요한 경우:
1. `build/reports/tests/test/index.html` 테스트 리포트 확인
2. `./gradlew test --info` 상세 로그 확인
3. Docker 로그: `docker-compose logs backend`
