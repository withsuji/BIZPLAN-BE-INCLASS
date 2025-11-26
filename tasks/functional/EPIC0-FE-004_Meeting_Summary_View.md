# EPIC0-FE-004: FE PoC - 회의 요약 및 액션 아이템 뷰

## 1. 개요 및 목적
완료된 회의에 대해 AI가 생성한 요약 노트, 결정 사항, 액션 아이템을 확인하고 수정하는 상세 화면을 구현한다. 사용자가 요약 내용을 검토하고 "확정"하거나 "재생성" 요청을 하는 UX를 검증한다.

## 2. 상세 요구사항

### 2.1. 화면 구성
- **URL**: `/meetings/:id`
- **Header**: 회의 제목, 일시, 참석자, 상태(요약 완료/처리 중).
- **Summary Section**:
    - **One-line Summary**: 한 줄 요약.
    - **Key Decisions**: 결정된 사항 (불릿 포인트).
    - **Action Items**: 할 일 목록 (체크박스, 담당자, 기한 포함).
    - **Transcript (Tab)**: 원문 스크립트 보기 (접기/펼치기).
- **Actions**:
    - "Copy to Clipboard" 버튼.
    - "Regenerate Summary" 버튼 (Loading UI 시뮬레이션).
    - "Edit" 모드 진입 버튼.

### 2.2. 상호작용 (Mock)
- "Regenerate" 버튼 클릭 시 3초간 스피너가 돌고 텍스트가 바뀌는 Mock 동작.
- Action Item의 체크박스를 토글하거나 텍스트를 수정할 수 있어야 함(로컬 State).

## 3. Task Definition (YAML)

```yaml
task_id: "EPIC0-FE-004"
title: "FE PoC - AI 회의 요약 결과 조회 및 수정 UI"
summary: >
  AI가 분석한 회의 요약(결정/액션)을 구조화하여 보여주는 페이지를 구현한다.
  재생성 및 편집 인터랙션을 Mock으로 포함한다.
type: "functional"

epic: "EPIC_0_FE_PROTOTYPE"
req_ids: ["REQ-FUNC-011"]
component: ["frontend.summary"]

context:
  srs_section: "3.2 Meeting/Summary Service"

inputs:
  description: "Mock Summary Data (JSON)"
  mock_data_example: |
    {
      "id": 101,
      "title": "Q4 Marketing Strategy",
      "status": "completed",
      "summary": "4분기 마케팅 예산 증액 및 채널 다변화 논의",
      "decisions": ["예산 20% 증액 확정", "인플루언서 마케팅 시작"],
      "action_items": [
        {"text": "예산안 기안", "owner": "Kim", "due": "2025-12-01"},
        {"text": "대행사 리스트업", "owner": "Lee", "due": "2025-11-30"}
      ]
    }

outputs:
  description: "회의 요약 상세 페이지"
  artifacts:
    - "src/pages/MeetingDetailPage.tsx"
    - "src/components/SummaryCard.tsx"
    - "src/components/ActionItemList.tsx"

steps_hint:
  - "MeetingDetailPage 레이아웃 구성 (Header + Body)"
  - "Mock Data 바인딩"
  - "ActionItemList 컴포넌트 구현 (각 아이템은 수정 가능한 Input으로 전환 가능)"
  - "재생성 버튼 클릭 시 `setTimeout`으로 비동기 로딩 상태(Loading Skeleton) 시뮬레이션"
  - "Transcript 탭 구현 (긴 텍스트 스크롤 처리)"

preconditions:
  - "EPIC0-FE-001 (Layout) 완료"

postconditions:
  - "요약 데이터가 깔끔한 타이포그래피로 가독성 있게 표시되어야 한다."
  - "재생성/로딩 상태 UI가 자연스러워야 한다."

dependencies: ["EPIC0-FE-001"]

parallelizable: true
estimated_effort: "S"
priority: "Must"
agent_profile: ["frontend"]
```









