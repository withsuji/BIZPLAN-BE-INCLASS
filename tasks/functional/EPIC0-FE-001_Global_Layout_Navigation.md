# EPIC0-FE-001: FE PoC - 공통 레이아웃 및 내비게이션 구현

## 1. 개요 및 목적
이 Task는 MVP PoC를 위한 React 애플리케이션의 기본 골격(Scaffolding)을 잡고, 전역 레이아웃과 라우팅 구조를 구현하는 것을 목적으로 한다. 사용자가 로그인 후 진입하는 메인 레이아웃(사이드바, 헤더)과 외부 사용자가 접근하는 공개 레이아웃(스케줄링 페이지용)을 분리하여 구현해야 한다.

## 2. 상세 요구사항

### 2.1. 프로젝트 초기화
- Vite + React + TypeScript 기반으로 프로젝트를 생성한다.
- 스타일링을 위해 Tailwind CSS를 설정한다.
- 라우팅을 위해 `react-router-dom`을 설정한다.

### 2.2. 레이아웃 구조
1.  **App Layout (인증된 사용자용)**
    - **Sidebar**: 로고, 내비게이션 메뉴(Dashboard, Calendar, Meetings, Invoices, Settings), 사용자 프로필 요약.
    - **Header**: 현재 페이지 타이틀, 알림 아이콘, 테마 토글(Optional).
    - **Content Area**: 라우팅된 페이지가 렌더링되는 영역.
2.  **Public Layout (외부 사용자용)**
    - 스케줄링 링크 등을 위한 단순한 레이아웃.
    - 브랜드 로고만 상단에 노출되고, 중앙에 콘텐츠가 배치되는 형태.

### 2.3. 내비게이션 메뉴 항목
- `/dashboard`: 대시보드
- `/calendar`: 캘린더 설정 및 뷰
- `/meetings`: 회의 요약 목록
- `/invoices`: 인보이스 및 시간기록
- `/settings`: 설정 (프로필, 연동 등)

## 3. Task Definition (YAML)

```yaml
task_id: "EPIC0-FE-001"
title: "FE PoC - 공통 레이아웃 및 내비게이션 구조 잡기"
summary: >
  Vite/React 프로젝트 초기화 및 라우팅 설정을 수행하고,
  App Layout(Sidebar 포함)과 Public Layout을 구현한다.
type: "functional"

epic: "EPIC_0_FE_PROTOTYPE"
req_ids: ["REQ-FUNC-001", "REQ-FUNC-010", "REQ-FUNC-020"] # 전반적인 UI 접근성
component: ["frontend.core", "frontend.layout"]

context:
  srs_section: "3.2 Client Applications"
  tech_stack: ["React", "Vite", "TypeScript", "Tailwind CSS", "react-router-dom"]

inputs:
  description: "N/A (초기 세팅 작업)"
  preloaded_state:
    - "Node.js 환경이 구성되어 있어야 함"

outputs:
  description: "라우팅이 동작하고 레이아웃이 적용된 React 앱"
  artifacts:
    - "src/layouts/AppLayout.tsx"
    - "src/layouts/PublicLayout.tsx"
    - "src/App.tsx (Router 설정)"

steps_hint:
  - "Vite 프로젝트 생성 및 Tailwind CSS 설치"
  - "react-router-dom 설치 및 기본 라우트 정의 (/dashboard, /calendar 등)"
  - "AppLayout 컴포넌트 구현 (Sidebar에 메뉴 링크 연결)"
  - "PublicLayout 컴포넌트 구현 (심플한 중앙 정렬 레이아웃)"
  - "각 라우트별로 Placeholder 페이지(빈 페이지) 생성하여 연결 확인"

preconditions:
  - "없음"

postconditions:
  - "앱 실행 시 `/dashboard` 등 주요 경로로 진입하면 사이드바가 보여야 한다."
  - "존재하지 않는 경로 접근 시 404 페이지 또는 리다이렉트 처리."

dependencies: []

parallelizable: false
estimated_effort: "S"
priority: "Must"
agent_profile: ["frontend"]

risk_notes:
  - "디자인 시스템이 없으므로, Tailwind 기본 유틸리티 클래스로 깔끔하게만 구성한다."
```









