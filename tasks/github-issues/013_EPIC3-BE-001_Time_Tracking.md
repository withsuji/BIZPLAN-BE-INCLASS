---
issue_number: 013
epic: EPIC-3
task_id: EPIC3-BE-001
title: "[BE] 캘린더 이벤트 기반 자동 시간 기록"
labels: ["backend", "billing", "time-tracking", "priority:must"]
assignees: []
milestone: "MVP-Phase-2-Billing"
dependencies: ["#010"]
parallel_group: "group-3-advanced"
estimated_effort: "XL"
---

# Issue #013: 캘린더 이벤트 기반 자동 시간 기록

## 📋 개요
캘린더 이벤트를 시간 기록으로 자동 변환하고, 프로젝트/클라이언트별로 시간을 추적한다.

## 🎯 목표
- 시간 기록 자동화
- 프로젝트/클라이언트 관리
- 청구 가능 데이터 생성

## 📝 상세 작업 내역

### 1. 데이터 모델
- [ ] Project, Client, TimeEntry Entity 생성
- [ ] Repository 생성
- [ ] 관계 설정 (FK)

### 2. 자동 시간 기록
- [ ] TimeEntryService 구현 (CRUD)
- [ ] CalendarEventListener 구현 (이벤트 종료 시 TimeEntry 생성)
- [ ] ProjectMatcher 구현 (이벤트 → 프로젝트 자동 매핑)
- [ ] RoundingUtil (15분 단위 반올림)

### 3. API 엔드포인트
- [ ] POST `/api/v1/time-entries` (수동 생성)
- [ ] GET `/api/v1/time-entries` (조회 및 필터링)
- [ ] PUT `/api/v1/time-entries/{id}` (수정)
- [ ] DELETE `/api/v1/time-entries/{id}` (삭제)
- [ ] GET `/api/v1/projects` (프로젝트 목록)
- [ ] POST `/api/v1/projects` (프로젝트 생성)
- [ ] POST `/api/v1/clients` (클라이언트 생성)

### 4. 통계
- [ ] 월별/프로젝트별 시간 합계 API
- [ ] 청구 가능 시간 합계
- [ ] 요율 계산

### 5. 테스트
- [ ] 자동 생성 정확도 테스트
- [ ] 매핑 로직 테스트
- [ ] 통합 테스트

## ✅ 완료 조건
- [ ] 캘린더 이벤트 종료 시 자동으로 시간 기록 생성
- [ ] 사용자가 시간 기록을 조회/수정/삭제 가능
- [ ] 프로젝트 및 클라이언트 관리 가능
- [ ] 청구 가능 여부 토글 가능

## 📚 참고 문서
- SRS: `docs/GPT-SRS-v02.md` (Section 3.3 Time Tracking Service)
- Task Spec: `tasks/functional/EPIC3-BE-001_Time_Tracking.md`
- REQ-IDs: REQ-FUNC-020

## 🔗 의존성
**선행 작업**: #010 (Calendar Sync)
**후행 작업**: #017 (Invoice Generation)
**병렬 가능**: #011 (Policy Engine), #012 (Focus Blocks), #014 (LLM Pipeline)

## ⚠️ 주의사항
- 프로젝트 자동 매핑 정확도가 낮을 수 있음 (ML 모델 고려)
- 시간 기록이 많아질수록 쿼리 성능 저하 (인덱싱 필수)
- 청구서에 포함된 시간 기록은 수정/삭제 불가

## 🏷️ 라벨
`backend`, `billing`, `time-tracking`, `priority:must`, `size:XL`

