# ⚡ 3단계 CI 빠른 참조

## 🎯 한눈에 보기

```
🪶 Light CI   → 30-60초  → 컴파일만
⚡ Fast CI    → 1-1.5분  → 단위 테스트
🔥 Full CI    → 2-3분    → 전체 빌드
```

---

## 🚀 로컬 명령어

```bash
# 🪶 초고속 컴파일 체크
./gradlew lightCheck

# ⚡ 빠른 단위 테스트
./gradlew fastTest

# 🔍 컴파일만
./gradlew compileOnly

# 🔥 전체 빌드
./gradlew clean build
```

---

## 📊 CI 선택 가이드

| 상황 | 추천 CI | 시간 |
|------|---------|------|
| 오타/포맷팅 수정 | 🪶 Light | 30-60초 |
| 간단한 버그 수정 | 🪶 Light | 30-60초 |
| 새로운 기능 추가 | ⚡ Fast | 1-1.5분 |
| API 엔드포인트 추가 | ⚡ Fast | 1-1.5분 |
| main 병합 전 | 🔥 Full | 2-3분 |
| 배포 직전 | 🔥 Full | 2-3분 |

---

## 🔧 트러블슈팅

### Light CI 실패
```bash
# 로컬 확인
./gradlew compileJava
```

### Fast CI 실패
```bash
# 로컬 테스트
./gradlew test
```

### Full CI 실패
```bash
# 전체 빌드
./gradlew clean build
```

---

## 💡 팁

### CI 건너뛰기
```
[skip ci] docs: README 업데이트
```

### 빌드 시간 측정
```bash
time ./gradlew clean build
```

### 캐시 초기화
```bash
./gradlew clean --build-cache
rm -rf ~/.gradle/caches
```

---

## 📁 파일 위치

- 🪶 Light: `.github/workflows/backend-light-ci.yml`
- ⚡ Fast: `.github/workflows/backend-fast-ci.yml`
- 🔥 Full: `.github/workflows/backend-ci.yml`
- 📝 가이드: `CC_BE/CI_STRATEGY_GUIDE.md`

---

## 🎉 핵심 요약

**52% GitHub Actions 비용 절감**  
**70% 평균 대기 시간 감소**  
**2.8시간/월 개발 시간 절약**

**Happy Fast Coding! 🚀**
