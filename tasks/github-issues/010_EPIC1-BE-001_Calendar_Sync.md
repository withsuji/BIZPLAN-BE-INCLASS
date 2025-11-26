---
issue_number: 010
epic: EPIC-1
task_id: EPIC1-BE-001
title: "[BE] Google/Outlook Calendar 양방향 동기화 및 Webhook"
labels: ["backend", "calendar", "integration", "webhook", "priority:must"]
assignees: []
milestone: "MVP-Phase-1-Core"
dependencies: ["#001", "#002"]
parallel_group: "group-2-core"
estimated_effort: "XL"
---

# Issue #010: Google/Outlook Calendar 양방향 동기화 및 Webhook

## 📋 개요
외부 캘린더 시스템과의 양방향 동기화를 구현하고, 실시간 이벤트 변경 사항을 Webhook으로 수신한다.

## 🎯 목표
- Google/Outlook Calendar API 연동
- 실시간 양방향 동기화
- Webhook 처리

## 📝 상세 작업 내역

### 1. Calendar API 연동
- [ ] CalendarConnection, CalendarEvent Entity 생성
- [ ] Google Calendar API Client 구현 (`google-api-client`)
- [ ] Microsoft Graph API Client 구현
- [ ] OAuth 토큰 기반 인증 연동

### 2. 양방향 동기화
- [ ] Pull Sync: 주기적 동기화 (5분마다)
- [ ] Push Sync: 내부 이벤트를 외부에 반영
- [ ] Conflict Resolution: 최신 타임스탬프 우선
- [ ] Sync Status 추적

### 3. Webhook 수신
- [ ] POST `/api/v1/calendar/webhook/{provider}` 구현
- [ ] Google: X-Goog-Channel-Token 검증
- [ ] Microsoft: clientState 검증
- [ ] SyncQueue 구현 (Redis 또는 SQS)
- [ ] Idempotency 처리

### 4. Background Worker
- [ ] CalendarSyncService 구현
- [ ] Scheduled Task (주기적 Pull Sync)
- [ ] Event Processing Worker
- [ ] Error Handling 및 Retry 메커니즘

## ✅ 완료 조건
- [ ] 사용자가 Google/Outlook 캘린더 연결 가능
- [ ] 외부 캘린더 이벤트가 30초 이내에 동기화
- [ ] 내부 생성 이벤트가 외부 캘린더에 반영
- [ ] Webhook으로 실시간 변경 사항 수신

## 📚 참고 문서
- SRS: `docs/GPT-SRS-v02.md` (Section 3.1 External Systems)
- Task Spec: `tasks/functional/EPIC1-BE-001_Calendar_Sync.md`
- REQ-IDs: REQ-FUNC-001, REQ-FUNC-003

## 🔗 의존성
**선행 작업**: #001 (DB), #002 (OAuth)
**후행 작업**: #011 (Policy Engine), #012 (Focus Blocks), #013 (Time Tracking)
**병렬 가능**: #014 (LLM Pipeline), #007 (Logging)

## ⚠️ 주의사항
- 외부 API Rate Limit 준수
- Webhook 엔드포인트 보안 강화 (HMAC 검증)
- 대량 이벤트 동기화 시 배치 처리 고려

## 🏷️ 라벨
`backend`, `calendar`, `integration`, `webhook`, `priority:must`, `size:XL`

