---
issue_number: 007
epic: EPIC-4
task_id: EPIC4-NFR-001
title: "[NFR] 구조화 로깅 및 모니터링 설정"
labels: ["backend", "nfr", "logging", "monitoring", "cloudwatch", "priority:must"]
assignees: []
milestone: "MVP-Phase-1-Core"
dependencies: ["#001"]
parallel_group: "group-2-nfr"
estimated_effort: "M"
---

# Issue #007: 구조화 로깅 및 모니터링 설정

## 📋 개요
애플리케이션 로그를 JSON 포맷으로 표준화하고 CloudWatch 기반 모니터링 환경을 구축한다.

## 🎯 목표
- 관측성(Observability) 확보
- 구조화된 로그 수집 및 분석
- 기본 모니터링 대시보드 구성

## 📝 상세 작업 내역

### 1. 구조화 로깅
- [ ] Logback 설정 (`logback-spring.xml`)에 JSON Appender 추가
- [ ] MDC Filter 구현 (Request ID, User ID 주입)
- [ ] 필수 필드 정의: `trace_id`, `span_id`, `level`, `timestamp`, `service_name`
- [ ] PII 마스킹 유틸리티 구현

### 2. 로그 수집
- [ ] ECS Task Definition에 LogConfiguration(awslogs) 설정
- [ ] CloudWatch Log Group 생성 (Terraform)
- [ ] 로그 보존 기간 설정 (개발: 7일, 운영: 90일)

### 3. 모니터링 대시보드
- [ ] CloudWatch Dashboard 리소스 정의
- [ ] Golden Signals 시각화:
  - Latency (p95, p99)
  - Traffic (RPS)
  - Errors (5xx Rate)
  - Saturation (CPU/Mem)
- [ ] Metric Filter 생성

### 4. 알림 설정
- [ ] CloudWatch Alarm 생성
- [ ] 에러율 1% 초과 시 알림
- [ ] 응답지연 1s 초과 시 알림
- [ ] Slack/이메일 알림 연동

## ✅ 완료 조건
- [ ] 애플리케이션 구동 시 JSON 로그 출력
- [ ] CloudWatch 콘솔에서 로그 검색 가능
- [ ] 모니터링 대시보드에서 주요 지표 확인 가능
- [ ] 알림 테스트 성공

## 📚 참고 문서
- SRS: `docs/GPT-SRS-v02.md` (Section 4.2 Non-Functional Requirements)
- Task Spec: `tasks/non-functional/EPIC4-NFR-001_Logging_and_Monitoring.md`
- REQ-IDs: REQ-NF-012, REQ-NF-006, REQ-NF-002

## 🔗 의존성
**선행 작업**: #001 (ECS Infra)
**후행 작업**: 모든 백엔드 작업에서 로깅 활용
**병렬 가능**: #004 (CI/CD), #010 (Calendar Sync)

## ⚠️ 주의사항
- 로그 양이 많을 경우 CloudWatch 비용 급증 가능
- 불필요한 DEBUG 로그 제외
- PII는 평문으로 로그에 남기지 않음

## 🏷️ 라벨
`backend`, `nfr`, `logging`, `monitoring`, `cloudwatch`, `priority:must`, `size:M`

