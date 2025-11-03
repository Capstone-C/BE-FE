# GitHub Actions CI/CD 설정 가이드

## 📁 생성된 Workflow 파일

```
.github/workflows/
├── backend-ci.yml          # 백엔드 빌드 & 테스트
├── frontend-ci.yml         # 프론트엔드 빌드 & 테스트
└── integration-test.yml    # 전체 스택 통합 테스트 (선택)
```

---

## 🔄 1. Backend CI Workflow

### 실행 조건
- ✅ PR 생성/업데이트 (main, develop 브랜치로)
- ✅ main, develop 브랜치에 push
- ✅ `CC_BE/` 폴더 변경 시에만

### 주요 기능
1. **MySQL Service Container 자동 실행**
   - 이미지: `mysql:8.0`
   - 데이터베이스: `ccdb_test`
   - 포트: `3306`
   - Health check로 준비 완료 대기

2. **JDK 17 설정 및 캐싱**
   - Gradle 의존성 캐싱으로 빌드 속도 향상

3. **테스트 실행**
   - `./gradlew clean build` 실행
   - MySQL 컨테이너와 자동 연결

4. **테스트 결과 아티팩트 업로드**
   - 테스트 리포트: `build/reports/tests/test/`
   - 실패 시에도 업로드되어 디버깅 가능

### MySQL 연결 정보
```yaml
URL: jdbc:mysql://localhost:3306/ccdb_test
Username: ccuser
Password: testpass
```

### 현재 설정 확인 필요
`CC_BE/src/test/resources/application-test.yml` 파일을 확인하세요:

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/ccdb_test
    username: ccuser
    password: testpass
```

---

## 🎨 2. Frontend CI Workflow

### 실행 조건
- ✅ PR 생성/업데이트 (main, develop 브랜치로)
- ✅ main, develop 브랜치에 push
- ✅ `CC_FE/` 폴더 변경 시에만

### 주요 기능
1. **Node.js 20 + pnpm 설정**
   - pnpm 캐싱으로 설치 속도 향상

2. **코드 품질 검사**
   - ESLint 실행
   - TypeScript 타입 체크 (있는 경우)

3. **빌드 테스트**
   - `pnpm run build` 실행
   - 빌드 가능 여부 검증

4. **테스트 실행** (설정되어 있는 경우)
   - Vitest 또는 Jest 실행

---

## 🔗 3. Integration Test Workflow (선택사항)

### 실행 조건
- ✅ main 브랜치로 PR
- ✅ main 브랜치에 push

### 주요 기능
1. **Docker Compose로 전체 스택 실행**
   - MySQL + Backend + Frontend

2. **서비스 Health Check**
   - 백엔드 `/actuator/health` 확인
   - 프론트엔드 접근 확인

3. **실패 시 로그 출력**
   - 디버깅을 위한 컨테이너 로그

---

## 🚀 사용 방법

### 1단계: Workflow 파일 커밋
```bash
git add .github/workflows/
git commit -m "ci: GitHub Actions workflow 추가

- Backend CI: MySQL Service Container로 테스트
- Frontend CI: pnpm으로 빌드 및 린트 검사
- Integration Test: Docker Compose로 전체 스택 테스트"
git push origin feat/refrigerator
```

### 2단계: PR 생성
1. GitHub에서 PR 생성
2. Actions 탭에서 자동 실행 확인
3. 체크 결과 대기

### 3단계: 결과 확인
- ✅ 모든 체크 통과 시 → Merge 가능
- ❌ 실패 시 → 아티팩트 다운로드하여 테스트 리포트 확인

---

## 🔧 트러블슈팅

### 문제 1: MySQL 연결 실패
**증상**: `Communications link failure`

**해결**:
`CC_BE/src/test/resources/application-test.yml` 파일이 올바른지 확인:
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/ccdb_test
    username: ccuser
    password: testpass
```

### 문제 2: Tesseract 관련 에러
**증상**: `Unable to load library 'tesseract'`

**해결**:
GitHub Actions에는 Tesseract가 설치되어 있지 않습니다.
`backend-ci.yml`에 설치 단계 추가:

```yaml
- name: Install Tesseract
  run: |
    sudo apt-get update
    sudo apt-get install -y tesseract-ocr tesseract-ocr-kor
```

### 문제 3: OpenCV 네이티브 라이브러리 에러
**증상**: `UnsatisfiedLinkError: opencv_java`

**해결**:
현재 `build.gradle`의 테스트 설정이 로컬에서만 동작합니다.
CI 환경에서는 OpenCV가 자동으로 추출됩니다. (이미 설정됨)

### 문제 4: 프론트엔드 스크립트 없음
**증상**: `test script not found`

**해결**:
`CC_FE/package.json`에 스크립트 추가:
```json
{
  "scripts": {
    "test": "vitest",
    "type-check": "tsc --noEmit"
  }
}
```

---

## 📊 예상 실행 시간

| Workflow | 예상 시간 |
|----------|----------|
| Backend CI | 3-5분 |
| Frontend CI | 1-2분 |
| Integration Test | 5-7분 |

---

## ✨ 추가 개선 사항 (선택)

### 1. 캐시 최적화
이미 적용됨:
- ✅ Gradle 의존성 캐싱
- ✅ pnpm 의존성 캐싱

### 2. 병렬 실행
Backend와 Frontend CI는 독립적으로 병렬 실행됩니다.

### 3. 브랜치 보호 규칙 설정
GitHub 설정 → Branches → Branch protection rules:
1. **Require status checks to pass**
   - ✅ Backend CI / test
   - ✅ Frontend CI / test

2. **Require branches to be up to date**
   - ✅ 체크

---

## 📚 참고 자료

### GitHub Actions
- [Service Containers](https://docs.github.com/en/actions/using-containerized-services)
- [Using databases in workflows](https://docs.github.com/en/actions/using-containerized-services/creating-postgresql-service-containers)

### Docker
- [MySQL Docker Hub](https://hub.docker.com/_/mysql)
- [Docker Compose in CI](https://docs.docker.com/compose/ci-cd/)

---

## 🎯 다음 단계

1. ✅ Workflow 파일 커밋 및 푸시
2. ⏳ PR 생성하여 Actions 실행 확인
3. ⏳ 필요한 경우 Tesseract 설치 단계 추가
4. ⏳ 브랜치 보호 규칙 설정
5. ⏳ 팀원들에게 공유

---

## 💡 팁

### PR 체크 통과 전 로컬 테스트
```bash
# Backend 테스트 (MySQL 컨테이너 필요)
docker-compose up -d mysql
cd CC_BE && ./gradlew test

# Frontend 테스트
cd CC_FE && pnpm install && pnpm run build
```

### Actions 로그 확인
1. GitHub → Actions 탭
2. 실패한 workflow 클릭
3. 각 step의 로그 확인
4. Artifacts에서 테스트 리포트 다운로드

### 비용 절감
- GitHub Free: 월 2,000분 무료
- Public 레포지토리: 무제한 무료
- Private 레포지토리: 분 단위 과금

현재 설정으로 PR당 약 5-7분 소요 → 월 200-300회 PR 가능
