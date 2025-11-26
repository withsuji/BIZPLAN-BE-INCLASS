---
issue_number: 012
epic: EPIC-2
task_id: EPIC2-BE-001
title: "[BE] 포커스 블록 생성 및 동적 차단 규칙"
labels: ["backend", "focus", "dnd", "priority:must"]
assignees: []
milestone: "MVP-Phase-2-Advanced"
dependencies: ["#010"]
parallel_group: "group-3-advanced"
estimated_effort: "L"
---

# Issue #012: 포커스 블록 생성 및 동적 차단 규칙

## 📋 개요
사용자의 집중 시간을 보호하는 포커스 블록을 자동/수동으로 생성하고, 미팅 및 알림 차단 규칙을 적용한다.

## 🎯 목표
- 포커스 블록 자동/수동 생성
- 미팅 및 알림 차단
- 통계 및 리포트

## 📝 상세 작업 내역

### 1. 포커스 블록 생성
- [ ] FocusBlock, FocusBlockViolation, UserFocusPreference Entity 생성
- [ ] FocusBlockService 구현 (CRUD)
- [ ] AutoFocusBlockScheduler 구현 (매주 일요일 밤 실행)
- [ ] Recurrence Rule Parser (RFC 5545 RRULE)

### 2. 차단 규칙
- [ ] FocusBlockEnforcer 구현 (미팅 예약 시 차단 검증)
- [ ] DND Integration (Slack API, Gmail API 연동)
- [ ] 긴급 미팅 예외 처리

### 3. API 엔드포인트
- [ ] POST `/api/v1/focus/blocks` (포커스 블록 생성)
- [ ] GET `/api/v1/focus/blocks` (조회)
- [ ] PUT `/api/v1/focus/blocks/{id}` (수정)
- [ ] DELETE `/api/v1/focus/blocks/{id}` (삭제)
- [ ] GET `/api/v1/focus/stats` (통계)

### 4. 통계 및 리포트
- [ ] 주간 포커스 시간 합계
- [ ] 목표 대비 달성률
- [ ] 방해 받은 횟수
- [ ] 생산성 점수 계산

### 5. 외부 캘린더 동기화
- [ ] 포커스 블록을 CalendarEvent로 동기화
- [ ] `is_focus_block=true` 플래그 설정

## ✅ 완료 조건
- [ ] 사용자가 포커스 블록을 생성하고 관리 가능
- [ ] 포커스 블록 시간대에 미팅 예약 시도 시 차단
- [ ] 자동 생성 기능 활성화 시 매주 포커스 블록 생성
- [ ] 주간 포커스 시간 통계 조회 가능

## 📚 참고 문서
- SRS: `docs/GPT-SRS-v02.md` (Section 3.2 Focus Service)
- Task Spec: `tasks/functional/EPIC2-BE-001_Focus_Blocks.md`
- REQ-IDs: REQ-FUNC-002

## 🔗 의존성
**선행 작업**: #010 (Calendar Sync)
**후행 작업**: 없음
**병렬 가능**: #011 (Policy Engine), #013 (Time Tracking), #014 (LLM Pipeline)

## ⚠️ 주의사항
- 외부 캘린더 동기화 시 포커스 블록이 일반 이벤트로 보임
- 반복 패턴 복잡도에 따른 버그 가능성
- DND 통합 시 외부 API 의존성 증가

## 🏷️ 라벨
`backend`, `focus`, `dnd`, `priority:must`, `size:L`

