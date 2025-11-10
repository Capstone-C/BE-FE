# ⚡ 3단계 CI 전략 가이드

## 🎯 CI 전략 개요

### 왜 3단계 CI가 필요한가?
- **개발 속도 향상**: PR 초기 단계에서 빠른 피드백
- **비용 절감**: GitHub Actions 무료 플랜 효율적 사용
- **개발자 경험**: 긴 빌드 대기 시간 최소화

---

## 🏗️ 3단계 CI 구조

```
┌─────────────────────────────────────────────────────────┐
│                    개발 프로세스                          │
├─────────────────────────────────────────────────────────┤
│                                                           │
│  feat/xxx 브랜치 → develop PR                            │
│       ↓                                                   │
│  🪶 Light CI (30-60초)                                   │
│     - 컴파일만 체크                                       │
│     - 문법 오류 확인                                      │
│       ↓                                                   │
│  develop 브랜치 → main PR                                │
│       ↓                                                   │
│  ⚡ Fast CI (1-1.5분)                                    │
│     - 단위 테스트만 실행                                  │
│     - JAR 빌드 스킵                                       │
│       ↓                                                   │
│  main 브랜치 병합 전                                      │
│       ↓                                                   │
│  🔥 Full CI (2-3분)                                      │
│     - 전체 빌드                                           │
│     - 모든 테스트                                         │
│     - JAR 생성                                            │
│     - 코드 품질 검사                                      │
│                                                           │
└─────────────────────────────────────────────────────────┘
```

---

## 📊 CI 비교표

| CI 유형 | 트리거 | 실행 시간 | 검증 범위 | 사용 시점 |
|---------|--------|----------|----------|----------|
| 🪶 **Light CI** | feat/fix → develop PR | **30-60초** | 컴파일만 | 초기 PR, 빠른 피드백 |
| ⚡ **Fast CI** | develop → main PR | **1-1.5분** | 단위 테스트 | 중간 검증 |
| 🔥 **Full CI** | main 병합/Push | **2-3분** | 전체 빌드 | 최종 배포 전 |

---

## 🪶 Light CI (초고속)

### 📁 파일
`.github/workflows/backend-light-ci.yml`

### 🎯 목적
**최소 시간으로 컴파일 가능 여부만 확인**

### ✅ 실행 내용
```bash
./gradlew compileJava compileTestJava \
  -x test -x bootJar -x jar
```

### 📊 예상 시간
- **컴파일**: 20-40초
- **캐시 히트 시**: 10-20초
- **총 시간**: 30-60초

### 🔍 검증 항목
- ✅ 코드 문법 오류
- ✅ 컴파일 에러
- ✅ 의존성 문제
- ❌ 테스트 (스킵)
- ❌ JAR 빌드 (스킵)

### 💡 사용 시나리오
```
개발자: "간단한 오타 수정했는데 PR 올려야지"
        ↓
Light CI: 30초 만에 컴파일 체크 ✅
        ↓
개발자: "오케이, 다음 작업 진행!"
```

### 📝 로컬 실행
```bash
# Gradle 태스크 사용
./gradlew lightCheck

# 또는 직접 명령
./gradlew compileJava compileTestJava -x test -x bootJar
```

---

## ⚡ Fast CI (빠른 테스트)

### 📁 파일
`.github/workflows/backend-fast-ci.yml`

### 🎯 목적
**핵심 단위 테스트만 빠르게 실행**

### ✅ 실행 내용
```bash
./gradlew test \
  -x bootJar -x jar \
  --tests '*Test' --tests '*Tests'
```

### 📊 예상 시간
- **컴파일**: 20-30초
- **테스트 실행**: 30-60초
- **총 시간**: 1-1.5분

### 🔍 검증 항목
- ✅ 코드 컴파일
- ✅ 단위 테스트
- ✅ 비즈니스 로직 검증
- ❌ 통합 테스트 (스킵)
- ❌ JAR 빌드 (스킵)

### 💡 사용 시나리오
```
개발자: "develop에서 main으로 올리기 전에 체크"
        ↓
Fast CI: 1분 만에 테스트 완료 ✅
        ↓
개발자: "테스트 통과! main으로 PR 생성"
```

### 📝 로컬 실행
```bash
# Gradle 태스크 사용
./gradlew fastTest

# 또는 직접 명령
./gradlew test -x bootJar -x jar
```

---

## 🔥 Full CI (완전 빌드)

### 📁 파일
`.github/workflows/backend-ci.yml`

### 🎯 목적
**배포 전 완벽한 검증**

### ✅ 실행 내용
```bash
./gradlew clean build
```

### 📊 예상 시간
- **Clean 빌드**: 40-70초
- **테스트 실행**: 40-60초
- **JAR 생성**: 10-20초
- **코드 품질 검사**: 20-30초
- **총 시간**: 2-3분

### 🔍 검증 항목
- ✅ 코드 컴파일
- ✅ 모든 단위 테스트
- ✅ 통합 테스트
- ✅ JAR 빌드
- ✅ 코드 품질 검사
- ✅ 배포 가능 여부

### 💡 사용 시나리오
```
개발자: "main 브랜치에 병합 준비 완료"
        ↓
Full CI: 2-3분 만에 전체 검증 ✅
        ↓
자동 배포 시작 🚀
```

### 📝 로컬 실행
```bash
# 전체 빌드
./gradlew clean build

# 빌드 시간 측정
time ./gradlew clean build
```

---

## 🎓 CI 선택 가이드

### 언제 어떤 CI를 사용할까?

#### 🪶 Light CI 사용 시점
- ✅ 간단한 버그 수정
- ✅ 오타/포맷팅 수정
- ✅ 문서 업데이트
- ✅ 코드 리팩토링 (로직 변경 없음)
- ✅ feat/fix → develop PR

**예시**:
```java
// Before
pubilc void processOrder() { ... }  // 오타

// After
public void processOrder() { ... }  // 수정
```
→ Light CI로 30초 만에 확인! ✅

#### ⚡ Fast CI 사용 시점
- ✅ 새로운 기능 추가
- ✅ 비즈니스 로직 변경
- ✅ API 엔드포인트 추가
- ✅ develop → main PR

**예시**:
```java
// 새로운 서비스 메서드 추가
@Service
public class OrderService {
    public Order createOrder(OrderDto dto) {
        // 새로운 로직...
    }
}
```
→ Fast CI로 1분 만에 단위 테스트 확인! ✅

#### 🔥 Full CI 사용 시점
- ✅ main 브랜치 병합 직전
- ✅ 릴리즈 태그 생성 시
- ✅ 프로덕션 배포 전
- ✅ 주요 기능 완성 후

**예시**:
```
Release v1.2.0 준비 완료
```
→ Full CI로 완벽 검증 후 배포! 🚀

---

## 📝 Gradle 커스텀 태스크

### build.gradle에 추가된 3가지 태스크

#### 1. `lightCheck` - 초고속 컴파일 체크
```bash
./gradlew lightCheck
```
**실행 시간**: 10-20초  
**용도**: 컴파일 가능 여부만 빠르게 확인

#### 2. `fastTest` - 빠른 단위 테스트
```bash
./gradlew fastTest
```
**실행 시간**: 30-60초  
**용도**: JAR 빌드 없이 테스트만 실행

#### 3. `compileOnly` - 컴파일만
```bash
./gradlew compileOnly
```
**실행 시간**: 15-25초  
**용도**: 메인/테스트 코드 컴파일만

---

## 🚀 로컬 개발 워크플로우

### 개발 중 빠른 피드백
```bash
# 1. 코드 작성
vim src/main/java/com/capstone/...

# 2. 빠른 컴파일 체크
./gradlew lightCheck  # 10-20초

# 3. 테스트 작성
vim src/test/java/com/capstone/...

# 4. 빠른 테스트
./gradlew fastTest  # 30-60초

# 5. 전체 빌드 (PR 전)
./gradlew build  # 1-2분
```

---

## 💰 비용 절감 효과

### GitHub Actions 무료 플랜 기준

#### Before (단일 Full CI)
```
PR당 평균 실행: 2.5분
월 PR 수: 100개
총 사용 시간: 250분

GitHub 무료 플랜: 2,000분/월
사용률: 12.5%
```

#### After (3단계 CI)
```
Light CI: 50개 × 0.5분 = 25분
Fast CI: 30개 × 1.5분 = 45분
Full CI: 20개 × 2.5분 = 50분
총 사용 시간: 120분

GitHub 무료 플랜: 2,000분/월
사용률: 6%
절감: 130분 (52%)
```

### 개발자 생산성
```
Before: PR당 평균 대기 2.5분
After: PR당 평균 대기 0.8분

시간 절감: 1.7분/PR
월 100개 PR: 170분 절감 = 2.8시간/월
```

---

## 🔍 트러블슈팅

### Q1. Light CI가 실패했어요
**A**: 컴파일 에러입니다. 다음을 확인하세요:
```bash
# 로컬에서 확인
./gradlew compileJava compileTestJava
```

### Q2. Fast CI는 통과했는데 Full CI는 실패해요
**A**: 통합 테스트나 JAR 빌드 단계의 문제입니다:
```bash
# 전체 빌드로 확인
./gradlew clean build
```

### Q3. 모든 CI를 건너뛰고 싶어요
**A**: PR 제목에 `[skip ci]` 추가:
```
[skip ci] docs: README 업데이트
```

### Q4. Fast CI만 실행하고 싶어요
**A**: PR에 `fast-ci` 라벨 추가 또는:
```bash
# 로컬에서
./gradlew fastTest
```

---

## 📊 CI 실행 시간 비교

### 실제 측정 결과 (예상)

| 작업 | Light CI | Fast CI | Full CI |
|------|----------|---------|---------|
| Checkout | 2-5초 | 2-5초 | 2-5초 |
| JDK Setup | 5-10초 | 5-10초 | 8-15초 |
| Gradle 캐시 | 5-10초 | 5-10초 | 10-20초 |
| 컴파일 | 10-20초 | 20-30초 | 40-60초 |
| 테스트 | ⏭️ 스킵 | 30-60초 | 40-60초 |
| JAR 빌드 | ⏭️ 스킵 | ⏭️ 스킵 | 10-20초 |
| 코드 품질 | ⏭️ 스킵 | ⏭️ 스킵 | 20-30초 |
| **총 시간** | **30-60초** | **1-1.5분** | **2-3분** |

---

## 🎉 결론

### 3단계 CI 전략의 이점

1. **⚡ 속도**: 평균 대기 시간 70% 감소
2. **💰 비용**: GitHub Actions 사용량 50% 절감
3. **👨‍💻 생산성**: 빠른 피드백으로 개발 흐름 유지
4. **🎯 효율성**: 필요한 만큼만 검증

### 추천 사용법

```
일상적인 PR → 🪶 Light CI (30-60초)
        ↓
기능 완성 후 → ⚡ Fast CI (1-1.5분)
        ↓
배포 직전 → 🔥 Full CI (2-3분)
```

**Happy Fast Coding! 🚀**

---

**작성일**: 2025-11-10  
**버전**: v1.0  
**작성자**: GitHub Copilot + Minwoo
