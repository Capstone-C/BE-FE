# 백엔드 테스트 가이드

## 개요

이 프로젝트는 **H2 인메모리 데이터베이스**를 사용하여 빠르고 독립적인 단위/통합 테스트를 수행합니다.

## 테스트 환경

### 데이터베이스

- **Production**: MySQL 8.0 (Docker Compose)
- **Test**: H2 Database (In-memory, MySQL 호환 모드)

### H2를 선택한 이유

1. **빠른 실행 속도**: 메모리 기반으로 동작하여 테스트 속도가 매우 빠름
2. **독립성**: 외부 DB 서비스에 의존하지 않아 CI/CD 환경에서 안정적
3. **초기화 자동화**: 각 테스트마다 깨끗한 상태로 시작 (`ddl-auto: create-drop`)
4. **MySQL 호환**: `MODE=MySQL` 설정으로 production 환경과 유사한 SQL 동작

### 테스트 설정 파일

테스트 환경 설정은 `src/test/resources/application-test.yml`에 정의되어 있습니다.

```yaml
spring:
  datasource:
    url: jdbc:h2:mem:testdb;MODE=MySQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE
    username: sa
    password:
    driver-class-name: org.h2.Driver

  jpa:
    hibernate:
      ddl-auto: create-drop
    properties:
      hibernate:
        dialect: org.hibernate.dialect.H2Dialect
        format_sql: true
```

## 로컬에서 테스트 실행

### 1. 전체 테스트 실행

```bash
cd CC_BE
./gradlew clean test
```

### 2. 특정 테스트 클래스만 실행

```bash
./gradlew test --tests "com.capstone.web.refrigerator.service.RefrigeratorServiceTest"
```

### 3. 특정 테스트 메서드만 실행

```bash
./gradlew test --tests "com.capstone.web.refrigerator.service.RefrigeratorServiceTest.식재료 추가 - 성공"
```

### 4. 상세 로그와 함께 실행

```bash
./gradlew test --info
```

## GitHub Actions에서 테스트

GitHub Actions는 Ubuntu 환경에서 자동으로 테스트를 실행합니다.

### 워크플로우 파일

`.github/workflows/backend-ci.yml`

### 실행 조건

- Pull Request가 `main` 또는 `develop` 브랜치로 생성될 때
- `CC_BE/**` 또는 워크플로우 파일이 변경될 때

### 실행 단계

1. ✅ 코드 체크아웃
2. ✅ JDK 17 설치
3. ✅ Tesseract OCR 설치 (OCR 테스트용)
4. ✅ Gradle 빌드 및 테스트 실행 (H2 DB 사용)
5. ✅ 테스트 결과 업로드

### 주요 특징

- **빠른 실행**: MySQL 컨테이너 없이 H2만 사용하여 빠르게 테스트
- **안정성**: 외부 서비스 의존성 없음
- **자동화**: 모든 테스트가 자동으로 실행되고 결과 확인 가능

## 테스트 작성 가이드

### 1. 기본 구조

```java
@SpringBootTest
@ActiveProfiles("test")
class MyServiceTest {
    
    @Autowired
    private MyService myService;
    
    @Test
    @DisplayName("테스트 설명")
    void testMethod() {
        // given
        
        // when
        
        // then
    }
}
```

### 2. 컨트롤러 테스트

```java
@WebMvcTest(MyController.class)
@ActiveProfiles("test")
class MyControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private MyService myService;
    
    @Test
    void testEndpoint() throws Exception {
        mockMvc.perform(get("/api/endpoint"))
            .andExpect(status().isOk());
    }
}
```

### 3. 트랜잭션 롤백

각 테스트는 기본적으로 트랜잭션이 롤백되어 다른 테스트에 영향을 주지 않습니다.

```java
@Transactional  // 기본적으로 롤백됨
@Test
void testWithRollback() {
    // 테스트 후 자동 롤백
}
```

## 문제 해결

### Q1. 로컬에서는 되는데 CI에서 실패해요

A. 다음을 확인하세요:
- `application-test.yml` 파일이 올바르게 커밋되었는지
- 테스트가 외부 서비스(실제 API 등)에 의존하지 않는지
- 테스트 간 상태 공유가 없는지 (격리 확인)

### Q2. H2와 MySQL의 SQL 문법 차이로 오류가 나요

A. H2의 MySQL 호환 모드를 사용하고 있지만, 일부 문법은 다를 수 있습니다:
- JPQL이나 JPA Query Methods를 사용하면 대부분 호환됩니다
- Native Query를 사용한다면 양쪽 DB에서 테스트 필요

### Q3. 테스트 실행 시 HikariCP 풀이 여러 개 생성돼요

A. 정상입니다. 각 테스트 컨텍스트마다 새로운 ApplicationContext가 생성될 수 있으며, 테스트 종료 시 자동으로 정리됩니다. 성능 최적화를 원한다면 `@DirtiesContext` 사용을 최소화하세요.

## 참고 자료

- [Spring Boot Testing Documentation](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.testing)
- [H2 Database Documentation](http://www.h2database.com/html/main.html)
- [JUnit 5 User Guide](https://junit.org/junit5/docs/current/user-guide/)
