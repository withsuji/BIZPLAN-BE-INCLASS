# EPIC0-FE-002: FE PoC - 스케줄링 예약 페이지 UI

## 1. 개요 및 목적
외부 사용자(클라이언트, 파트너)가 링크를 통해 접속하여 미팅 가능한 시간을 선택하고 예약을 확정하는 화면(Public View)을 구현한다. 타임존 자동 변환과 대체 슬롯 제안 기능을 UI상에서 시뮬레이션해야 한다.

## 2. 상세 요구사항

### 2.1. 화면 구성
- **URL**: `/schedule/:linkId` (예: `/schedule/suji-meeting`)
- **좌측 패널**:
    - 주최자 프로필 (이름, 직함, 사진)
    - 미팅 제목 및 설명 (예: "30분 커피챗")
    - 소요 시간 (30 min)
- **우측 패널 (캘린더/슬롯 선택)**:
    - 월 달력 뷰 (날짜 선택)
    - 선택한 날짜의 가용 슬롯 리스트
    - **Timezone Selector**: 사용자가 자신의 타임존을 변경할 수 있는 드롭다운.
- **예약 폼 (슬롯 클릭 시)**:
    - 이름, 이메일, 메모 입력 필드.
    - "예약 확정" 버튼.

### 2.2. 상호작용 (Mock)
- 타임존 변경 시 슬롯 시간이 변환되어 표시되는 것처럼 Mock 데이터를 구성한다.
- 날짜를 바꾸면 해당 날짜의 슬롯이 갱신된다.
- 예약 확정 버튼 클릭 시 "성공 페이지"로 이동한다.

## 3. Task Definition (YAML)

```yaml
task_id: "EPIC0-FE-002"
title: "FE PoC - 외부 사용자용 스케줄링 및 슬롯 선택 UI 구현"
summary: >
  외부 사용자가 접속하는 예약 페이지를 Public Layout 기반으로 구현한다.
  타임존 선택, 날짜별 슬롯 표시, 예약 폼 제출 과정을 Mock으로 처리한다.
type: "functional"

epic: "EPIC_0_FE_PROTOTYPE"
req_ids: ["REQ-FUNC-001", "REQ-FUNC-002", "REQ-FUNC-003"]
component: ["frontend.schedule"]

context:
  srs_section: "3.1 External Systems (Google/Outlook) & 3.4 Interaction Sequences"
  related_layout: "PublicLayout"

inputs:
  description: "Mock 가용 슬롯 데이터 (JSON)"
  mock_data_example: |
    [
      { "date": "2025-11-25", "slots": ["10:00", "14:00", "16:30"] },
      { "date": "2025-11-26", "slots": ["11:00", "13:00"] }
    ]

outputs:
  description: "예약 가능한 스케줄링 페이지 컴포넌트"
  artifacts:
    - "src/pages/SchedulePage.tsx"
    - "src/components/TimezoneSelect.tsx"
    - "src/components/SlotPicker.tsx"

steps_hint:
  - "Mock Data 정의 (사용자 프로필, 날짜별 가용 슬롯)"
  - "SchedulePage 구조 잡기 (좌측 프로필, 우측 캘린더)"
  - "`react-calendar` 또는 유사 라이브러리(혹은 직접 구현)로 날짜 선택기 구현"
  - "타임존 드롭다운 구현 (단순히 Label만 바꾸거나, `date-fns-tz`로 시간 변환 시늉)"
  - "예약 폼 모달 또는 인라인 확장 UI 구현"
  - "예약 완료 후 결과 페이지(`/schedule/success`) 라우팅"

preconditions:
  - "EPIC0-FE-001 (Layout)이 완료되어야 함"

postconditions:
  - "URL로 진입 시 예약 페이지가 렌더링된다."
  - "날짜를 클릭하면 슬롯 목록이 바뀐다."
  - "예약 확정 시 성공 메시지를 볼 수 있다."

dependencies: ["EPIC0-FE-001"]

parallelizable: true
estimated_effort: "M"
priority: "Must"
agent_profile: ["frontend"]

risk_notes:
  - "실제 타임존 계산 로직은 복잡하므로 PoC에서는 Mock 데이터가 특정 타임존(Asia/Seoul) 기준이라고 가정하고 단순 텍스트 매핑만 수행한다."
```









