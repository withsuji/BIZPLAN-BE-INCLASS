---
issue_number: 003
epic: EPIC-0
task_id: EPIC0-FE-001
title: "[FE] 공통 레이아웃 및 내비게이션 구현"
labels: ["frontend", "ui", "layout", "react", "priority:must", "status:completed"]
assignees: []
milestone: "MVP-Phase-0-FE-PoC"
dependencies: []
parallel_group: "group-0-foundation"
estimated_effort: "S"
status: "✅ COMPLETED"
completed_date: "2025-11-26"
note: "별도 프로젝트에서 완수됨. GitHub Issue 생성 불필요."
---

# ~~Issue #003: 공통 레이아웃 및 내비게이션 구현~~ ✅ **완료됨**

> ⚠️ **본 이슈는 별도 FE 프로젝트에서 이미 완료되었습니다. GitHub Issue 생성이 필요하지 않습니다.**

## 📋 개요
Vite + React + TypeScript 기반 프로젝트를 초기화하고, 전역 레이아웃과 라우팅 구조를 구현한다.

## 🎯 목표
- React 앱의 기본 골격 구축
- 사용자 레이아웃과 공개 레이아웃 분리
- 라우팅 및 내비게이션 설정

## 📝 상세 작업 내역

### 1. 프로젝트 초기화
- [ ] Vite + React + TypeScript 프로젝트 생성
- [ ] Tailwind CSS 설치 및 설정
- [ ] react-router-dom 설치
- [ ] 기본 디렉토리 구조 설정

### 2. 레이아웃 컴포넌트
- [ ] AppLayout 구현 (Sidebar, Header, Content Area)
- [ ] Sidebar 구현 (로고, 내비게이션 메뉴, 사용자 프로필)
- [ ] Header 구현 (페이지 타이틀, 알림, 테마 토글)
- [ ] PublicLayout 구현 (단순 중앙 정렬)

### 3. 라우팅 설정
- [ ] Router 설정 (`App.tsx`)
- [ ] 기본 라우트 정의:
  - `/dashboard` - 대시보드
  - `/calendar` - 캘린더
  - `/meetings` - 회의 요약 목록
  - `/invoices` - 인보이스 관리
  - `/settings` - 설정
  - `/schedule/:linkId` - 스케줄링 링크 (Public)
- [ ] Placeholder 페이지 생성 (각 라우트별)
- [ ] 404 페이지 또는 리다이렉트 처리

### 4. 내비게이션 메뉴
- [ ] 메뉴 항목 컴포넌트 구현
- [ ] 활성 메뉴 하이라이트
- [ ] 반응형 레이아웃 (모바일 대응)

## ✅ 완료 조건
- [ ] 앱 실행 시 `/dashboard` 등 주요 경로로 진입하면 사이드바 표시
- [ ] 메뉴 클릭 시 해당 페이지로 이동
- [ ] 존재하지 않는 경로 접근 시 404 처리
- [ ] Public Layout과 App Layout이 올바르게 분리

## 📚 참고 문서
- SRS: `docs/GPT-SRS-v02.md` (Section 3.2 Client Applications)
- Task Spec: `tasks/functional/EPIC0-FE-001_Global_Layout_Navigation.md`
- REQ-IDs: REQ-FUNC-001, REQ-FUNC-010, REQ-FUNC-020

## 🔗 의존성
**선행 작업**: 없음 (프로젝트 시작점)
**후행 작업**: #005 (Schedule UI), #006 (Dashboard UI)
**병렬 가능**: #001 (인프라), #002 (OAuth)

## ⚠️ 주의사항
- 디자인 시스템이 없으므로 Tailwind 기본 유틸리티 클래스로 깔끔하게 구성
- 이모지는 명시적 요청이 없는 한 사용하지 않음
- Node.js 환경이 구성되어 있어야 함

## 🏷️ 라벨
`frontend`, `ui`, `layout`, `react`, `vite`, `priority:must`, `size:S`

