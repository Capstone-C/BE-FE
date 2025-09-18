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

### Backend (미정)
| 항목 | 선택 | 비고 |
|------|------|------|
| Language | Java 17 |  |
| Framework | Spring Boot 3.2.0 |  |
| Persistence | Spring Data JPA |  |
| DB (Prod) | MySQL 8.0 |  |
| DB (Test) | H2 |  |
| Build | Gradle | (Maven에서 Gradle로 전환 예정/진행) |
| Validation | Spring Validation |  |

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