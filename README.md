# Capstone Project

공동 캡스톤 프로젝트로 식단 기록, 공유, 추천 및 커뮤니티 기능을 통합 제공하는 플랫폼입니다. (이 문서는 BE + FE 전체 관점 README이며, 기술 스택이 확정되지 않은 부분은 이후 채워 넣을 예정입니다.)

---
## 1. 프로젝트 개요 (Overview)
사용자는 식단을 기록(다이어리), 식재료/냉장고 상태를 관리하고, 이에 맞는 레시피 및 쇼핑 정보를 탐색·추천받으며 커뮤니티에서 소통할 수 있습니다. 추가로 Q&A 전문가 시스템, 게시판 관리자 선거, 알림/검색 등 확장형 기능을 포함합니다.

---
## 2. 아키텍처 (Architecture)


---
## 3. 기술 스택 (Technology Stack)
### Frontend (미정)

### Backend
| 항목 | 선택 | 비고 |
|------|------|------|
| Language | Java 17 |  |
| Framework | Spring Boot 3.5.6 | build.gradle 기준 |
| Persistence | Spring Data JPA |  |
| DB (Prod) | MySQL 8.0 | Docker compose 사용 |
| DB (Test) | H2 (Memory) | test scope |
| Build | Gradle |  |
| Validation | Spring Validation | javax -> jakarta 전환 완료 |
| Actuator | spring-boot-starter-actuator | health/info 노출 |

#### 주요 의존성 (build.gradle)
- web, security, data-jpa, validation, actuator
- mysql-connector-j (runtime), h2 (test), lombok

---
## 3-1. 로컬 개발 환경 (Docker 사용)
프론트엔드는 아직 컨테이너화하지 않고, 백엔드(Spring Boot)와 MySQL을 Docker Compose로 동시에 실행합니다. Java / Gradle 설치 없이 Docker만 있어도 서버를 띄울 수 있습니다.

### 사전 준비물
1. Docker Desktop 설치 (Mac / Windows)
2. (선택) `docker --version`, `docker compose version`으로 정상 설치 확인

### 디렉토리 구조 요약
```
project_root/
	CC_BE/
		compose.yaml  <- Docker Compose 정의 (mysql + backend)
		build.gradle
		src/
```

### 환경 변수 / 기본값
| 항목 | 값 (기본) | 비고 |
|------|-----------|------|
| MYSQL_DATABASE | ccdb | compose 환경설정 |
| MYSQL_USER | ccuser |  |
| MYSQL_PASSWORD | devpass | 일반 계정 비밀번호 |
| MYSQL_ROOT_PASSWORD | rootpass | root 계정 비밀번호 |
| SPRING_PROFILES_ACTIVE | local | backend 서비스에서 지정 |

### 실행 절차 (처음부터 끝까지)
아래 명령은 그대로 복사/붙여넣기 하면 됩니다.

```bash
# 1. 저장소 클론
git clone <YOUR_REPO_URL> project_capstone
cd project_capstone/CC_BE

# 2. (선택) MySQL만 먼저 올리기 (백엔드 빌드 이전에 DB 준비)
docker compose up -d mysql

# 3. MySQL 상태 확인 (healthy 될 때까지 대기)
docker compose ps
# 또는 로그 추적
docker compose logs -f mysql

# 4. 백엔드 + (이미 없다면) MySQL 함께 실행
docker compose up backend
# 또는 두 서비스 모두 데몬 모드 실행
# docker compose up -d

# 5. 애플리케이션 기동 확인 (Started ... in X seconds 문구)
docker compose logs -f backend

# 6. 헬스 체크 (actuator 사용)
curl http://localhost:8080/actuator/health

# 7. 중지
docker compose down

# 8. 데이터 유지 (mysql_data 볼륨). 완전 초기화 하려면:
docker compose down -v
```

### 자주 발생하는 이슈 & 해결
| 이슈 | 원인 | 해결 |
|------|------|------|
| Cannot connect to MySQL | MySQL 초기화 지연 | healthcheck 후 backend 기동 또는 재시도 |
| Port already allocated 3306 | 로컬에 기존 MySQL 실행 중 | 로컬 MySQL 중지하거나 compose.yaml 포트 변경 |
| 권한 오류 (Access denied) | 계정/비밀번호 불일치 | compose.yaml 환경변수와 `application.yml` 기본값 확인 |

### 개발 생산성 팁
- 코드 수정 시 backend 컨테이너는 Gradle `bootRun`을 실행 중이므로 변경 사항이 재시작되며 반영(Hot reload 수준) 되지만 큰 변경 후에는 컨테이너 재시작 권장.
- 의존성 추가 후에는 `docker compose up --build` 대신 현재 구성에서는 Gradle 이미지를 재사용하므로 컨테이너를 내려(`down`) 다시 올리면 됨.


---
## 4. 협업 (Collaboration)
### 역할 (people.md)
| 구분 | 인원 |
|------|------|
| FE | 김재훈, 나희성 |
| BE | 김준한, 김민우, 윤정환 |

### 브랜치 전략 (초안)
| 종류 | 네이밍 | 목적 |
|------|--------|------|
| Main | main | 안정 배포 |
| Develop | develop (선택) | 통합 테스트 |
| Feature | feat/<scope> | 기능 개발 |
| Fix | fix/<scope> | 버그 수정 |
| Docs | docs/<scope> | 문서 |

### 커밋 컨벤션 (제안)
`feat:`, `fix:`, `docs:`, `refactor:`, `test:`, `chore:`

---