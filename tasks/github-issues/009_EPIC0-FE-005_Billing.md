---
issue_number: 009
epic: EPIC-0
task_id: EPIC0-FE-005
title: "[FE] 시간 기록 및 인보이스 관리 UI"
labels: ["frontend", "react", "billing", "invoice", "priority:must", "size:M"]
assignees: []
milestone: "MVP-Phase-1-Core-Features"
dependencies: ["#006"]
parallel_group: "group-2-frontend"
estimated_effort: "M"
difficulty: "중"
estimated_duration: "3일"
status: "completed"
completed_note: "별도 FE 프로젝트에서 완료됨"
skip_github_issue: true
---

# ✅ [완료] Issue #009: 시간 기록 및 인보이스 관리 UI

> **📢 이 작업은 별도 프론트엔드 프로젝트에서 완료되었습니다.**  
> GitHub Issue 발행 대상에서 제외됩니다.

## 📋 개요
캘린더 일정과 연동된 시간 기록(Time Entries) 목록을 조회하고, 이를 바탕으로 생성된 인보이스(Invoice)의 상태를 관리하는 화면을 구현한다. 사용자가 "자동 생성된 기록"을 검토하고 "청구서 발송"을 누르는 흐름을 검증한다.

## 🎯 목표
- 시간 기록 테이블 UI 구현
- 인보이스 목록 및 상태 관리 UI
- 시간 기록 → 인보이스 생성 흐름
- 인보이스 발송 기능 (Mock)

---

## 📝 상세 요구사항

### 1. 화면 구성

**URL**: `/invoices`

#### 화면 레이아웃
```
┌──────────────────────────────────────────────────────────────┐
│  [Time Entries]  [Invoices]                                  │
├──────────────────────────────────────────────────────────────┤
│                                                              │
│  Time Entries Tab:                                           │
│  ┌────────┬──────────┬─────────────┬───────┬────────┬──────┐ │
│  │ 날짜   │ 프로젝트  │ 작업 내용    │ 시간  │ 청구   │ ☐   │ │
│  ├────────┼──────────┼─────────────┼───────┼────────┼──────┤ │
│  │ 12/01  │ Acme Inc │ Consulting  │ 2.0h  │ ✓      │ ☐   │ │
│  │ 12/01  │ Internal │ Team Sync   │ 1.0h  │ ✗      │ ☐   │ │
│  └────────┴──────────┴─────────────┴───────┴────────┴──────┘ │
│                                                              │
│  [선택 항목으로 인보이스 생성]                                 │
│                                                              │
├──────────────────────────────────────────────────────────────┤
│                                                              │
│  Invoices Tab:                                               │
│  ┌─────────────┬──────────┬─────────┬──────────┬───────────┐ │
│  │ Invoice #   │ Client   │ Amount  │ Status   │ Actions   │ │
│  ├─────────────┼──────────┼─────────┼──────────┼───────────┤ │
│  │ INV-001     │ Acme Inc │ $500    │ 🟡 Draft │ [Send]    │ │
│  │ INV-002     │ Beta Co  │ $1,200  │ 🟢 Paid  │ [View]    │ │
│  │ INV-003     │ Gamma    │ $800    │ 🔴 Overdue│ [Remind] │ │
│  └─────────────┴──────────┴─────────┴──────────┴───────────┘ │
│                                                              │
└──────────────────────────────────────────────────────────────┘
```

### 2. 상태별 배지 스타일

| 상태 | 색상 | 아이콘 |
|------|------|--------|
| Draft | 노란색 | 🟡 |
| Sent | 파란색 | 🔵 |
| Paid | 초록색 | 🟢 |
| Overdue | 빨간색 | 🔴 |

### 3. Mock 데이터

```typescript
interface TimeEntry {
  id: string;
  date: string;
  project: string;
  client: string;
  description: string;
  hours: number;
  rate: number;
  billable: boolean;
  invoiceId?: string;
}

interface Invoice {
  id: string;
  invoiceNumber: string;
  client: string;
  amount: number;
  currency: string;
  status: 'draft' | 'sent' | 'paid' | 'overdue';
  dueDate: string;
  createdAt: string;
}

const mockTimeEntries: TimeEntry[] = [
  { id: '1', date: '2025-12-01', project: 'Consulting', client: 'Acme Inc', description: 'Strategy session', hours: 2.0, rate: 100, billable: true },
  { id: '2', date: '2025-12-01', project: 'Internal', client: '-', description: 'Team Sync', hours: 1.0, rate: 0, billable: false },
  { id: '3', date: '2025-12-02', project: 'Development', client: 'Acme Inc', description: 'API integration', hours: 4.0, rate: 150, billable: true },
];

const mockInvoices: Invoice[] = [
  { id: '1', invoiceNumber: 'INV-001', client: 'Acme Inc', amount: 500, currency: 'USD', status: 'draft', dueDate: '2025-12-15', createdAt: '2025-12-01' },
  { id: '2', invoiceNumber: 'INV-002', client: 'Beta Corp', amount: 1200, currency: 'USD', status: 'paid', dueDate: '2025-11-30', createdAt: '2025-11-15' },
  { id: '3', invoiceNumber: 'INV-003', client: 'Gamma Ltd', amount: 800, currency: 'USD', status: 'overdue', dueDate: '2025-11-20', createdAt: '2025-11-05' },
];
```

---

## 🔧 구현 가이드

### Phase 1: 탭 및 테이블 구조 (Day 1)

#### InvoicePage.tsx
```tsx
import { useState } from 'react';

type TabType = 'entries' | 'invoices';

export function InvoicePage() {
  const [activeTab, setActiveTab] = useState<TabType>('entries');
  const [selectedEntries, setSelectedEntries] = useState<string[]>([]);

  return (
    <div>
      <h1 className="text-2xl font-bold mb-6">Billing & Invoices</h1>
      
      {/* Tab Navigation */}
      <div className="flex border-b mb-6">
        <button
          onClick={() => setActiveTab('entries')}
          className={`px-6 py-3 font-medium ${
            activeTab === 'entries' 
              ? 'border-b-2 border-blue-600 text-blue-600' 
              : 'text-gray-500'
          }`}
        >
          Time Entries
        </button>
        <button
          onClick={() => setActiveTab('invoices')}
          className={`px-6 py-3 font-medium ${
            activeTab === 'invoices' 
              ? 'border-b-2 border-blue-600 text-blue-600' 
              : 'text-gray-500'
          }`}
        >
          Invoices
        </button>
      </div>

      {/* Tab Content */}
      {activeTab === 'entries' ? (
        <TimeEntriesTab 
          selectedEntries={selectedEntries}
          onSelectionChange={setSelectedEntries}
        />
      ) : (
        <InvoicesTab />
      )}
    </div>
  );
}
```

### Phase 2: Time Entries 테이블 (Day 1-2)

#### TimeEntriesTab.tsx
```tsx
interface TimeEntriesTabProps {
  selectedEntries: string[];
  onSelectionChange: (ids: string[]) => void;
}

export function TimeEntriesTab({ selectedEntries, onSelectionChange }: TimeEntriesTabProps) {
  const [entries, setEntries] = useState(mockTimeEntries);

  const handleToggleBillable = (id: string) => {
    setEntries(entries.map(e => 
      e.id === id ? { ...e, billable: !e.billable } : e
    ));
  };

  const handleSelectAll = () => {
    if (selectedEntries.length === entries.length) {
      onSelectionChange([]);
    } else {
      onSelectionChange(entries.map(e => e.id));
    }
  };

  const handleSelect = (id: string) => {
    if (selectedEntries.includes(id)) {
      onSelectionChange(selectedEntries.filter(i => i !== id));
    } else {
      onSelectionChange([...selectedEntries, id]);
    }
  };

  const handleCreateInvoice = () => {
    const selectedData = entries.filter(e => selectedEntries.includes(e.id));
    const total = selectedData.reduce((sum, e) => sum + e.hours * e.rate, 0);
    alert(`Invoice created for $${total.toFixed(2)}`);
    // Navigate to invoices tab
  };

  return (
    <div>
      {/* Actions Bar */}
      {selectedEntries.length > 0 && (
        <div className="mb-4 p-4 bg-blue-50 rounded-lg flex items-center justify-between">
          <span>{selectedEntries.length}개 항목 선택됨</span>
          <button
            onClick={handleCreateInvoice}
            className="px-4 py-2 bg-blue-600 text-white rounded-lg"
          >
            인보이스 생성
          </button>
        </div>
      )}

      {/* Table */}
      <div className="bg-white rounded-xl shadow overflow-hidden">
        <table className="w-full">
          <thead className="bg-gray-50">
            <tr>
              <th className="p-4 text-left">
                <input 
                  type="checkbox" 
                  checked={selectedEntries.length === entries.length}
                  onChange={handleSelectAll}
                />
              </th>
              <th className="p-4 text-left">날짜</th>
              <th className="p-4 text-left">프로젝트</th>
              <th className="p-4 text-left">작업 내용</th>
              <th className="p-4 text-right">시간</th>
              <th className="p-4 text-right">금액</th>
              <th className="p-4 text-center">청구</th>
            </tr>
          </thead>
          <tbody>
            {entries.map((entry) => (
              <tr key={entry.id} className="border-t hover:bg-gray-50">
                <td className="p-4">
                  <input
                    type="checkbox"
                    checked={selectedEntries.includes(entry.id)}
                    onChange={() => handleSelect(entry.id)}
                  />
                </td>
                <td className="p-4">{entry.date}</td>
                <td className="p-4">
                  <div>{entry.project}</div>
                  <div className="text-sm text-gray-500">{entry.client}</div>
                </td>
                <td className="p-4">{entry.description}</td>
                <td className="p-4 text-right">{entry.hours}h</td>
                <td className="p-4 text-right">
                  ${(entry.hours * entry.rate).toFixed(2)}
                </td>
                <td className="p-4 text-center">
                  <button
                    onClick={() => handleToggleBillable(entry.id)}
                    className={`w-6 h-6 rounded-full ${
                      entry.billable ? 'bg-green-500' : 'bg-gray-300'
                    }`}
                  >
                    {entry.billable ? '✓' : ''}
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}
```

### Phase 3: Invoices 테이블 (Day 2-3)

#### InvoicesTab.tsx
```tsx
export function InvoicesTab() {
  const [invoices, setInvoices] = useState(mockInvoices);

  const handleSend = (id: string) => {
    setInvoices(invoices.map(inv => 
      inv.id === id ? { ...inv, status: 'sent' as const } : inv
    ));
    alert('인보이스가 발송되었습니다!');
  };

  const getStatusBadge = (status: Invoice['status']) => {
    const styles = {
      draft: 'bg-yellow-100 text-yellow-800',
      sent: 'bg-blue-100 text-blue-800',
      paid: 'bg-green-100 text-green-800',
      overdue: 'bg-red-100 text-red-800',
    };
    const labels = {
      draft: '🟡 Draft',
      sent: '🔵 Sent',
      paid: '🟢 Paid',
      overdue: '🔴 Overdue',
    };
    return (
      <span className={`px-3 py-1 rounded-full text-sm ${styles[status]}`}>
        {labels[status]}
      </span>
    );
  };

  return (
    <div className="bg-white rounded-xl shadow overflow-hidden">
      <table className="w-full">
        <thead className="bg-gray-50">
          <tr>
            <th className="p-4 text-left">Invoice #</th>
            <th className="p-4 text-left">Client</th>
            <th className="p-4 text-right">Amount</th>
            <th className="p-4 text-left">Due Date</th>
            <th className="p-4 text-center">Status</th>
            <th className="p-4 text-center">Actions</th>
          </tr>
        </thead>
        <tbody>
          {invoices.map((invoice) => (
            <tr key={invoice.id} className="border-t hover:bg-gray-50">
              <td className="p-4 font-medium">{invoice.invoiceNumber}</td>
              <td className="p-4">{invoice.client}</td>
              <td className="p-4 text-right font-medium">
                ${invoice.amount.toLocaleString()}
              </td>
              <td className="p-4">{invoice.dueDate}</td>
              <td className="p-4 text-center">
                {getStatusBadge(invoice.status)}
              </td>
              <td className="p-4 text-center">
                {invoice.status === 'draft' && (
                  <button
                    onClick={() => handleSend(invoice.id)}
                    className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700"
                  >
                    Send
                  </button>
                )}
                {invoice.status === 'overdue' && (
                  <button className="px-4 py-2 bg-red-600 text-white rounded-lg hover:bg-red-700">
                    Remind
                  </button>
                )}
                {(invoice.status === 'sent' || invoice.status === 'paid') && (
                  <button className="px-4 py-2 border rounded-lg hover:bg-gray-50">
                    View
                  </button>
                )}
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
```

---

## 📁 산출물 (Artifacts)

```
src/
├── pages/
│   └── InvoicePage.tsx
├── components/
│   └── billing/
│       ├── TimeEntriesTab.tsx
│       ├── TimeEntryRow.tsx
│       ├── InvoicesTab.tsx
│       ├── InvoiceRow.tsx
│       └── StatusBadge.tsx
└── mocks/
    └── billingData.ts
```

---

## ✅ 완료 조건 (Acceptance Criteria)

| # | 조건 | 검증 방법 |
|---|------|----------|
| AC-1 | 탭 전환 동작 | Time Entries / Invoices 탭 클릭 시 전환 |
| AC-2 | 시간 기록 목록 표시 | 날짜, 프로젝트, 시간, 금액 표시 |
| AC-3 | 청구 가능 토글 | 토글 클릭 시 상태 변경 |
| AC-4 | 다중 선택 | 체크박스로 여러 항목 선택 가능 |
| AC-5 | 인보이스 생성 | 선택 항목으로 인보이스 생성 (Mock) |
| AC-6 | 상태별 배지 | Draft/Sent/Paid/Overdue 색상 구분 |
| AC-7 | Send 버튼 | Draft 상태에서 클릭 시 Sent로 변경 |
| AC-8 | 반응형 레이아웃 | 모바일에서도 테이블 스크롤 가능 |

---

## 🔗 의존성

**선행 작업**:
- #006 (Dashboard) - AppLayout 사용

**후행 작업**:
- #014 (Time Tracking) 완료 후 API 연동
- #017 (Invoice) 완료 후 API 연동

**병렬 가능**:
- #008 (Summary UI) - 동일 레벨

---

## 🏷️ 라벨
`frontend`, `react`, `billing`, `invoice`, `priority:must`, `size:M`, `difficulty:medium`

---

## 📚 참고 문서
- Task Spec: `tasks/functional/EPIC0-FE-005_Billing_Invoice_View.md`
- REQ-IDs: REQ-FUNC-020, REQ-FUNC-021
