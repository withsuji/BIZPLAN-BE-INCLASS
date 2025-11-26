---
issue_number: 008
epic: EPIC-0
task_id: EPIC0-FE-004
title: "[FE] 회의 요약 및 액션 아이템 뷰 UI"
labels: ["frontend", "ui", "meeting", "summary", "react", "priority:must", "status:completed"]
assignees: []
milestone: "MVP-Phase-0-FE-PoC"
dependencies: ["#006"]
parallel_group: "group-2-fe-detail"
estimated_effort: "S"
status: "✅ COMPLETED"
completed_date: "2025-11-26"
note: "별도 프로젝트에서 완수됨. GitHub Issue 생성 불필요."
---

# ~~Issue #008: 회의 요약 및 액션 아이템 뷰 UI~~ ✅ **완료됨**

> ⚠️ **본 이슈는 별도 FE 프로젝트에서 이미 완료되었습니다. GitHub Issue 생성이 필요하지 않습니다.**

## 📋 개요
AI가 생성한 회의 요약, 결정 사항, 액션 아이템을 확인하고 수정하는 상세 화면을 구현한다.

## 🎯 목표
- 요약 내용을 깔끔하게 표시
- 액션 아이템 수정 가능
- 재생성 인터랙션

## 📝 상세 작업 내역

### 1. 화면 구성
- [ ] MeetingDetailPage 컴포넌트 생성 (`/meetings/:id`)
- [ ] Header: 회의 제목, 일시, 참석자, 상태
- [ ] Summary Section: 한 줄 요약, Key Decisions, Action Items
- [ ] Transcript Tab: 원문 스크립트 (접기/펼치기)

### 2. Action Items
- [ ] ActionItemList 컴포넌트 구현
- [ ] 체크박스, 담당자, 기한 표시
- [ ] 수정 가능한 Input으로 전환 가능

### 3. 상호작용
- [ ] "Copy to Clipboard" 버튼
- [ ] "Regenerate Summary" 버튼 (Loading Skeleton)
- [ ] "Edit" 모드 진입 버튼
- [ ] Mock 데이터 바인딩

### 4. Mock Data
- [ ] 회의 요약 샘플 데이터 정의
- [ ] 재생성 시 `setTimeout`으로 로딩 시뮬레이션

## ✅ 완료 조건
- [ ] 요약 데이터가 가독성 있게 표시
- [ ] 재생성/로딩 상태 UI가 자연스러움
- [ ] 액션 아이템 수정 가능

## 📚 참고 문서
- SRS: `docs/GPT-SRS-v02.md` (Section 3.2 Meeting/Summary Service)
- Task Spec: `tasks/functional/EPIC0-FE-004_Meeting_Summary_View.md`
- REQ-IDs: REQ-FUNC-011

## 🔗 의존성
**선행 작업**: #006 (Dashboard - 회의 목록에서 진입)
**후행 작업**: #016 (Summary Storage - 실제 API 연동 시)
**병렬 가능**: #009 (Billing UI)

## ⚠️ 주의사항
- Mock 데이터로 충분히 검증
- 타이포그래피에 신경 쓸 것

## 🏷️ 라벨
`frontend`, `ui`, `meeting`, `summary`, `react`, `priority:must`, `size:S`

