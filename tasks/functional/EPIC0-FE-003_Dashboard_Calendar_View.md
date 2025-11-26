# EPIC0-FE-003: FE PoC - 대시보드 및 캘린더 메인 뷰

## 1. 개요 및 목적
내부 사용자(로그인한 유저)가 자신의 일정을 확인하고 관리하는 메인 대시보드를 구현한다. 주간/월간 캘린더 뷰를 제공하며, 일반 일정과 "포커스 블록(Focus Block)"이 시각적으로 구분되어야 한다.

## 2. 상세 요구사항

### 2.1. 화면 구성
- **URL**: `/calendar` (또는 `/dashboard`에서 캘린더 위젯 형태)
- **Calendar View**:
    - 주간(Weekly) 뷰를 기본으로 함.
    - `react-big-calendar` 또는 `FullCalendar` 등의 라이브러리 활용(또는 CSS Grid로 간이 구현).
- **이벤트 표시**:
    - **일반 미팅**: 파란색 계열. 제목, 시간 표시.
    - **포커스 블록**: 보라색/회색 계열. "Focus Time" 라벨링.
    - **충돌/경고**: 붉은색 테두리 또는 아이콘 (SRS의 '충돌 감지' 시각화).
- **사이드 패널 (Right or Left)**:
    - "오늘의 요약": 예정된 미팅 수, 확보된 포커스 시간 합계.
    - "미팅 카드": 다음 미팅 정보 및 "요약 보기/생성" 버튼.

### 2.2. 상호작용 (Mock)
- 캘린더의 빈 공간을 클릭하면 "새 일정 생성" 모달이 뜬다 (저장은 Mock).
- 기존 이벤트를 클릭하면 상세 팝업이 뜬다.
- "포커스 시간 확보하기" 버튼을 누르면 빈 슬롯에 포커스 블록이 자동 생성되는 애니메이션(Mock)을 보여준다.

## 3. Task Definition (YAML)

```yaml
task_id: "EPIC0-FE-003"
title: "FE PoC - 내부 사용자용 캘린더 및 대시보드 UI"
summary: >
  로그인 사용자가 보는 메인 캘린더 뷰를 구현한다.
  일반 일정과 포커스 블록을 시각적으로 구분하고, 간단한 일정 생성 인터랙션을 포함한다.
type: "functional"

epic: "EPIC_0_FE_PROTOTYPE"
req_ids: ["REQ-FUNC-001", "REQ-FUNC-002"]
component: ["frontend.calendar", "frontend.dashboard"]

context:
  srs_section: "3.2 Client Applications - 캘린더/포커스 설정 UI"
  
inputs:
  description: "Mock Calendar Events (JSON)"
  mock_data_example: |
    [
      { "id": 1, "title": "Team Sync", "start": "2025-11-25T10:00:00", "end": "2025-11-25T11:00:00", "type": "meeting" },
      { "id": 2, "title": "Focus Time", "start": "2025-11-25T14:00:00", "end": "2025-11-25T16:00:00", "type": "focus" }
    ]

outputs:
  description: "이벤트 렌더링이 포함된 캘린더 페이지"
  artifacts:
    - "src/pages/CalendarPage.tsx"
    - "src/components/CalendarView.tsx"
    - "src/components/EventCard.tsx"

steps_hint:
  - "Calendar 라이브러리 선정 및 설치 (react-big-calendar 추천)"
  - "Mock Events 데이터를 정의하고 캘린더에 바인딩"
  - "이벤트 타입(`meeting` vs `focus`)에 따른 조건부 스타일링 구현"
  - "일정 클릭 시 상세 모달(Event Detail Modal) 구현"
  - "사이드 패널에 KPI 요약(총 포커스 시간 등) 표시 컴포넌트 배치"

preconditions:
  - "EPIC0-FE-001 (Layout) 완료"

postconditions:
  - "사용자는 자신의 일정을 주간 뷰로 볼 수 있다."
  - "포커스 블록이 시각적으로 구분된다."

dependencies: ["EPIC0-FE-001"]

parallelizable: true
estimated_effort: "M"
priority: "Must"
agent_profile: ["frontend"]

risk_notes:
  - "캘린더 라이브러리 커스터마이징에 시간을 너무 쏟지 말 것. 기본 뷰만 나오면 됨."
```









