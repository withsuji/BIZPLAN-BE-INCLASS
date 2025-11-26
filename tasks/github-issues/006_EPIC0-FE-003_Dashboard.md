---
issue_number: 006
epic: EPIC-0
task_id: EPIC0-FE-003
title: "[FE] 대시보드 및 캘린더 메인 뷰 UI"
labels: ["frontend", "ui", "calendar", "dashboard", "react", "priority:must", "status:completed"]
assignees: []
milestone: "MVP-Phase-0-FE-PoC"
dependencies: ["#003"]
parallel_group: "group-1-fe-poc"
estimated_effort: "M"
status: "✅ COMPLETED"
completed_date: "2025-11-26"
note: "별도 프로젝트에서 완수됨. GitHub Issue 생성 불필요."
---

# ~~Issue #006: 대시보드 및 캘린더 메인 뷰 UI~~ ✅ **완료됨**

> ⚠️ **본 이슈는 별도 FE 프로젝트에서 이미 완료되었습니다. GitHub Issue 생성이 필요하지 않습니다.**

## 📋 개요
내부 사용자가 자신의 일정을 확인하고 관리하는 메인 대시보드를 구현한다.

## 🎯 목표
- 주간 캘린더 뷰 구현
- 일반 일정과 포커스 블록 시각적 구분
- 일정 생성 인터랙션

## 📝 상세 작업 내역

### 1. Calendar View
- [ ] CalendarPage 컴포넌트 생성 (`/calendar`)
- [ ] Calendar 라이브러리 선정 및 설치 (`react-big-calendar`)
- [ ] 주간(Weekly) 뷰 기본 설정
- [ ] Mock Events 데이터 정의

### 2. 이벤트 표시
- [ ] 일반 미팅: 파란색 계열
- [ ] 포커스 블록: 보라색/회색 계열
- [ ] 충돌/경고: 붉은색 테두리
- [ ] 이벤트 타입별 조건부 스타일링

### 3. 상세 정보
- [ ] EventCard 컴포넌트 구현
- [ ] 일정 클릭 시 상세 모달
- [ ] 빈 공간 클릭 시 "새 일정 생성" 모달

### 4. 사이드 패널
- [ ] "오늘의 요약" 위젯 (예정 미팅 수, 포커스 시간)
- [ ] 다음 미팅 정보 카드
- [ ] "포커스 시간 확보하기" 버튼 (Mock 애니메이션)

## ✅ 완료 조건
- [ ] 사용자가 자신의 일정을 주간 뷰로 볼 수 있음
- [ ] 포커스 블록이 시각적으로 구분됨
- [ ] 일정 클릭 시 상세 모달 표시

## 📚 참고 문서
- SRS: `docs/GPT-SRS-v02.md` (Section 3.2 Client Applications)
- Task Spec: `tasks/functional/EPIC0-FE-003_Dashboard_Calendar_View.md`
- REQ-IDs: REQ-FUNC-001, REQ-FUNC-002

## 🔗 의존성
**선행 작업**: #003 (Layout - AppLayout 필요)
**후행 작업**: #008 (Summary UI), #009 (Billing UI)
**병렬 가능**: #005 (Schedule UI)

## ⚠️ 주의사항
- 캘린더 라이브러리 커스터마이징에 시간을 너무 쏟지 말 것
- 기본 뷰만 나오면 충분

## 🏷️ 라벨
`frontend`, `ui`, `calendar`, `dashboard`, `react`, `priority:must`, `size:M`

