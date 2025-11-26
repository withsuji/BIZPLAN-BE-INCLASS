---
issue_number: 005
epic: EPIC-0
task_id: EPIC0-FE-002
title: "[FE] 스케줄링 링크 및 예약 페이지 UI"
labels: ["frontend", "ui", "scheduling", "react", "priority:must", "status:completed"]
assignees: []
milestone: "MVP-Phase-0-FE-PoC"
dependencies: ["#003"]
parallel_group: "group-1-fe-poc"
estimated_effort: "M"
status: "✅ COMPLETED"
completed_date: "2025-11-26"
note: "별도 프로젝트에서 완수됨. GitHub Issue 생성 불필요."
---

# ~~Issue #005: 스케줄링 링크 및 예약 페이지 UI~~ ✅ **완료됨**

> ⚠️ **본 이슈는 별도 FE 프로젝트에서 이미 완료되었습니다. GitHub Issue 생성이 필요하지 않습니다.**

## 📋 개요
외부 사용자가 링크를 통해 미팅 가능한 시간을 선택하고 예약을 확정하는 Public View를 구현한다.

## 🎯 목표
- 외부 사용자용 예약 페이지 구현
- 타임존 선택 및 슬롯 표시
- Mock 데이터로 예약 플로우 검증

## 📝 상세 작업 내역

### 1. 화면 구성
- [ ] SchedulePage 컴포넌트 생성 (`/schedule/:linkId`)
- [ ] 좌측 패널: 주최자 프로필, 미팅 정보
- [ ] 우측 패널: 캘린더 뷰, 슬롯 선택

### 2. 타임존 및 슬롯
- [ ] TimezoneSelect 컴포넌트 구현
- [ ] 날짜 선택기 구현 (`react-calendar` 또는 직접 구현)
- [ ] SlotPicker 컴포넌트 (가용 슬롯 리스트)
- [ ] Mock 데이터 정의 (날짜별 가용 슬롯)

### 3. 예약 폼
- [ ] 예약 폼 모달/인라인 구현
- [ ] 입력 필드: 이름, 이메일, 메모
- [ ] "예약 확정" 버튼
- [ ] 성공 페이지 (`/schedule/success`)

### 4. 상호작용
- [ ] 타임존 변경 시 슬롯 시간 변환 (Mock)
- [ ] 날짜 변경 시 슬롯 갱신
- [ ] 예약 확정 후 성공 메시지

## ✅ 완료 조건
- [ ] URL로 진입 시 예약 페이지 렌더링
- [ ] 날짜 클릭 시 슬롯 목록 변경
- [ ] 예약 확정 시 성공 메시지 표시

## 📚 참고 문서
- SRS: `docs/GPT-SRS-v02.md` (Section 3.4 Interaction Sequences)
- Task Spec: `tasks/functional/EPIC0-FE-002_Scheduling_Link_Page.md`
- REQ-IDs: REQ-FUNC-001, REQ-FUNC-002, REQ-FUNC-003

## 🔗 의존성
**선행 작업**: #003 (Layout - PublicLayout 필요)
**후행 작업**: #015 (Slot Algorithm - 실제 API 연동 시)
**병렬 가능**: #006 (Dashboard UI)

## ⚠️ 주의사항
- 실제 타임존 계산 로직은 복잡하므로 PoC에서는 Mock 데이터로 처리
- 특정 타임존(Asia/Seoul) 기준 가정

## 🏷️ 라벨
`frontend`, `ui`, `scheduling`, `react`, `priority:must`, `size:M`

