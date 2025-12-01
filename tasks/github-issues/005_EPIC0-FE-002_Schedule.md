---
issue_number: 005
epic: EPIC-0
task_id: EPIC0-FE-002
title: "[FE] 스케줄링 예약 페이지 UI"
labels: ["frontend", "react", "scheduling", "priority:must", "size:M"]
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

# ✅ [완료] Issue #005: 스케줄링 예약 페이지 UI

> **📢 이 작업은 별도 프론트엔드 프로젝트에서 완료되었습니다.**  
> GitHub Issue 발행 대상에서 제외됩니다.

## 📋 개요
외부 사용자(클라이언트, 파트너)가 링크를 통해 접속하여 미팅 가능한 시간을 선택하고 예약을 확정하는 화면(Public View)을 구현한다. 타임존 자동 변환과 대체 슬롯 제안 기능을 UI상에서 시뮬레이션한다.

## 🎯 목표
- 외부 사용자용 예약 페이지 구현
- 날짜/시간 슬롯 선택 UI
- 타임존 선택 기능
- 예약 폼 및 확인 페이지

---

## 📝 상세 요구사항

### 1. 화면 구성

**URL**: `/schedule/:linkId` (예: `/schedule/suji-meeting`)

#### 화면 레이아웃
```
┌────────────────┬─────────────────────────────┐
│                │                             │
│  주최자 정보   │      달력 / 슬롯 선택        │
│  - 프로필      │      - 월 달력 뷰            │
│  - 미팅 제목   │      - 가용 슬롯 리스트       │
│  - 소요 시간   │      - 타임존 선택기          │
│                │                             │
└────────────────┴─────────────────────────────┘
```

### 2. 좌측 패널 (Host Info)
- 주최자 프로필 이미지
- 주최자 이름/직함
- 미팅 제목 (예: "30분 커피챗")
- 소요 시간
- 미팅 설명

### 3. 우측 패널 (Calendar/Slots)
- **월 달력**: 날짜 선택 가능한 미니 캘린더
- **슬롯 리스트**: 선택한 날짜의 가용 시간대
- **타임존 드롭다운**: 사용자 타임존 변경 가능

### 4. 예약 폼
슬롯 선택 시 표시:
- 이름 (필수)
- 이메일 (필수)
- 메모 (선택)
- "예약 확정" 버튼

### 5. Mock 데이터

```typescript
// Mock Host Data
const hostInfo = {
  name: "김수지",
  title: "Product Manager",
  profileImage: "/avatar.jpg",
  meetingTitle: "30분 커피챗",
  duration: 30,
  description: "편하게 이야기 나눠요!"
};

// Mock Slots Data
const availableSlots = {
  "2025-12-01": ["10:00", "14:00", "16:30"],
  "2025-12-02": ["11:00", "13:00", "15:00"],
  "2025-12-03": [],
  "2025-12-04": ["09:00", "10:30", "14:00", "16:00"],
};

// Timezones
const timezones = [
  { value: "Asia/Seoul", label: "서울 (GMT+9)" },
  { value: "America/New_York", label: "뉴욕 (GMT-5)" },
  { value: "Europe/London", label: "런던 (GMT+0)" },
];
```

---

## 🔧 구현 가이드

### Phase 1: 페이지 구조 및 라우팅 (Day 1)

#### SchedulePage.tsx
```tsx
import { useState } from 'react';
import { useParams } from 'react-router-dom';
import { HostInfo } from '@/components/schedule/HostInfo';
import { CalendarPicker } from '@/components/schedule/CalendarPicker';
import { SlotList } from '@/components/schedule/SlotList';
import { BookingForm } from '@/components/schedule/BookingForm';

export function SchedulePage() {
  const { linkId } = useParams();
  const [selectedDate, setSelectedDate] = useState<Date | null>(null);
  const [selectedSlot, setSelectedSlot] = useState<string | null>(null);
  const [timezone, setTimezone] = useState('Asia/Seoul');

  return (
    <div className="min-h-screen bg-gray-50 py-8">
      <div className="max-w-4xl mx-auto">
        <div className="bg-white rounded-xl shadow-lg overflow-hidden">
          <div className="grid grid-cols-1 md:grid-cols-3">
            {/* Left Panel - Host Info */}
            <div className="p-6 bg-gray-900 text-white">
              <HostInfo />
            </div>

            {/* Right Panel - Calendar & Slots */}
            <div className="col-span-2 p-6">
              {!selectedSlot ? (
                <>
                  <TimezoneSelect 
                    value={timezone} 
                    onChange={setTimezone} 
                  />
                  <CalendarPicker 
                    selectedDate={selectedDate}
                    onDateSelect={setSelectedDate}
                  />
                  {selectedDate && (
                    <SlotList 
                      date={selectedDate}
                      timezone={timezone}
                      onSlotSelect={setSelectedSlot}
                    />
                  )}
                </>
              ) : (
                <BookingForm 
                  date={selectedDate!}
                  slot={selectedSlot}
                  timezone={timezone}
                  onBack={() => setSelectedSlot(null)}
                />
              )}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
```

### Phase 2: 컴포넌트 구현 (Day 1-2)

#### CalendarPicker.tsx
```tsx
import { useState } from 'react';
import { 
  format, 
  startOfMonth, 
  endOfMonth, 
  eachDayOfInterval,
  isSameDay,
  isToday,
  addMonths,
  subMonths 
} from 'date-fns';
import { ko } from 'date-fns/locale';

interface CalendarPickerProps {
  selectedDate: Date | null;
  onDateSelect: (date: Date) => void;
  availableDates: string[]; // ['2025-12-01', '2025-12-02', ...]
}

export function CalendarPicker({ selectedDate, onDateSelect, availableDates }: CalendarPickerProps) {
  const [currentMonth, setCurrentMonth] = useState(new Date());
  
  const days = eachDayOfInterval({
    start: startOfMonth(currentMonth),
    end: endOfMonth(currentMonth),
  });

  const isAvailable = (date: Date) => {
    const dateStr = format(date, 'yyyy-MM-dd');
    return availableDates.includes(dateStr);
  };

  return (
    <div className="mb-6">
      {/* Month Navigation */}
      <div className="flex items-center justify-between mb-4">
        <button onClick={() => setCurrentMonth(subMonths(currentMonth, 1))}>
          ←
        </button>
        <h3 className="font-semibold">
          {format(currentMonth, 'yyyy년 M월', { locale: ko })}
        </h3>
        <button onClick={() => setCurrentMonth(addMonths(currentMonth, 1))}>
          →
        </button>
      </div>

      {/* Calendar Grid */}
      <div className="grid grid-cols-7 gap-1">
        {['일', '월', '화', '수', '목', '금', '토'].map((day) => (
          <div key={day} className="text-center text-sm text-gray-500 py-2">
            {day}
          </div>
        ))}
        {days.map((day) => (
          <button
            key={day.toString()}
            onClick={() => isAvailable(day) && onDateSelect(day)}
            disabled={!isAvailable(day)}
            className={`
              p-2 text-center rounded-lg
              ${isAvailable(day) ? 'hover:bg-primary-100 cursor-pointer' : 'text-gray-300 cursor-not-allowed'}
              ${isSameDay(day, selectedDate) ? 'bg-primary-500 text-white' : ''}
              ${isToday(day) ? 'border border-primary-500' : ''}
            `}
          >
            {format(day, 'd')}
          </button>
        ))}
      </div>
    </div>
  );
}
```

### Phase 3: 예약 폼 및 성공 페이지 (Day 2-3)

#### BookingForm.tsx
```tsx
import { useState } from 'react';
import { useNavigate } from 'react-router-dom';

interface BookingFormProps {
  date: Date;
  slot: string;
  timezone: string;
  onBack: () => void;
}

export function BookingForm({ date, slot, timezone, onBack }: BookingFormProps) {
  const navigate = useNavigate();
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [formData, setFormData] = useState({
    name: '',
    email: '',
    notes: '',
  });

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setIsSubmitting(true);
    
    // Mock API call
    await new Promise(resolve => setTimeout(resolve, 1000));
    
    navigate('/schedule/success', { 
      state: { date, slot, name: formData.name } 
    });
  };

  return (
    <form onSubmit={handleSubmit} className="space-y-4">
      <button type="button" onClick={onBack} className="text-sm text-gray-500">
        ← 다른 시간 선택
      </button>
      
      <div className="bg-gray-100 p-4 rounded-lg">
        <p className="font-medium">
          {format(date, 'M월 d일 (E)', { locale: ko })} {slot}
        </p>
        <p className="text-sm text-gray-500">{timezone}</p>
      </div>

      <div>
        <label className="block text-sm font-medium mb-1">이름 *</label>
        <input
          type="text"
          required
          value={formData.name}
          onChange={(e) => setFormData({ ...formData, name: e.target.value })}
          className="w-full border rounded-lg px-3 py-2"
        />
      </div>

      <div>
        <label className="block text-sm font-medium mb-1">이메일 *</label>
        <input
          type="email"
          required
          value={formData.email}
          onChange={(e) => setFormData({ ...formData, email: e.target.value })}
          className="w-full border rounded-lg px-3 py-2"
        />
      </div>

      <div>
        <label className="block text-sm font-medium mb-1">메모 (선택)</label>
        <textarea
          value={formData.notes}
          onChange={(e) => setFormData({ ...formData, notes: e.target.value })}
          className="w-full border rounded-lg px-3 py-2"
          rows={3}
        />
      </div>

      <button
        type="submit"
        disabled={isSubmitting}
        className="w-full bg-primary-600 text-white py-3 rounded-lg hover:bg-primary-700 disabled:opacity-50"
      >
        {isSubmitting ? '예약 중...' : '예약 확정'}
      </button>
    </form>
  );
}
```

---

## 📁 산출물 (Artifacts)

```
src/
├── pages/
│   ├── SchedulePage.tsx
│   └── ScheduleSuccessPage.tsx
├── components/
│   └── schedule/
│       ├── HostInfo.tsx
│       ├── CalendarPicker.tsx
│       ├── SlotList.tsx
│       ├── TimezoneSelect.tsx
│       └── BookingForm.tsx
└── mocks/
    └── scheduleData.ts
```

---

## ✅ 완료 조건 (Acceptance Criteria)

| # | 조건 | 검증 방법 |
|---|------|----------|
| AC-1 | 예약 페이지 렌더링 | `/schedule/test` 접속 시 페이지 표시 |
| AC-2 | 주최자 정보 표시 | 좌측 패널에 프로필, 미팅 정보 표시 |
| AC-3 | 달력 네비게이션 | 월 이동 버튼 동작 |
| AC-4 | 날짜 선택 | 가용 날짜 클릭 시 하이라이트 |
| AC-5 | 슬롯 목록 표시 | 날짜 선택 시 해당 날짜의 슬롯 리스트 표시 |
| AC-6 | 타임존 변경 | 드롭다운에서 타임존 변경 가능 |
| AC-7 | 예약 폼 제출 | 폼 작성 후 제출 시 성공 페이지 이동 |
| AC-8 | 반응형 레이아웃 | 모바일에서 세로 스택 레이아웃 |

---

## 🔗 의존성

**선행 작업**:
- #004 (공통 레이아웃) - PublicLayout 사용

**후행 작업**:
- #016 (Slot Algorithm) 완료 후 실제 API 연동

**병렬 가능**:
- #006 (Dashboard UI) - 동일 레벨, 동시 진행 가능

---

## 🏷️ 라벨
`frontend`, `react`, `scheduling`, `priority:must`, `size:M`, `difficulty:medium`

---

## 📚 참고 문서
- Task Spec: `tasks/functional/EPIC0-FE-002_Scheduling_Link_Page.md`
- REQ-IDs: REQ-FUNC-001, REQ-FUNC-002, REQ-FUNC-003
- [date-fns 문서](https://date-fns.org/)
