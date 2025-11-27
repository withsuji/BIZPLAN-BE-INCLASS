# Calendar Agent (BIZPLAN) Backend

AI Productivity Tracking Automation: Schedule → Time Tracking → Invoice

## 🛠️ Tech Stack

- **Language:** Java 17
- **Framework:** Spring Boot 3.3.x
- **Database:** MySQL 8.0 (Aurora MySQL Compatible)
- **ORM:** JPA (Hibernate) + Flyway Migration
- **Build Tool:** Gradle

## 📋 Prerequisites

### Java 17 설치 (필수)

```bash
# macOS (Homebrew)
brew install openjdk@17
export JAVA_HOME=$(/usr/libexec/java_home -v 17)

# 또는 SDKMAN (권장)
curl -s "https://get.sdkman.io" | bash
sdk install java 17.0.9-tem
sdk use java 17.0.9-tem
```

### Docker (로컬 개발용)

```bash
# Docker Desktop 설치 필요
# https://www.docker.com/products/docker-desktop
```

## 🚀 Quick Start

### 1. Docker 환경 실행

```bash
# 데이터베이스 및 Redis 실행
docker compose up -d

# 상태 확인
docker compose ps
```

### 2. 애플리케이션 실행

```bash
# Gradle로 실행
./gradlew bootRun --args='--spring.profiles.active=local'

# 또는 IDE에서 실행 (IntelliJ, VS Code)
```

### 3. 접속 정보

| 서비스 | URL | 비고 |
|--------|-----|------|
| API Server | http://localhost:8080 | Spring Boot |
| Swagger UI | http://localhost:8080/swagger-ui.html | API 문서 |
| Actuator | http://localhost:8080/actuator/health | 헬스체크 |
| Adminer | http://localhost:8081 | DB 관리 UI |
| MailHog | http://localhost:8025 | 이메일 테스트 |

### 4. 데이터베이스 접속

```bash
# MySQL CLI
docker exec -it bizplan-mysql mysql -ubizplan -pbizplanpassword bizplan

# 또는 Adminer 사용 (http://localhost:8081)
# Server: mysql, Username: bizplan, Password: bizplanpassword, Database: bizplan
```

## 📁 Project Structure

```
src/main/java/vibe/bizplan/bizplan_be_inclass/
├── common/
│   └── entity/BaseEntity.java         # 공통 엔티티 (createdAt, updatedAt)
├── team/
│   ├── entity/Team.java
│   └── repository/TeamRepository.java
├── user/
│   ├── entity/User.java, UserRole.java
│   └── repository/UserRepository.java
├── project/
│   ├── entity/Project.java, ProjectStatus.java
│   └── repository/ProjectRepository.java
├── calendar/
│   ├── entity/CalendarEvent.java, EventStatus.java
│   └── repository/CalendarEventRepository.java
├── focus/
│   ├── entity/FocusBlock.java
│   └── repository/FocusBlockRepository.java
├── summary/
│   ├── entity/SummaryNote.java
│   └── repository/SummaryNoteRepository.java
├── timetracking/
│   ├── entity/TimeEntry.java
│   └── repository/TimeEntryRepository.java
├── customer/
│   ├── entity/Customer.java
│   └── repository/CustomerRepository.java
├── invoice/
│   ├── entity/Invoice.java, InvoiceStatus.java
│   └── repository/InvoiceRepository.java
├── payment/
│   ├── entity/PaymentReminder.java, ReminderChannel.java, ReminderStatus.java
│   └── repository/PaymentReminderRepository.java
├── audit/
│   ├── entity/AuditLog.java
│   └── repository/AuditLogRepository.java
└── config/
    └── JpaAuditingConfig.java
```

## 🗄️ Database Schema

ERD 및 상세 스키마는 `tasks/github-issues/001_EPIC4-SYS-001_Infrastructure.md` 참조

### Tables (10개)

1. `teams` - 팀/조직
2. `users` - 사용자
3. `projects` - 프로젝트
4. `calendar_events` - 캘린더 이벤트
5. `focus_blocks` - 포커스 블록
6. `summary_notes` - 회의 요약
7. `time_entries` - 시간 기록
8. `customers` - 고객
9. `invoices` - 인보이스
10. `payment_reminders` - 결제 알림
11. `audit_logs` - 감사 로그

## 🧪 Testing

```bash
# 전체 테스트
./gradlew test

# 특정 테스트 클래스
./gradlew test --tests "UserRepositoryTest"
```

## 📚 Related Documents

- PRD: `docs/GPT-PRD.md`
- SRS: `docs/GPT-SRS-v02.md`
- WBS: `docs/MVP_Task_WBS_and_DAG.md`
- Issue Guide: `tasks/github-issues/ISSUE_EXECUTION_GUIDE.md`

## 🔗 Links

- GitHub Issues: https://github.com/withsuji/BIZPLAN-BE-INCLASS/issues
- Issue #14: EPIC4-SYS-001 Infrastructure
