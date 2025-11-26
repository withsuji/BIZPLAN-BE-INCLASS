---
issue_number: 016
epic: EPIC-2
task_id: EPIC2-BE-002
title: "[BE] LLM 요약 결과 파싱 및 구조화 저장"
labels: ["backend", "meeting", "summary", "priority:must"]
assignees: []
milestone: "MVP-Phase-2-Advanced"
dependencies: ["#014"]
parallel_group: "group-4-summary"
estimated_effort: "L"
---

# Issue #016: LLM 요약 결과 파싱 및 구조화 저장

## 📋 개요
LLM 생성 요약을 DB에 저장하고, 액션 아이템을 추출하여 관리 가능한 형태로 제공한다.

## 🎯 목표
- 구조화된 요약 저장
- 액션 아이템 생명주기 관리
- 버전 관리

## 📝 상세 작업 내역

### 1. 데이터 모델
- [ ] MeetingSummary, ActionItem Entity 생성
- [ ] Repository 생성
- [ ] 버전 관리 컬럼 추가

### 2. Summary Service
- [ ] MeetingSummaryService 구현 (CRUD 및 버전 관리)
- [ ] ActionItemService 구현 (상태 관리, 리마인더)
- [ ] SummaryWebhookController (Python Service로부터 결과 수신)
- [ ] 데이터 검증 로직

### 3. API 엔드포인트
- [ ] GET `/api/v1/meetings/{id}/summary` (최신 요약 조회)
- [ ] GET `/api/v1/meetings/{id}/summary/versions` (버전 히스토리)
- [ ] PATCH `/api/v1/meetings/{id}/summary` (요약 수정)
- [ ] PUT `/api/v1/action-items/{id}` (액션 아이템 수정)
- [ ] POST `/api/v1/meetings/{id}/summary/regenerate` (재생성)

### 4. 액션 아이템 관리
- [ ] 담당자 이메일 → User ID 매핑
- [ ] 상태 관리 (TODO, IN_PROGRESS, DONE, CANCELLED)
- [ ] ActionItemReminderScheduler (매일 00시 실행)
- [ ] 이메일/Slack 알림 발송

### 5. 검색 및 필터링
- [ ] 키워드 검색 (요약 내용, 액션 아이템)
- [ ] 필터링 (날짜, 참석자, 상태, 담당자)
- [ ] 통계 (완료율, 평균 완료 시간)

## ✅ 완료 조건
- [ ] LLM 생성 요약이 DB에 저장
- [ ] 액션 아이템이 별도로 추출되어 관리
- [ ] 사용자가 액션 아이템 상태 변경 가능
- [ ] 기한 전날 담당자에게 리마인더 발송
- [ ] 요약 수정 시 버전 증가 및 히스토리 유지

## 📚 참고 문서
- SRS: `docs/GPT-SRS-v02.md` (Section 3.2 Meeting/Summary Service)
- Task Spec: `tasks/functional/EPIC2-BE-002_Summary_Storage.md`
- REQ-IDs: REQ-FUNC-011

## 🔗 의존성
**선행 작업**: #014 (LLM Pipeline)
**후행 작업**: #008 (Summary UI - API 연동)
**병렬 가능**: #015 (Slot Algorithm), #017 (Invoice Generation)

## ⚠️ 주의사항
- 담당자 이메일 매칭 실패 시 수동 매핑 UI 필요
- 액션 아이템이 많을 경우 알림 폭주 가능 (배치 알림 고려)
- 버전 관리로 인한 스토리지 증가 (오래된 버전 정리 정책)

## 🏷️ 라벨
`backend`, `meeting`, `summary`, `action-item`, `priority:must`, `size:L`

