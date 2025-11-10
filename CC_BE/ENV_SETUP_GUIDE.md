# 🔐 환경변수 설정 가이드

## 📋 목차
1. [GitHub Actions 설정](#github-actions-설정)
2. [로컬 개발 환경 설정](#로컬-개발-환경-설정)
3. [IntelliJ IDEA 설정](#intellij-idea-설정)
4. [새로운 JWT Secret](#새로운-jwt-secret)

---

## 🚀 GitHub Actions 설정

### 1. GitHub Secrets 등록

1. **GitHub 리포지토리로 이동**
   - https://github.com/qoweh/BE-FE

2. **Settings → Secrets and variables → Actions**

3. **다음 Secrets 추가** (New repository secret 클릭)

| Secret Name | Value |
|------------|-------|
| `JWT_SECRET` | `SH0aUd2UroPzeD7b/pPLYukQ82UBkcayF/IefsVWkczJG6LWiCOXBUAZW+kARsoD` |
| `GEMINI_API_KEY` | 실제 Gemini API 키 |

### 2. 확인

- `.github/workflows/backend-ci.yml`에 이미 설정됨:
```yaml
env:
  JWT_SECRET: ${{ secrets.JWT_SECRET }}
  GEMINI_API_KEY: ${{ secrets.GEMINI_API_KEY }}
```

---

## 💻 로컬 개발 환경 설정

### 방법 1: .env 파일 사용 (추천) ⭐

```bash
# 1. 예제 파일을 복사
cd CC_BE
cp .env.example .env

# 2. .env 파일 수정 (실제 값 입력)
vim .env  # 또는 VSCode에서 열기
```

**`.env` 파일 내용:**
```bash
JWT_SECRET=SH0aUd2UroPzeD7b/pPLYukQ82UBkcayF/IefsVWkczJG6LWiCOXBUAZW+kARsoD
GEMINI_API_KEY=your-actual-gemini-api-key
MYSQL_HOST=localhost
MYSQL_PORT=3306
MYSQL_DATABASE=ccdb
MYSQL_USER=ccuser
MYSQL_PASSWORD=devpass
```

> ⚠️ **주의**: `.env` 파일은 절대 Git에 커밋하지 마세요! (이미 `.gitignore`에 포함됨)

### 방법 2: application-local.yml 사용

```bash
# Spring Profile을 'local'로 설정하고 실행
./gradlew bootRun --args='--spring.profiles.active=local'
```

---

## 🛠 IntelliJ IDEA 설정

### 1. Run Configuration 설정

1. **Run → Edit Configurations...**
2. **Environment variables** 추가:
   ```
   JWT_SECRET=SH0aUd2UroPzeD7b/pPLYukQ82UBkcayF/IefsVWkczJG6LWiCOXBUAZW+kARsoD;GEMINI_API_KEY=your-key
   ```

### 2. Active Profile 설정

**Option 1: VM Options**
```
-Dspring.profiles.active=local
```

**Option 2: Program Arguments**
```
--spring.profiles.active=local
```

### 3. EnvFile 플러그인 사용 (선택)

1. **Preferences → Plugins → "EnvFile" 검색 및 설치**
2. **Run Configuration → EnvFile 탭 → `.env` 파일 추가**

---

## 🔑 새로운 JWT Secret

### 🎉 Production용 JWT Secret

```
SH0aUd2UroPzeD7b/pPLYukQ82UBkcayF/IefsVWkczJG6LWiCOXBUAZW+kARsoD
```

### 특징
- ✅ **64자 길이** (Base64 인코딩)
- ✅ **OpenSSL로 생성** (암호학적으로 안전)
- ✅ **최소 32바이트 이상** (256비트)
- ✅ **특수문자 포함** (강력한 엔트로피)

### GitHub Secrets에 등록 필수! 🔒

1. GitHub → Settings → Secrets and variables → Actions
2. Name: `JWT_SECRET`
3. Value: `SH0aUd2UroPzeD7b/pPLYukQ82UBkcayF/IefsVWkczJG6LWiCOXBUAZW+kARsoD`
4. Add secret 클릭

---

## 🧪 테스트

### 로컬 환경 테스트
```bash
cd CC_BE
./gradlew bootRun --args='--spring.profiles.active=local'
```

### CI 환경 확인
- GitHub Actions가 실행될 때 자동으로 Secrets 사용
- JWT_SECRET과 GEMINI_API_KEY가 환경변수로 주입됨

---

## ⚠️ 보안 주의사항

1. ❌ **절대 커밋하지 말 것:**
   - `.env` 파일
   - `application-local.yml` (민감한 값 포함 시)
   - 실제 API 키나 Secret

2. ✅ **반드시 확인:**
   - `.env`가 `.gitignore`에 있는지
   - GitHub Secrets에 프로덕션 키가 등록되었는지
   - 로컬 개발용과 프로덕션 키를 분리했는지

3. 🔄 **주기적으로:**
   - JWT Secret 변경 (보안 사고 발생 시)
   - API 키 갱신 (만료일 확인)

---

## 📞 문제 해결

### Q: 로컬에서 실행 시 JWT_SECRET을 찾을 수 없다고 나와요
```bash
# 해결: .env 파일을 확인하거나 직접 환경변수 설정
export JWT_SECRET=SH0aUd2UroPzeD7b/pPLYukQ82UBkcayF/IefsVWkczJG6LWiCOXBUAZW+kARsoD
./gradlew bootRun
```

### Q: GitHub Actions에서 JWT_SECRET 에러가 발생해요
```
# 해결: GitHub Secrets에 JWT_SECRET이 등록되었는지 확인
Settings → Secrets and variables → Actions → JWT_SECRET 확인
```

### Q: .env 파일이 Git에 추가되었어요
```bash
# 해결: 즉시 제거하고 .gitignore 확인
git rm --cached CC_BE/.env
git commit -m "security: remove .env file from git"
```

---

**Happy Coding! 🚀**
