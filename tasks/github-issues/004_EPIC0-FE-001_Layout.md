---
issue_number: 004
epic: EPIC-0
task_id: EPIC0-FE-001
title: "[FE] 공통 레이아웃 및 내비게이션 구현"
labels: ["frontend", "react", "layout", "priority:must", "size:S"]
assignees: []
milestone: "MVP-Phase-0-Infrastructure"
dependencies: []
parallel_group: "group-0-foundation"
estimated_effort: "S"
difficulty: "하"
estimated_duration: "2일"
status: "completed"
completed_note: "별도 FE 프로젝트에서 완료됨"
skip_github_issue: true
---

# ✅ [완료] Issue #004: 공통 레이아웃 및 내비게이션 구현

> **📢 이 작업은 별도 프론트엔드 프로젝트에서 완료되었습니다.**  
> GitHub Issue 발행 대상에서 제외됩니다.

## 📋 개요
MVP PoC를 위한 React 애플리케이션의 기본 골격(Scaffolding)을 잡고, 전역 레이아웃과 라우팅 구조를 구현한다. 로그인 사용자용 메인 레이아웃과 외부 사용자용 공개 레이아웃을 분리하여 구현한다.

## 🎯 목표
- Vite + React + TypeScript 프로젝트 초기화
- Tailwind CSS 스타일링 설정
- App Layout (Sidebar, Header) 구현
- Public Layout (스케줄링 페이지용) 구현
- React Router 기반 라우팅 구조

---

## 📝 상세 요구사항

### 1. 프로젝트 초기화

#### 기술 스택
- **React**: 18.x
- **Vite**: 5.x (빌드 도구)
- **TypeScript**: 5.x
- **Tailwind CSS**: 3.x
- **React Router**: 6.x

#### 폴더 구조
```
src/
├── components/
│   ├── common/
│   │   ├── Button.tsx
│   │   ├── Card.tsx
│   │   └── Modal.tsx
│   ├── layout/
│   │   ├── AppLayout.tsx
│   │   ├── PublicLayout.tsx
│   │   ├── Sidebar.tsx
│   │   └── Header.tsx
│   └── ...
├── pages/
│   ├── DashboardPage.tsx
│   ├── CalendarPage.tsx
│   ├── MeetingsPage.tsx
│   ├── InvoicesPage.tsx
│   ├── SettingsPage.tsx
│   └── SchedulePage.tsx (Public)
├── hooks/
├── contexts/
├── utils/
├── types/
├── App.tsx
└── main.tsx
```

### 2. 레이아웃 구조

#### App Layout (인증된 사용자용)

```
┌──────────────────────────────────────────────┐
│  Header (현재 페이지, 알림, 프로필)           │
├─────────────┬────────────────────────────────┤
│             │                                │
│   Sidebar   │        Content Area            │
│   - Logo    │     (라우팅된 페이지)           │
│   - 메뉴    │                                │
│   - 프로필  │                                │
│             │                                │
└─────────────┴────────────────────────────────┘
```

**Sidebar 메뉴 항목**:
- Dashboard (`/dashboard`)
- Calendar (`/calendar`)
- Meetings (`/meetings`)
- Invoices (`/invoices`)
- Settings (`/settings`)

#### Public Layout (외부 사용자용)

```
┌──────────────────────────────────────────────┐
│              Logo (브랜드)                    │
├──────────────────────────────────────────────┤
│                                              │
│            Content (중앙 정렬)                │
│                                              │
└──────────────────────────────────────────────┘
```

### 3. 라우팅 구조

```tsx
// App.tsx
<Routes>
  {/* Public Routes */}
  <Route element={<PublicLayout />}>
    <Route path="/schedule/:linkId" element={<SchedulePage />} />
    <Route path="/schedule/success" element={<ScheduleSuccessPage />} />
  </Route>

  {/* Protected Routes */}
  <Route element={<AppLayout />}>
    <Route path="/" element={<Navigate to="/dashboard" />} />
    <Route path="/dashboard" element={<DashboardPage />} />
    <Route path="/calendar" element={<CalendarPage />} />
    <Route path="/meetings" element={<MeetingsPage />} />
    <Route path="/meetings/:id" element={<MeetingDetailPage />} />
    <Route path="/invoices" element={<InvoicesPage />} />
    <Route path="/settings" element={<SettingsPage />} />
  </Route>

  {/* 404 */}
  <Route path="*" element={<NotFoundPage />} />
</Routes>
```

---

## 🔧 구현 가이드

### Phase 1: 프로젝트 초기화 (Day 1)

```bash
# Vite 프로젝트 생성
npm create vite@latest bizplan-fe -- --template react-ts
cd bizplan-fe

# 의존성 설치
npm install react-router-dom
npm install -D tailwindcss postcss autoprefixer
npx tailwindcss init -p

# Tailwind 설정
```

#### tailwind.config.js
```javascript
/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        primary: {
          50: '#eff6ff',
          500: '#3b82f6',
          600: '#2563eb',
          700: '#1d4ed8',
        },
        secondary: {
          500: '#8b5cf6',
        },
      },
    },
  },
  plugins: [],
}
```

### Phase 2: 레이아웃 컴포넌트 구현 (Day 1-2)

#### Sidebar.tsx
```tsx
import { NavLink } from 'react-router-dom';
import { 
  HomeIcon, 
  CalendarIcon, 
  UsersIcon, 
  DocumentTextIcon, 
  CogIcon 
} from '@heroicons/react/24/outline';

const navigation = [
  { name: 'Dashboard', href: '/dashboard', icon: HomeIcon },
  { name: 'Calendar', href: '/calendar', icon: CalendarIcon },
  { name: 'Meetings', href: '/meetings', icon: UsersIcon },
  { name: 'Invoices', href: '/invoices', icon: DocumentTextIcon },
  { name: 'Settings', href: '/settings', icon: CogIcon },
];

export function Sidebar() {
  return (
    <aside className="w-64 bg-gray-900 text-white min-h-screen p-4">
      <div className="mb-8">
        <h1 className="text-xl font-bold">BizPlan</h1>
      </div>
      <nav className="space-y-1">
        {navigation.map((item) => (
          <NavLink
            key={item.name}
            to={item.href}
            className={({ isActive }) =>
              `flex items-center px-4 py-2 rounded-lg ${
                isActive ? 'bg-primary-600' : 'hover:bg-gray-800'
              }`
            }
          >
            <item.icon className="w-5 h-5 mr-3" />
            {item.name}
          </NavLink>
        ))}
      </nav>
    </aside>
  );
}
```

#### AppLayout.tsx
```tsx
import { Outlet } from 'react-router-dom';
import { Sidebar } from './Sidebar';
import { Header } from './Header';

export function AppLayout() {
  return (
    <div className="flex min-h-screen bg-gray-100">
      <Sidebar />
      <div className="flex-1 flex flex-col">
        <Header />
        <main className="flex-1 p-6">
          <Outlet />
        </main>
      </div>
    </div>
  );
}
```

### Phase 3: 라우터 및 Placeholder 페이지 (Day 2)
- [ ] App.tsx에 라우터 설정
- [ ] 각 페이지 Placeholder 컴포넌트 생성
- [ ] 404 페이지 구현

---

## 📁 산출물 (Artifacts)

```
bizplan-fe/
├── src/
│   ├── components/
│   │   └── layout/
│   │       ├── AppLayout.tsx
│   │       ├── PublicLayout.tsx
│   │       ├── Sidebar.tsx
│   │       └── Header.tsx
│   ├── pages/
│   │   ├── DashboardPage.tsx
│   │   ├── CalendarPage.tsx
│   │   ├── MeetingsPage.tsx
│   │   ├── InvoicesPage.tsx
│   │   ├── SettingsPage.tsx
│   │   ├── SchedulePage.tsx
│   │   └── NotFoundPage.tsx
│   ├── App.tsx
│   └── main.tsx
├── tailwind.config.js
├── vite.config.ts
├── tsconfig.json
└── package.json
```

---

## ✅ 완료 조건 (Acceptance Criteria)

| # | 조건 | 검증 방법 |
|---|------|----------|
| AC-1 | 앱 정상 실행 | `npm run dev` 실행 후 `localhost:5173` 접속 |
| AC-2 | Sidebar 렌더링 | 메인 레이아웃에서 왼쪽 사이드바 표시 |
| AC-3 | 내비게이션 동작 | 메뉴 클릭 시 URL 변경 및 페이지 전환 |
| AC-4 | Active 상태 표시 | 현재 페이지에 해당하는 메뉴 하이라이트 |
| AC-5 | Public Layout 분리 | `/schedule/:id` 경로에서 별도 레이아웃 적용 |
| AC-6 | 404 페이지 | 존재하지 않는 경로 접근 시 404 페이지 표시 |
| AC-7 | 반응형 기본 구조 | 모바일 크기에서도 깨지지 않음 |

---

## 🔗 의존성

**선행 작업**:
- 없음 (프로젝트 시작점)

**후행 작업**:
- #005 (Schedule UI) - Layout 위에 구현
- #006 (Dashboard UI) - Layout 위에 구현

**병렬 가능**:
- #001 (AWS 인프라) - 완전 독립적으로 병렬 진행 가능

---

## 🏷️ 라벨
`frontend`, `react`, `layout`, `priority:must`, `size:S`, `difficulty:easy`

---

## ⚠️ 주의사항 및 리스크

| 리스크 | 영향 | 완화 방안 |
|--------|------|----------|
| 디자인 시스템 부재 | UI 일관성 저하 | Tailwind 기본 스타일로 깔끔하게 구성 |
| 상태 관리 미결정 | 컴포넌트 간 데이터 공유 어려움 | 우선 React Context 사용, 필요시 Zustand 도입 |

---

## 📚 참고 문서
- Task Spec: `tasks/functional/EPIC0-FE-001_Global_Layout_Navigation.md`
- REQ-IDs: REQ-FUNC-001, REQ-FUNC-010, REQ-FUNC-020
- [React Router 문서](https://reactrouter.com/)
- [Tailwind CSS 문서](https://tailwindcss.com/docs)



