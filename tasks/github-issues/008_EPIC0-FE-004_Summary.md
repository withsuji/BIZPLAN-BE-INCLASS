---
issue_number: 008
epic: EPIC-0
task_id: EPIC0-FE-004
title: "[FE] 회의 요약 및 액션 아이템 뷰 UI"
labels: ["frontend", "react", "meeting", "summary", "priority:must", "size:S"]
assignees: []
milestone: "MVP-Phase-1-Core-Features"
dependencies: ["#006"]
parallel_group: "group-2-frontend"
estimated_effort: "S"
difficulty: "하"
estimated_duration: "2일"
status: "completed"
completed_note: "별도 FE 프로젝트에서 완료됨"
skip_github_issue: true
---

# ✅ [완료] Issue #008: 회의 요약 및 액션 아이템 뷰 UI

> **📢 이 작업은 별도 프론트엔드 프로젝트에서 완료되었습니다.**  
> GitHub Issue 발행 대상에서 제외됩니다.

## 📋 개요
완료된 회의에 대해 AI가 생성한 요약 노트, 결정 사항, 액션 아이템을 확인하고 수정하는 상세 화면을 구현한다. 사용자가 요약 내용을 검토하고 "확정"하거나 "재생성" 요청을 하는 UX를 검증한다.

## 🎯 목표
- 회의 요약 상세 페이지 구현
- 액션 아이템 체크리스트 UI
- 요약 재생성 기능 (Mock)
- 클립보드 복사 기능

---

## 📝 상세 요구사항

### 1. 화면 구성

**URL**: `/meetings/:id`

#### 화면 레이아웃
```
┌──────────────────────────────────────────────────────────────┐
│  ← 뒤로  |  Q4 Marketing Strategy  |  📋 복사  |  🔄 재생성  │
├──────────────────────────────────────────────────────────────┤
│  📅 2025년 12월 1일 10:00-11:00                              │
│  👥 김철수, 이영희, 박민수                                    │
│  🏷️ 요약 완료                                                │
├──────────────────────────────────────────────────────────────┤
│                                                              │
│  📝 한 줄 요약                                               │
│  ─────────────────                                           │
│  4분기 마케팅 예산 증액 및 채널 다변화 논의                    │
│                                                              │
│  ✅ 주요 결정사항                                            │
│  ─────────────────                                           │
│  • 예산 20% 증액 확정                                        │
│  • 인플루언서 마케팅 시작                                    │
│                                                              │
│  📌 액션 아이템                                              │
│  ─────────────────                                           │
│  ☐ 예산안 기안 - 김철수 (12/01)                             │
│  ☐ 대행사 리스트업 - 이영희 (11/30)                         │
│                                                              │
│  📄 원문 스크립트 [펼치기 ▼]                                 │
│                                                              │
└──────────────────────────────────────────────────────────────┘
```

### 2. 컴포넌트 구성

#### Header
- 뒤로 가기 버튼
- 회의 제목
- 복사 버튼 (전체 요약)
- 재생성 버튼

#### Meeting Info
- 일시
- 참석자
- 상태 배지 (처리 중 / 완료)

#### Summary Section
- 한 줄 요약
- 주요 결정사항 (불릿 리스트)
- 액션 아이템 (체크박스 리스트)
- 원문 스크립트 (Collapsible)

### 3. 액션 아이템 상호작용
- 체크박스 토글 (완료 표시)
- 인라인 수정 가능
- 담당자, 기한 표시

### 4. Mock 데이터

```typescript
interface MeetingSummary {
  id: string;
  title: string;
  datetime: Date;
  participants: string[];
  status: 'processing' | 'completed';
  summary: string;
  decisions: string[];
  actionItems: ActionItem[];
  transcript?: string;
  confidenceScore: number;
}

interface ActionItem {
  id: string;
  text: string;
  owner: string;
  dueDate: string;
  completed: boolean;
}

const mockSummary: MeetingSummary = {
  id: '101',
  title: 'Q4 Marketing Strategy',
  datetime: new Date('2025-12-01T10:00:00'),
  participants: ['김철수', '이영희', '박민수'],
  status: 'completed',
  summary: '4분기 마케팅 예산 증액 및 채널 다변화 논의',
  decisions: [
    '예산 20% 증액 확정',
    '인플루언서 마케팅 시작',
  ],
  actionItems: [
    { id: '1', text: '예산안 기안', owner: '김철수', dueDate: '2025-12-01', completed: false },
    { id: '2', text: '대행사 리스트업', owner: '이영희', dueDate: '2025-11-30', completed: false },
  ],
  transcript: '김철수: 오늘 4분기 마케팅 전략에 대해...',
  confidenceScore: 0.95,
};
```

---

## 🔧 구현 가이드

### Phase 1: 페이지 구조 (Day 1)

#### MeetingDetailPage.tsx
```tsx
import { useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { format } from 'date-fns';
import { ko } from 'date-fns/locale';

export function MeetingDetailPage() {
  const { id } = useParams();
  const navigate = useNavigate();
  const [isRegenerating, setIsRegenerating] = useState(false);
  const [summary, setSummary] = useState(mockSummary);
  const [isTranscriptOpen, setIsTranscriptOpen] = useState(false);

  const handleRegenerate = async () => {
    setIsRegenerating(true);
    // Mock API call
    await new Promise(resolve => setTimeout(resolve, 3000));
    setSummary({
      ...summary,
      summary: '4분기 마케팅 전략 회의에서 예산 증액과 신규 채널 도입을 논의함',
    });
    setIsRegenerating(false);
  };

  const handleCopy = () => {
    const text = `
# ${summary.title}

## 요약
${summary.summary}

## 결정사항
${summary.decisions.map(d => `- ${d}`).join('\n')}

## 액션 아이템
${summary.actionItems.map(a => `- [ ] ${a.text} (${a.owner}, ${a.dueDate})`).join('\n')}
    `.trim();
    
    navigator.clipboard.writeText(text);
    alert('클립보드에 복사되었습니다!');
  };

  return (
    <div className="max-w-3xl mx-auto">
      {/* Header */}
      <div className="flex items-center justify-between mb-6">
        <button onClick={() => navigate(-1)} className="text-gray-500">
          ← 뒤로
        </button>
        <div className="flex gap-2">
          <button 
            onClick={handleCopy}
            className="px-4 py-2 border rounded-lg hover:bg-gray-50"
          >
            📋 복사
          </button>
          <button 
            onClick={handleRegenerate}
            disabled={isRegenerating}
            className="px-4 py-2 bg-purple-600 text-white rounded-lg hover:bg-purple-700 disabled:opacity-50"
          >
            {isRegenerating ? '⏳ 생성 중...' : '🔄 재생성'}
          </button>
        </div>
      </div>

      {/* Meeting Info */}
      <div className="bg-white rounded-xl shadow p-6 mb-6">
        <h1 className="text-2xl font-bold mb-4">{summary.title}</h1>
        <div className="flex flex-wrap gap-4 text-sm text-gray-600">
          <span>📅 {format(summary.datetime, 'yyyy년 M월 d일 HH:mm', { locale: ko })}</span>
          <span>👥 {summary.participants.join(', ')}</span>
          <span className={`px-2 py-1 rounded-full text-xs ${
            summary.status === 'completed' ? 'bg-green-100 text-green-800' : 'bg-yellow-100 text-yellow-800'
          }`}>
            {summary.status === 'completed' ? '✅ 요약 완료' : '⏳ 처리 중'}
          </span>
        </div>
      </div>

      {/* Summary Content */}
      <SummaryContent 
        summary={summary} 
        onActionItemToggle={handleActionItemToggle}
        isTranscriptOpen={isTranscriptOpen}
        onTranscriptToggle={() => setIsTranscriptOpen(!isTranscriptOpen)}
      />

      {/* Loading Overlay */}
      {isRegenerating && (
        <div className="fixed inset-0 bg-black/30 flex items-center justify-center">
          <div className="bg-white p-6 rounded-xl shadow-xl">
            <div className="animate-spin w-8 h-8 border-4 border-purple-600 border-t-transparent rounded-full mx-auto mb-4" />
            <p>AI가 요약을 재생성하고 있습니다...</p>
          </div>
        </div>
      )}
    </div>
  );
}
```

### Phase 2: 요약 컴포넌트 구현 (Day 1-2)

#### SummaryContent.tsx
```tsx
interface SummaryContentProps {
  summary: MeetingSummary;
  onActionItemToggle: (id: string) => void;
  isTranscriptOpen: boolean;
  onTranscriptToggle: () => void;
}

export function SummaryContent({ 
  summary, 
  onActionItemToggle,
  isTranscriptOpen,
  onTranscriptToggle,
}: SummaryContentProps) {
  return (
    <div className="space-y-6">
      {/* 한 줄 요약 */}
      <section className="bg-white rounded-xl shadow p-6">
        <h2 className="text-lg font-semibold mb-3">📝 한 줄 요약</h2>
        <p className="text-gray-700">{summary.summary}</p>
        {summary.confidenceScore < 0.8 && (
          <p className="mt-2 text-sm text-yellow-600">
            ⚠️ AI 신뢰도가 낮습니다. 내용을 확인해주세요.
          </p>
        )}
      </section>

      {/* 주요 결정사항 */}
      <section className="bg-white rounded-xl shadow p-6">
        <h2 className="text-lg font-semibold mb-3">✅ 주요 결정사항</h2>
        <ul className="space-y-2">
          {summary.decisions.map((decision, idx) => (
            <li key={idx} className="flex items-start gap-2">
              <span className="text-green-500">•</span>
              <span>{decision}</span>
            </li>
          ))}
        </ul>
      </section>

      {/* 액션 아이템 */}
      <section className="bg-white rounded-xl shadow p-6">
        <h2 className="text-lg font-semibold mb-3">📌 액션 아이템</h2>
        <ul className="space-y-3">
          {summary.actionItems.map((item) => (
            <ActionItemRow 
              key={item.id} 
              item={item} 
              onToggle={() => onActionItemToggle(item.id)}
            />
          ))}
        </ul>
      </section>

      {/* 원문 스크립트 */}
      {summary.transcript && (
        <section className="bg-white rounded-xl shadow p-6">
          <button 
            onClick={onTranscriptToggle}
            className="flex items-center justify-between w-full"
          >
            <h2 className="text-lg font-semibold">📄 원문 스크립트</h2>
            <span>{isTranscriptOpen ? '▲' : '▼'}</span>
          </button>
          {isTranscriptOpen && (
            <div className="mt-4 p-4 bg-gray-50 rounded-lg max-h-60 overflow-y-auto">
              <pre className="whitespace-pre-wrap text-sm text-gray-600">
                {summary.transcript}
              </pre>
            </div>
          )}
        </section>
      )}
    </div>
  );
}
```

#### ActionItemRow.tsx
```tsx
interface ActionItemRowProps {
  item: ActionItem;
  onToggle: () => void;
}

export function ActionItemRow({ item, onToggle }: ActionItemRowProps) {
  const isOverdue = new Date(item.dueDate) < new Date() && !item.completed;

  return (
    <li className={`flex items-center gap-3 p-3 rounded-lg ${
      item.completed ? 'bg-gray-50' : 'bg-white border'
    }`}>
      <input
        type="checkbox"
        checked={item.completed}
        onChange={onToggle}
        className="w-5 h-5 rounded border-gray-300"
      />
      <div className="flex-1">
        <span className={item.completed ? 'line-through text-gray-400' : ''}>
          {item.text}
        </span>
        <div className="flex gap-2 text-sm text-gray-500 mt-1">
          <span>👤 {item.owner}</span>
          <span className={isOverdue ? 'text-red-500' : ''}>
            📅 {item.dueDate} {isOverdue && '(기한 초과)'}
          </span>
        </div>
      </div>
    </li>
  );
}
```

---

## 📁 산출물 (Artifacts)

```
src/
├── pages/
│   ├── MeetingsPage.tsx
│   └── MeetingDetailPage.tsx
├── components/
│   └── meeting/
│       ├── SummaryContent.tsx
│       ├── ActionItemRow.tsx
│       └── TranscriptViewer.tsx
└── mocks/
    └── summaryData.ts
```

---

## ✅ 완료 조건 (Acceptance Criteria)

| # | 조건 | 검증 방법 |
|---|------|----------|
| AC-1 | 상세 페이지 렌더링 | `/meetings/101` 접속 시 요약 정보 표시 |
| AC-2 | 요약 정보 표시 | 한 줄 요약, 결정사항, 액션 아이템 모두 표시 |
| AC-3 | 액션 아이템 토글 | 체크박스 클릭 시 완료 상태 변경 |
| AC-4 | 재생성 버튼 | 클릭 시 로딩 UI 표시 후 요약 변경 |
| AC-5 | 복사 버튼 | 클릭 시 클립보드에 마크다운 형식 복사 |
| AC-6 | 스크립트 토글 | 펼치기/접기 동작 |
| AC-7 | 신뢰도 경고 | 신뢰도 0.8 미만일 때 경고 메시지 표시 |

---

## 🔗 의존성

**선행 작업**:
- #006 (Dashboard) - 미팅 목록에서 상세 페이지 진입

**후행 작업**:
- #015 (Summary Storage) 완료 후 실제 API 연동

**병렬 가능**:
- #009 (Billing UI) - 동일 레벨

---

## 🏷️ 라벨
`frontend`, `react`, `meeting`, `summary`, `priority:must`, `size:S`, `difficulty:easy`

---

## 📚 참고 문서
- Task Spec: `tasks/functional/EPIC0-FE-004_Meeting_Summary_View.md`
- REQ-IDs: REQ-FUNC-011
