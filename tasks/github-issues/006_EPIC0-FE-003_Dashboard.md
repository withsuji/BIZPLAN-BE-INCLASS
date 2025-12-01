---
issue_number: 006
epic: EPIC-0
task_id: EPIC0-FE-003
title: "[FE] 대시보드 및 캘린더 메인 뷰 UI"
labels: ["frontend", "react", "calendar", "dashboard", "priority:must", "size:M"]
assignees: []
milestone: "MVP-Phase-1-Core-Features"
dependencies: ["#004"]
parallel_group: "group-1-core-setup"
estimated_effort: "M"
difficulty: "중"
estimated_duration: "3일"
status: "completed"
completed_note: "별도 FE 프로젝트에서 완료됨"
skip_github_issue: true
---

# ✅ [완료] Issue #006: 대시보드 및 캘린더 메인 뷰 UI

> **📢 이 작업은 별도 프론트엔드 프로젝트에서 완료되었습니다.**  
> GitHub Issue 발행 대상에서 제외됩니다.

## 📋 개요
내부 사용자(로그인한 유저)가 자신의 일정을 확인하고 관리하는 메인 대시보드를 구현한다. 주간/월간 캘린더 뷰를 제공하며, 일반 일정과 "포커스 블록(Focus Block)"이 시각적으로 구분되어야 한다.

## 🎯 목표
- 주간 캘린더 뷰 구현
- 일반 일정 vs 포커스 블록 시각적 구분
- 오늘의 요약 사이드 패널
- 일정 상세 모달

---

## 📝 상세 요구사항

### 1. 화면 구성

**URL**: `/calendar` (또는 `/dashboard`)

#### 화면 레이아웃
```
┌──────────────────────────────────────────────────────────────┐
│  Header: 오늘 날짜 | ◀ 이전 주 | 다음 주 ▶ | 주간/월간 토글  │
├────────────────────────────────────────────┬─────────────────┤
│                                            │                 │
│           주간 캘린더 뷰                    │   사이드 패널    │
│     (7일 x 24시간 그리드)                   │   - 오늘의 요약  │
│                                            │   - 다음 미팅    │
│     ┌───┬───┬───┬───┬───┬───┬───┐         │   - 포커스 시간  │
│     │월 │화 │수 │목 │금 │토 │일 │         │                 │
│     ├───┼───┼───┼───┼───┼───┼───┤         │                 │
│     │   │▓▓▓│   │▓▓▓│   │   │   │ ← 포커스 │                 │
│     │▒▒▒│   │▒▒▒│   │▒▒▒│   │   │ ← 미팅   │                 │
│     │   │   │   │   │   │   │   │         │                 │
│     └───┴───┴───┴───┴───┴───┴───┘         │                 │
│                                            │                 │
└────────────────────────────────────────────┴─────────────────┘
```

### 2. 이벤트 타입별 스타일링

| 타입 | 색상 | 라벨 |
|------|------|------|
| 일반 미팅 | 파란색 (`bg-blue-500`) | 제목 표시 |
| 포커스 블록 | 보라색 (`bg-purple-500`) | "🎯 Focus Time" |
| 충돌/경고 | 빨간 테두리 | 경고 아이콘 |

### 3. 사이드 패널 정보

- **오늘의 요약**
  - 예정된 미팅 수
  - 확보된 포커스 시간 합계
- **다음 미팅 카드**
  - 미팅 제목, 시간
  - "요약 보기" 버튼 (완료된 경우)
- **포커스 시간 확보 버튼**
  - 클릭 시 자동 슬롯 생성 애니메이션 (Mock)

### 4. Mock 데이터

```typescript
interface CalendarEvent {
  id: string;
  title: string;
  start: Date;
  end: Date;
  type: 'meeting' | 'focus';
  status?: 'confirmed' | 'tentative' | 'conflict';
  attendees?: string[];
}

const mockEvents: CalendarEvent[] = [
  {
    id: '1',
    title: 'Team Sync',
    start: new Date('2025-12-01T10:00:00'),
    end: new Date('2025-12-01T11:00:00'),
    type: 'meeting',
    status: 'confirmed',
    attendees: ['김철수', '이영희'],
  },
  {
    id: '2',
    title: 'Focus Time',
    start: new Date('2025-12-01T14:00:00'),
    end: new Date('2025-12-01T16:00:00'),
    type: 'focus',
  },
  {
    id: '3',
    title: 'Client Call',
    start: new Date('2025-12-02T09:00:00'),
    end: new Date('2025-12-02T10:00:00'),
    type: 'meeting',
    status: 'conflict', // 충돌 상태
  },
];
```

---

## 🔧 구현 가이드

### Phase 1: 캘린더 라이브러리 설정 (Day 1)

#### 의존성 설치
```bash
npm install react-big-calendar date-fns
npm install -D @types/react-big-calendar
```

#### CalendarPage.tsx
```tsx
import { useState, useMemo } from 'react';
import { Calendar, dateFnsLocalizer, Views } from 'react-big-calendar';
import { format, parse, startOfWeek, getDay, addWeeks, subWeeks } from 'date-fns';
import { ko } from 'date-fns/locale';
import 'react-big-calendar/lib/css/react-big-calendar.css';

const localizer = dateFnsLocalizer({
  format,
  parse,
  startOfWeek: () => startOfWeek(new Date(), { weekStartsOn: 1 }),
  getDay,
  locales: { ko },
});

export function CalendarPage() {
  const [currentDate, setCurrentDate] = useState(new Date());
  const [selectedEvent, setSelectedEvent] = useState<CalendarEvent | null>(null);

  const eventStyleGetter = (event: CalendarEvent) => {
    let style: React.CSSProperties = {
      borderRadius: '4px',
      opacity: 0.9,
      border: 'none',
      display: 'block',
    };

    if (event.type === 'focus') {
      style.backgroundColor = '#8b5cf6'; // purple
    } else if (event.status === 'conflict') {
      style.backgroundColor = '#3b82f6';
      style.border = '2px solid #ef4444'; // red border
    } else {
      style.backgroundColor = '#3b82f6'; // blue
    }

    return { style };
  };

  return (
    <div className="flex gap-6">
      {/* Main Calendar */}
      <div className="flex-1">
        <div className="flex items-center justify-between mb-4">
          <h1 className="text-2xl font-bold">캘린더</h1>
          <div className="flex items-center gap-2">
            <button onClick={() => setCurrentDate(subWeeks(currentDate, 1))}>
              ◀ 이전
            </button>
            <button onClick={() => setCurrentDate(new Date())}>
              오늘
            </button>
            <button onClick={() => setCurrentDate(addWeeks(currentDate, 1))}>
              다음 ▶
            </button>
          </div>
        </div>

        <Calendar
          localizer={localizer}
          events={mockEvents}
          startAccessor="start"
          endAccessor="end"
          style={{ height: 700 }}
          view={Views.WEEK}
          date={currentDate}
          onNavigate={setCurrentDate}
          onSelectEvent={setSelectedEvent}
          eventPropGetter={eventStyleGetter}
          messages={{
            today: '오늘',
            previous: '이전',
            next: '다음',
            week: '주',
            day: '일',
          }}
        />
      </div>

      {/* Side Panel */}
      <SidePanel events={mockEvents} />

      {/* Event Detail Modal */}
      {selectedEvent && (
        <EventDetailModal 
          event={selectedEvent} 
          onClose={() => setSelectedEvent(null)} 
        />
      )}
    </div>
  );
}
```

### Phase 2: 사이드 패널 구현 (Day 2)

#### SidePanel.tsx
```tsx
interface SidePanelProps {
  events: CalendarEvent[];
}

export function SidePanel({ events }: SidePanelProps) {
  const todayEvents = events.filter(e => 
    isToday(e.start)
  );
  
  const meetingCount = todayEvents.filter(e => e.type === 'meeting').length;
  const focusMinutes = todayEvents
    .filter(e => e.type === 'focus')
    .reduce((acc, e) => acc + differenceInMinutes(e.end, e.start), 0);

  const nextMeeting = events
    .filter(e => e.type === 'meeting' && e.start > new Date())
    .sort((a, b) => a.start.getTime() - b.start.getTime())[0];

  return (
    <div className="w-80 space-y-4">
      {/* Today Summary */}
      <div className="bg-white rounded-lg shadow p-4">
        <h3 className="font-semibold mb-3">📊 오늘의 요약</h3>
        <div className="grid grid-cols-2 gap-4">
          <div className="text-center p-3 bg-blue-50 rounded">
            <div className="text-2xl font-bold text-blue-600">{meetingCount}</div>
            <div className="text-sm text-gray-500">미팅</div>
          </div>
          <div className="text-center p-3 bg-purple-50 rounded">
            <div className="text-2xl font-bold text-purple-600">
              {Math.floor(focusMinutes / 60)}h {focusMinutes % 60}m
            </div>
            <div className="text-sm text-gray-500">포커스</div>
          </div>
        </div>
      </div>

      {/* Next Meeting */}
      {nextMeeting && (
        <div className="bg-white rounded-lg shadow p-4">
          <h3 className="font-semibold mb-3">⏰ 다음 미팅</h3>
          <div className="border-l-4 border-blue-500 pl-3">
            <p className="font-medium">{nextMeeting.title}</p>
            <p className="text-sm text-gray-500">
              {format(nextMeeting.start, 'HH:mm')} - {format(nextMeeting.end, 'HH:mm')}
            </p>
          </div>
        </div>
      )}

      {/* Quick Actions */}
      <div className="bg-white rounded-lg shadow p-4">
        <h3 className="font-semibold mb-3">⚡ 빠른 실행</h3>
        <button className="w-full py-2 bg-purple-600 text-white rounded-lg hover:bg-purple-700">
          🎯 포커스 시간 확보하기
        </button>
      </div>
    </div>
  );
}
```

### Phase 3: 이벤트 모달 및 인터랙션 (Day 3)

#### EventDetailModal.tsx
```tsx
interface EventDetailModalProps {
  event: CalendarEvent;
  onClose: () => void;
}

export function EventDetailModal({ event, onClose }: EventDetailModalProps) {
  return (
    <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50">
      <div className="bg-white rounded-xl shadow-xl w-full max-w-md p-6">
        <div className="flex items-center justify-between mb-4">
          <h2 className="text-xl font-bold">{event.title}</h2>
          <button onClick={onClose} className="text-gray-400 hover:text-gray-600">
            ✕
          </button>
        </div>

        <div className="space-y-3">
          <div className="flex items-center gap-2">
            <span className="text-gray-500">📅</span>
            <span>{format(event.start, 'yyyy년 M월 d일 (E)', { locale: ko })}</span>
          </div>
          <div className="flex items-center gap-2">
            <span className="text-gray-500">⏰</span>
            <span>
              {format(event.start, 'HH:mm')} - {format(event.end, 'HH:mm')}
            </span>
          </div>
          {event.attendees && (
            <div className="flex items-center gap-2">
              <span className="text-gray-500">👥</span>
              <span>{event.attendees.join(', ')}</span>
            </div>
          )}
        </div>

        <div className="mt-6 flex gap-2">
          {event.type === 'meeting' && (
            <button className="flex-1 py-2 bg-blue-600 text-white rounded-lg">
              요약 보기
            </button>
          )}
          <button className="flex-1 py-2 border rounded-lg">
            수정
          </button>
        </div>
      </div>
    </div>
  );
}
```

---

## 📁 산출물 (Artifacts)

```
src/
├── pages/
│   └── CalendarPage.tsx
├── components/
│   └── calendar/
│       ├── SidePanel.tsx
│       ├── EventDetailModal.tsx
│       ├── EventCard.tsx
│       └── CreateEventModal.tsx
└── mocks/
    └── calendarData.ts
```

---

## ✅ 완료 조건 (Acceptance Criteria)

| # | 조건 | 검증 방법 |
|---|------|----------|
| AC-1 | 캘린더 뷰 렌더링 | `/calendar` 접속 시 주간 뷰 표시 |
| AC-2 | 이벤트 색상 구분 | 미팅=파랑, 포커스=보라 |
| AC-3 | 주 네비게이션 | 이전/다음 주 버튼 동작 |
| AC-4 | 이벤트 클릭 | 클릭 시 상세 모달 표시 |
| AC-5 | 사이드 패널 | 오늘의 요약, 다음 미팅 정보 표시 |
| AC-6 | 충돌 표시 | 충돌 이벤트에 빨간 테두리 |
| AC-7 | 빈 슬롯 클릭 | 새 일정 생성 모달 표시 (Mock) |

---

## 🔗 의존성

**선행 작업**:
- #004 (공통 레이아웃) - AppLayout 사용

**후행 작업**:
- #008 (Summary UI) - 미팅 요약 연결
- #009 (Billing UI) - 시간 기록 연결

**병렬 가능**:
- #005 (Schedule UI) - 동일 레벨

---

## 🏷️ 라벨
`frontend`, `react`, `calendar`, `dashboard`, `priority:must`, `size:M`, `difficulty:medium`

---

## 📚 참고 문서
- Task Spec: `tasks/functional/EPIC0-FE-003_Dashboard_Calendar_View.md`
- REQ-IDs: REQ-FUNC-001, REQ-FUNC-002
- [react-big-calendar 문서](https://github.com/jquense/react-big-calendar)
