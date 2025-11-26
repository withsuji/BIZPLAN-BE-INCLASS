# EPIC4-NFR-001: 구조화 로깅 및 중앙 집중식 모니터링 환경 구성

## 1. 개요 및 목적
시스템의 **관측성(Observability)**을 확보하기 위해, 모든 애플리케이션 로그를 JSON 포맷으로 구조화하고 중앙 로그 저장소(CloudWatch Logs 또는 Datadog 등)로 수집하는 파이프라인을 구축한다. 또한 주요 비즈니스 이벤트(일정 생성, 결제 등)와 시스템 에러에 대한 기본 모니터링 대시보드를 구성한다.

## 2. 상세 요구사항

### 2.1. 구조화 로깅 (Structured Logging)
- **포맷**: 모든 로그는 **JSON** 포맷으로 출력해야 한다.
- **필수 필드**:
    - `trace_id`, `span_id` (분산 추적용)
    - `level` (INFO, WARN, ERROR)
    - `timestamp` (ISO8601)
    - `service_name`, `environment`
    - `user_id` (로그인된 경우), `request_path`
- **Logback/MDC**: Spring Boot(Java) 환경에서 MDC를 활용해 요청별 Context 정보를 로그에 자동 주입한다.

### 2.2. 로그 수집 및 저장
- **Log Router**: ECS Task 정의에서 `awslogs` 드라이버를 사용하여 stdout/stderr 로그를 CloudWatch Logs로 전송한다.
- **Retention**: 로그 보존 기간은 개발계 7일, 운영계 90일(또는 규정에 따름)로 설정한다.

### 2.3. 모니터링 대시보드
- **Golden Signals**: Latency(p95, p99), Traffic(RPS), Errors(5xx Rate), Saturation(CPU/Mem) 시각화.
- **Alert**: 에러율 1% 초과 또는 응답지연 1s 초과 시 슬랙/이메일 알림 연동.

## 3. Task Definition (YAML)

```yaml
task_id: "EPIC4-NFR-001"
title: "JSON 구조화 로깅 표준 적용 및 CloudWatch 모니터링 설정"
summary: >
  애플리케이션 로그를 JSON 포맷으로 표준화하여 추적성을 높이고,
  CloudWatch 기반의 로그 수집 및 기본 대시보드/알람을 구성한다.
type: "non_functional"

epic: "EPIC_4_SYSTEM_NFR"
req_ids: ["REQ-NF-012", "REQ-NF-006", "REQ-NF-002"]
component: ["backend.common", "infra.monitoring"]

category: "observability"
labels:
  - "logging:json"
  - "monitoring:cloudwatch"
  - "ops:alerting"

context:
  srs_section: "4.2 Non-Functional Requirements (REQ-NF-012)"
  related_req_func: ["REQ-FUNC-ALL"]

requirements:
  description: "모든 API 요청에 대해 Trace ID가 포함된 JSON 로그가 남아야 한다."
  kpis:
    - "로그 파싱 에러율 0%"
    - "API 요청 시작~종료 로그 쌍 매칭 가능"

design_constraints:
  - "PII(개인식별정보)는 로그에 평문으로 남기지 않고 마스킹 처리해야 한다."
  - "로그 라이브러리는 `Logback` + `ECS Encoder` 또는 동등한 JSON Encoder를 사용한다."

steps_hint:
  - "Spring Boot Logback 설정(`logback-spring.xml`)에 JSON Appender 추가"
  - "MDC Filter 구현 (Request ID, User ID 추출 및 주입)"
  - "ECS Task Definition에 LogConfiguration(awslogs) 설정 추가"
  - "Terraform으로 CloudWatch Log Group 및 Metric Filter 생성"
  - "CloudWatch Dashboard 리소스 정의 (Terraform)"

preconditions:
  - "EPIC4-SYS-001 (ECS Infra) 또는 로컬 Docker 환경"

postconditions:
  - "애플리케이션 구동 시 JSON 형태의 로그가 콘솔에 출력된다."
  - "CloudWatch 콘솔에서 로그 스트림을 검색할 수 있다."

dependencies: ["EPIC4-SYS-001"]

parallelizable: true
estimated_effort: "M"
priority: "Must"
agent_profile: ["backend", "infra"]

risk_notes:
  - "로그 양이 많을 경우 CloudWatch 비용이 급증할 수 있으므로, 불필요한 DEBUG 로그는 제외한다."
```









