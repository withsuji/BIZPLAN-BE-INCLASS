# Calendar Agent (BIZPLAN) - AI 생산성행동 트래킹 자동화

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0.0-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://openjdk.java.net/)
[![Gradle](https://img.shields.io/badge/Gradle-8.x-blue.svg)](https://gradle.org/)
[![License](https://img.shields.io/badge/License-Proprietary-red.svg)]()

## 📋 목차

- [프로젝트 개요](#-프로젝트-개요)
- [핵심 기능](#-핵심-기능)
- [기술 스택](#-기술-스택)
- [아키텍처](#-아키텍처)
- [시작하기](#-시작하기)
- [개발 가이드](#-개발-가이드)
- [API 문서](#-api-문서)
- [테스트](#-테스트)
- [배포](#-배포)
- [기여하기](#-기여하기)
- [문서](#-문서)

---

## 🎯 프로젝트 개요

**Calendar Agent (BIZPLAN)**는 일정·시간추적·정산 데이터를 통합하여 생산성 향상을 돕는 AI 기반 자동화 플랫폼입니다.

### 해결하는 문제

- **일정·리소스·정산 데이터 분절**: 동기화 비용, 누락, 지연 발생
- **알림·회의의 포커스 침식**: 컨텍스트 스위칭 증가, 생산성 저하
- **일정→시간기록→인보이스 파이프라인 부재**: 수동 작업, 미수·연체 리스크
- **온콜·교대 로스터 분산**: 야간 알림 과다, 휴식 규칙 미준수

### 목표 및 KPI

| 지표 | 목표 | 측정 주기 |
|------|------|-----------|
| 자동화 워크플로 완료율 (북극성 KPI) | 기준선 대비 +60%p | 주간 |
| 회의 조율 시간 | -50% 이상 | 주간 |
| 불필요 회의 비중 | -30% | 주간 |
| 주간 포커스 타임 비중 | +20~40%p | 주간 |
| 인보이스 리드타임 | -40% | 주간 |
| 미수·연체율 | -30~50% | 주간 |

### 타겟 사용자

- **G1**: 다시간대 협업 지식노동자 (타임존 조율·포커스 보호)
- **G2**: 미팅 다빈도 사무직 (회의 축소·맥락 전환 최소화)
- **G4**: 프리랜서/전문가 (일정=시간기록=인보이스 파이프라인)
- **G6**: 멀티앱 긱 워커 (수입·지출 통합, 연체 방지)
- **운영 리더**: 온콜/교대 관리 (로스터·DND·준거성)

---

## ✨ 핵심 기능

### 1. 타임존 자동 스케줄링
- 팀 캘린더·업무시간·공휴일·포커스 규칙 반영
- 충돌 회피 및 대체 슬롯 자동 제안 (≥2개)
- 포커스 블록 보존율 ≥95%
- 평균 확정 리드타임 ≤2시간

### 2. 포커스 보호 및 회의 요약
- 짧은 미팅 프리셋 (25/50분)
- AI 기반 자동 회의 요약 (p95 ≤30초)
  - 의사결정, 액션아이템, 오너, 기한 자동 추출
- DND(방해금지) 알림 묶음 처리

### 3. 일정→시간기록→인보이스 자동화 파이프라인
- 일정 완료 시 시간기록 자동 생성 (≥95%)
- 마감 주기별 인보이스 자동 생성·발송 (무인 처리율 ≥80%)
- 미수금 회수 플로우 (알림·부분 청구·재발송)
- 현금흐름 예측 및 최소 상환 경고

### 4. 페르소나 프리셋 및 승인 플로우
- 역할별 규칙·템플릿·리포트 기본 제공
- 예외 처리 승인 플로우
- 감사 로그 (1년+ 보존, append-only)

---

## 🛠 기술 스택

### Backend
- **Language**: Java 21
- **Framework**: Spring Boot 4.0.0
- **Build Tool**: Gradle 8.x
- **Architecture**: Layered Architecture (Controller → Service → Repository)

### AI/LLM Service
- **Language**: Python 3.10+
- **Framework**: FastAPI
- **Libraries**: LangChain, Pydantic
- **LLM Gateway**: 내부 LLM Gateway (OpenAI 호환) + Google Gemini

### Database
- **Database**: AWS Aurora MySQL 3.x (MySQL 8.x 호환)
- **Engine**: InnoDB
- **Charset**: `utf8mb4`
- **ORM**: JPA (Hibernate)
- **Migration**: Flyway (권장) or Liquibase

### Infrastructure
- **Cloud**: AWS
- **Compute**: ECS/Fargate or Lambda (배치 워커)
- **Monitoring**: Grafana, Amplitude, BigQuery
- **Alerting**: PagerDuty, Slack

### External Integrations
- Google Calendar / Microsoft Outlook (OAuth 2.0)
- 결제 게이트웨이 (예: Stripe)
- LLM Gateway (사내 Gemini 기반)

---

## 🏗 아키텍처

### 시스템 컨텍스트

```
┌─────────────┐     ┌──────────────────┐     ┌─────────────┐
│   사용자    │────▶│   Web App (React) │◀────│   Admin     │
└─────────────┘     └──────────────────┘     └─────────────┘
                            │
                            ▼
                    ┌──────────────┐
                    │  API Gateway  │
                    └──────────────┘
                            │
        ┌───────────────────┼───────────────────┐
        ▼                   ▼                   ▼
┌──────────────┐    ┌──────────────┐    ┌──────────────┐
│   Calendar   │    │    Focus     │    │   Summary    │
│   Service    │    │   Service    │    │   Service    │
└──────────────┘    └──────────────┘    └──────────────┘
        │                   │                   │
        └───────────────────┼───────────────────┘
                            ▼
                    ┌──────────────┐
                    │  Time Track  │
                    │   Service    │
                    └──────────────┘
                            │
                            ▼
                    ┌──────────────┐
                    │   Billing/   │
                    │   Dunning    │
                    └──────────────┘
                            │
                    ┌───────┴───────┐
                    ▼               ▼
            ┌──────────────┐ ┌──────────────┐
            │  MySQL DB    │ │  LLM Gateway │
            │  (Aurora)    │ │  (Gemini)    │
            └──────────────┘ └──────────────┘
```

### 핵심 엔터티

- **User, Team**: 사용자·팀 관리
- **CalendarEvent**: 일정 (Google/Outlook 동기화)
- **FocusBlock**: 포커스 블록·차단 규칙
- **SummaryNote**: AI 생성 회의 요약
- **TimeEntry**: 시간기록 (일정→시간 자동 전환)
- **Invoice**: 인보이스 (시간→청구 자동 전환)
- **PaymentReminder**: 미수금 알림 (Dunning)
- **RosterRule**: 온콜·알림 정책
- **AuditLog**: 감사 로그 (불변)

---

## 🚀 시작하기

### 사전 요구사항

- **Java**: JDK 21 이상
- **Gradle**: 8.x 이상
- **MySQL**: 8.x 이상 (로컬 개발용) 또는 Docker
- **Python**: 3.10+ (AI/LLM 서비스 개발 시)

### 설치 및 실행

#### 1. 저장소 클론

```bash
git clone <repository-url>
cd BIZPLAN-BE-INCLASS
```

#### 2. 환경 설정

`src/main/resources/application.properties` 파일을 생성하거나 수정:

```properties
# Database
spring.datasource.url=jdbc:mysql://localhost:3306/bizplan?useSSL=false&serverTimezone=UTC&characterEncoding=UTF-8
spring.datasource.username=root
spring.datasource.password=your_password
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# JPA/Hibernate
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQL8Dialect

# Flyway
spring.flyway.enabled=true
spring.flyway.locations=classpath:db/migration
spring.flyway.baseline-on-migrate=true

# Server
server.port=8080
```

#### 3. 데이터베이스 준비

```bash
# Docker로 MySQL 실행 (선택사항)
docker run --name mysql-bizplan \
  -e MYSQL_ROOT_PASSWORD=your_password \
  -e MYSQL_DATABASE=bizplan \
  -p 3306:3306 \
  -d mysql:8.0 \
  --character-set-server=utf8mb4 \
  --collation-server=utf8mb4_unicode_ci
```

#### 4. 빌드 및 실행

```bash
# Gradle 빌드
./gradlew clean build

# 애플리케이션 실행
./gradlew bootRun
```

서버가 `http://localhost:8080`에서 실행됩니다.

---

## 💻 개발 가이드

### 코딩 규칙

#### 네이밍 컨벤션

- **클래스**: `PascalCase` (예: `UserService`)
- **메서드/변수**: `camelCase` (예: `getUserById`)
- **상수**: `UPPER_SNAKE_CASE` (예: `MAX_RETRY_COUNT`)
- **DTO**: `~Dto`, `~Request`, `~Response` 접미사 (예: `UserCreateRequest`)
- **테이블**: `snake_case`, 복수형 (예: `users`, `calendar_events`)
- **컬럼**: `snake_case` (예: `created_at`, `user_id`)

#### 코드 스타일

```java
// Lombok 활용
@Getter
@RequiredArgsConstructor
@Builder
public class UserService {
    
    private final UserRepository userRepository;
    
    // Constructor Injection (권장)
    // @Autowired on fields 지양
    
    @Transactional(readOnly = true)
    public User getUserById(Long id) {
        return userRepository.findById(id)
            .orElseThrow(() -> new UserNotFoundException(id));
    }
    
    @Transactional
    public User createUser(UserCreateRequest request) {
        // 비즈니스 로직
        User user = User.builder()
            .name(request.getName())
            .email(request.getEmail())
            .build();
        return userRepository.save(user);
    }
}
```

#### 로깅

```java
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class CalendarService {
    
    public void syncCalendar(Long userId) {
        log.info("Starting calendar sync for user: {}", userId);
        try {
            // 동기화 로직
            log.debug("Sync completed successfully");
        } catch (Exception e) {
            log.error("Calendar sync failed for user: {}", userId, e);
            throw new CalendarSyncException("Failed to sync calendar", e);
        }
    }
}
```

**주의**: `System.out.println()` 사용 금지 → `@Slf4j` 사용

#### 예외 처리

```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFound(UserNotFoundException e) {
        ErrorResponse error = ErrorResponse.builder()
            .code("USER_NOT_FOUND")
            .message(e.getMessage())
            .build();
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneral(Exception e) {
        ErrorResponse error = ErrorResponse.builder()
            .code("INTERNAL_ERROR")
            .message("An unexpected error occurred")
            .build();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
```

### 데이터베이스 마이그레이션

Flyway를 사용하여 스키마 버전 관리:

```sql
-- src/main/resources/db/migration/V1__init_schema.sql
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    team_id BIGINT,
    email VARCHAR(255) NOT NULL UNIQUE,
    role ENUM('owner', 'member', 'viewer') NOT NULL DEFAULT 'member',
    timezone VARCHAR(64) NOT NULL DEFAULT 'UTC',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    INDEX idx_team_id (team_id),
    INDEX idx_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

**중요**: Production에서 `spring.jpa.hibernate.ddl-auto=update` 절대 사용 금지

### 트랜잭션 관리

```java
@Service
@RequiredArgsConstructor
public class TimeTrackingService {
    
    private final TimeEntryRepository timeEntryRepository;
    
    // 조회 작업: readOnly=true
    @Transactional(readOnly = true)
    public List<TimeEntry> getTimeEntries(Long userId) {
        return timeEntryRepository.findByUserId(userId);
    }
    
    // 쓰기 작업: @Transactional
    @Transactional
    public TimeEntry createTimeEntry(TimeEntryCreateRequest request) {
        TimeEntry entry = TimeEntry.builder()
            .eventId(request.getEventId())
            .durationMin(request.getDurationMin())
            .rate(request.getRate())
            .build();
        return timeEntryRepository.save(entry);
    }
}
```

---

## 📚 API 문서

### API 표준

- **프로토콜**: REST (JSON)
- **문서화**: OpenAPI 3.x (Swagger)
  - Spring Boot: `springdoc-openapi` 사용
  - 접속: `http://localhost:8080/swagger-ui.html`

### 경로 및 메서드

- **리소스**: 복수 명사 (예: `/api/v1/users`)
- **메서드**:
  - `GET`: 조회
  - `POST`: 생성 또는 복잡한 액션
  - `PUT`/`PATCH`: 수정 (부분 수정은 PATCH 권장)
  - `DELETE`: 삭제

### 응답 형식

#### 성공

```json
{
  "id": 123,
  "name": "John Doe",
  "email": "john@example.com",
  "createdAt": "2025-11-26T10:00:00.000Z"
}
```

#### 에러

```json
{
  "code": "USER_NOT_FOUND",
  "message": "User with id 123 not found",
  "details": {
    "userId": 123
  }
}
```

### 주요 API 엔드포인트

| Method | Path | 목적 | 인증 |
|--------|------|------|------|
| `GET` | `/api/v1/schedule/slots` | 가용/대체 슬롯 조회 | OAuth |
| `POST` | `/api/v1/schedule/book` | 슬롯 확정/생성 | OAuth |
| `POST` | `/api/v1/meetings/{id}/summary` | 회의 요약 생성 | OAuth |
| `GET` | `/api/v1/time-entries` | 시간기록 조회 | OAuth |
| `POST` | `/api/v1/invoices/run-billing-cycle` | 마감 배치 실행 | OAuth |
| `POST` | `/api/v1/dunning/reminders` | 미수 알림 발송 | OAuth |
| `GET` | `/api/v1/audit-logs` | 감사 로그 조회 | OAuth |

전체 API 명세는 SRS 문서 참조: `docs/GPT-SRS-v02.md`

---

## 🧪 테스트

### 테스트 전략

- **Framework**: JUnit 5 + Mockito
- **비즈니스 로직**: Service 계층 단위 테스트 필수
- **Controller**: `@WebMvcTest` 또는 `@SpringBootTest` 통합 테스트

### 테스트 실행

```bash
# 전체 테스트
./gradlew test

# 특정 테스트
./gradlew test --tests UserServiceTest

# 테스트 커버리지 리포트
./gradlew test jacocoTestReport
```

### 테스트 예시

```java
@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    
    @Mock
    private UserRepository userRepository;
    
    @InjectMocks
    private UserService userService;
    
    @Test
    @DisplayName("사용자 조회 성공")
    void getUserById_Success() {
        // Given
        Long userId = 1L;
        User user = User.builder()
            .id(userId)
            .email("test@example.com")
            .build();
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        
        // When
        User result = userService.getUserById(userId);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo("test@example.com");
        verify(userRepository).findById(userId);
    }
    
    @Test
    @DisplayName("사용자 조회 실패 - 존재하지 않는 ID")
    void getUserById_NotFound() {
        // Given
        Long userId = 999L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());
        
        // When & Then
        assertThrows(UserNotFoundException.class, 
            () -> userService.getUserById(userId));
    }
}
```

---

## 🚢 배포

### 환경 구성

- **Development**: 로컬 개발 환경
- **Staging**: 테스트 및 QA 환경
- **Production**: AWS 기반 프로덕션 환경

### CI/CD

(TBD: GitHub Actions, Jenkins, GitLab CI 등 설정 예정)

### 프로덕션 체크리스트

- [ ] `spring.jpa.hibernate.ddl-auto=validate` 설정
- [ ] Flyway 마이그레이션 스크립트 검증
- [ ] 환경 변수로 민감 정보 관리 (DB 비밀번호, API 키 등)
- [ ] 로깅 레벨 조정 (`INFO` 또는 `WARN`)
- [ ] 모니터링 및 알림 설정 (Grafana, PagerDuty)
- [ ] 보안: OAuth 2.0, HTTPS, PII 마스킹
- [ ] 성능: API p95 ≤500ms, 가용성 ≥99.5%

---

## 🤝 기여하기

### Git 워크플로

1. Feature 브랜치 생성: `git checkout -b feat/your-feature`
2. 변경 사항 커밋: `git commit -m "feat: add new feature"`
3. 브랜치 푸시: `git push origin feat/your-feature`
4. Pull Request 생성

### 커밋 메시지 규칙

[Conventional Commits](https://www.conventionalcommits.org/) 준수 (영어):

- `feat:` 새로운 기능
- `fix:` 버그 수정
- `docs:` 문서 변경
- `style:` 코드 포맷팅 (기능 변경 없음)
- `refactor:` 리팩토링
- `test:` 테스트 추가/수정
- `chore:` 빌드 설정, 의존성 등

**예시**:
```
feat: add time entry auto-creation from calendar events
fix: resolve timezone conversion issue in slot calculation
docs: update API documentation for billing endpoints
```

### 코드 리뷰

- 모든 PR은 최소 1명 이상의 리뷰 필요
- 테스트 통과 필수
- Lint 및 포맷팅 규칙 준수

---

## 📖 문서

### 주요 문서

- **PRD (Product Requirements Document)**: [`docs/GPT-PRD.md`](docs/GPT-PRD.md)
- **SRS (Software Requirements Specification)**: [`docs/GPT-SRS-v02.md`](docs/GPT-SRS-v02.md)
- **MVP Task WBS**: [`docs/MVP_Task_WBS_and_DAG.md`](docs/MVP_Task_WBS_and_DAG.md)
- **AI Agent Tasks 가이드**: [`docs/AI_AGENT_TASKS_USAGE_GUIDE.md`](docs/AI_AGENT_TASKS_USAGE_GUIDE.md)

### 개발 규칙

- **프로젝트 컨텍스트**: [`.cursor/rules/000-context.mdc`](.cursor/rules/000-context.mdc)
- **Java 백엔드 규칙**: [`.cursor/rules/100-java-backend.mdc`](.cursor/rules/100-java-backend.mdc)
- **Python AI 서비스 규칙**: [`.cursor/rules/101-python-ai.mdc`](.cursor/rules/101-python-ai.mdc)
- **데이터베이스 규칙**: [`.cursor/rules/200-database.mdc`](.cursor/rules/200-database.mdc)
- **API 설계 규칙**: [`.cursor/rules/300-api-general.mdc`](.cursor/rules/300-api-general.mdc)

---

## 📊 성능 및 모니터링

### SLO (Service Level Objectives)

| 지표 | 목표 |
|------|------|
| UI 응답 p95 | ≤ 800ms |
| 백엔드 API p95 | ≤ 500ms |
| 배치/동기화 지연 p95 | ≤ 5분 |
| 요약 생성 p95 | ≤ 30초 |
| 월 가용성 | ≥ 99.5% (Beta) |
| 오류율 (5xx) | ≤ 0.5% |

### 모니터링 항목

- 요청 지연 (p95/p99)
- API 실패율
- 동기화 백로그
- 요약 생성 지연
- 인보이스 파이프라인 전환율
- 데이터베이스 성능

---

## 📞 문의 및 지원

- **Team**: Product · Engineering · Data
- **Repository**: [Project Repository URL]
- **Issues**: [Issue Tracker URL]
- **Slack**: #bizplan-dev

---

## 📄 라이선스

Proprietary - All rights reserved.

---

## 🔄 변경 이력

### v0.0.1-SNAPSHOT (2025-11-26)
- 초기 프로젝트 설정
- Spring Boot 4.0.0 + Java 21 기반 구조 생성
- 기본 문서 작성 (README, PRD, SRS)

---

**Last Updated**: 2025-11-26

