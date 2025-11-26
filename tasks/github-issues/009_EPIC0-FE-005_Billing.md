---
issue_number: 009
epic: EPIC-0
task_id: EPIC0-FE-005
title: "[FE] 시간 기록 및 인보이스 관리 UI"
labels: ["frontend", "ui", "billing", "invoice", "react", "priority:must", "status:completed"]
assignees: []
milestone: "MVP-Phase-0-FE-PoC"
dependencies: ["#006"]
parallel_group: "group-2-fe-detail"
estimated_effort: "M"
status: "✅ COMPLETED"
completed_date: "2025-11-26"
note: "별도 프로젝트에서 완수됨. GitHub Issue 생성 불필요."
---

# ~~Issue #009: 시간 기록 및 인보이스 관리 UI~~ ✅ **완료됨**

> ⚠️ **본 이슈는 별도 FE 프로젝트에서 이미 완료되었습니다. GitHub Issue 생성이 필요하지 않습니다.**

## 📋 개요
시간 기록 목록과 인보이스 상태를 관리하는 테이블 기반 UI를 구현한다.

## 🎯 목표
- 시간 기록 및 인보이스 조회
- 상태 변경 액션
- Mock 데이터로 플로우 검증

## 📝 상세 작업 내역

### 1. 화면 구성
- [ ] InvoicePage 컴포넌트 생성 (`/invoices`)
- [ ] Tab UI: "Time Entries" / "Invoices"

### 2. Time Entries Tab
- [ ] TimeEntryTable 컴포넌트 구현
- [ ] 데이터 테이블: 날짜, 프로젝트, 작업 내용, 소요 시간, Billable
- [ ] 청구 가능 여부 토글 스위치
- [ ] "Create Invoice" 버튼

### 3. Invoices Tab
- [ ] InvoiceTable 컴포넌트 구현
- [ ] 인보이스 리스트: ID, 클라이언트, 금액, 상태, 기한
- [ ] 상태별 배지 스타일링 (Paid=Green, Overdue=Red)
- [ ] "Send Now" 버튼 (Draft 상태)

### 4. 상호작용
- [ ] Time Entry 클릭 시 수정 모달
- [ ] "Create Invoice" 클릭 시 Invoices 탭으로 이동 및 새 행 추가 (Mock)
- [ ] "Send Now" 클릭 시 상태 변경 및 알림 토스트
- [ ] Mock 데이터 정의

## ✅ 완료 조건
- [ ] 사용자가 시간 기록을 수정할 수 있음 (Mock)
- [ ] 인보이스 발송 시 상태 변화가 UI에 즉시 반영
- [ ] 테이블이 깔끔하게 렌더링

## 📚 참고 문서
- SRS: `docs/GPT-SRS-v02.md` (Section 3.3 Billing/Dunning Service)
- Task Spec: `tasks/functional/EPIC0-FE-005_Billing_Invoice_View.md`
- REQ-IDs: REQ-FUNC-020, REQ-FUNC-021

## 🔗 의존성
**선행 작업**: #006 (Dashboard - 메뉴에서 진입)
**후행 작업**: #017 (Invoice Generation - 실제 API 연동 시)
**병렬 가능**: #008 (Summary UI)

## ⚠️ 주의사항
- TanStack Table 등 활용 또는 단순 Table
- 데이터 그리드 라이브러리 선택 시 가벼운 것 우선

## 🏷️ 라벨
`frontend`, `ui`, `billing`, `invoice`, `react`, `priority:must`, `size:M`

