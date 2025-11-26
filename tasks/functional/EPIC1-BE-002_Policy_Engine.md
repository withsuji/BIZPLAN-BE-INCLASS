# EPIC1-BE-002: 타임존 정규화 및 업무시간/공휴일 정책 엔진

## 1. 개요 및 목적
다양한 타임존의 사용자 간 일정 조율을 위한 타임존 정규화 로직과 업무시간, 공휴일, 개인 선호도를 반영한 정책 엔진을 구현한다.

## 2. 상세 요구사항

### 2.1. 타임존 정규화
- **IANA Timezone DB**: 모든 시간은 IANA Timezone 기준으로 저장 및 처리
- **UTC Conversion**: DB에는 UTC로 저장, 조회 시 사용자 타임존으로 변환
- **DST Handling**: 서머타임 자동 처리 (java.time.ZonedDateTime 활용)
- **Timezone Validation**: 유효하지 않은 타임존 입력 시 에러 반환

### 2.2. 업무시간 정책
- **Working Hours**: 
  - 사용자별 업무 시작/종료 시간 설정 (예: 09:00-18:00)
  - 요일별 다른 설정 가능 (월-금, 토-일 분리)
- **Break Time**: 점심시간 등 휴식 시간 설정
- **Preferred Meeting Hours**: 미팅 선호 시간대 설정 (예: 오전 10시-12시, 오후 2시-4시)
- **Buffer Time**: 미팅 간 최소 간격 설정 (예: 10분)

### 2.3. 공휴일 관리
- **Holiday Calendar**: 
  - 국가/지역별 공휴일 데이터 (API 또는 DB에 사전 로드)
  - 사용자 정의 휴일 추가 가능
- **Holiday API Integration**: 
  - Calendarific API 또는 Google Calendar Holiday API 연동
  - 캐싱을 통한 성능 최적화 (연간 데이터 캐시)
- **Holiday Handling**: 공휴일에는 미팅 슬롯 제외

### 2.4. 충돌 감지 및 검증
- **Time Overlap Check**: 기존 이벤트와 시간 충돌 여부 확인
- **Focus Block Protection**: 포커스 블록 시간대는 미팅 불가
- **Policy Violation Detection**: 업무시간 외, 공휴일, 버퍼 타임 위반 감지
- **Warning Level**: 위반 심각도에 따라 ERROR | WARNING | INFO 반환

### 2.5. 데이터 모델
```yaml
UserPolicy:
  - id: UUID
  - user_id: FK
  - timezone: string (IANA)
  - working_hours: JSON
    {
      "monday": {"start": "09:00", "end": "18:00"},
      "tuesday": {"start": "09:00", "end": "18:00"},
      ...
    }
  - break_times: JSON
    [{"start": "12:00", "end": "13:00", "days": ["monday", "tuesday", ...]}]
  - preferred_meeting_hours: JSON
  - buffer_minutes: integer
  - created_at: timestamp
  - updated_at: timestamp

Holiday:
  - id: UUID
  - country_code: string (ISO 3166)
  - date: date
  - name: string
  - is_public: boolean
  - created_at: timestamp

UserHoliday:
  - id: UUID
  - user_id: FK
  - date: date
  - name: string
  - created_at: timestamp
```

## 3. Task Definition (YAML)

```yaml
task_id: "EPIC1-BE-002"
title: "타임존 정규화 및 업무시간/공휴일 정책 엔진 구현"
summary: >
  다국적 협업을 지원하는 타임존 처리 로직과
  사용자별 업무시간/공휴일 정책을 관리하는 엔진을 구축한다.
type: "functional"

epic: "EPIC_1_CALENDAR_CORE"
req_ids: ["REQ-FUNC-001", "REQ-FUNC-002"]
component: ["backend.calendar", "backend.policy"]

context:
  srs_section: "3.4 Interaction Sequences (타임존 자동 스케줄링)"
  tech_stack: ["Spring Boot", "java.time", "MySQL", "Redis"]

requirements:
  description: "정확한 타임존 변환 및 정책 기반 일정 검증"
  kpis:
    - "타임존 변환 정확도 100%"
    - "정책 위반 감지율 100%"
    - "정책 검증 시간 < 50ms"

design_constraints:
  - "모든 시간 계산은 UTC 기준으로 수행 후 타임존 변환"
  - "공휴일 데이터는 연 단위로 캐싱하여 API 호출 최소화"
  - "정책 위반 시에도 예약 가능하도록 경고만 표시 (강제 차단 아님)"

steps_hint:
  - "UserPolicy, Holiday, UserHoliday Entity 및 Repository 생성"
  - "TimezoneService 구현 (UTC <-> User TZ 변환)"
  - "WorkingHoursService 구현 (업무시간 검증)"
  - "HolidayService 구현 (공휴일 조회 및 캐싱)"
  - "PolicyEngine 구현 (종합 정책 검증)"
  - "Calendarific API Client 구현 (공휴일 데이터 로드)"
  - "TimeOverlapChecker 구현 (충돌 감지 알고리즘)"
  - "Policy Violation DTO 및 Response 구조 정의"
  - "단위 테스트 작성 (타임존 경계 케이스, DST 전환 등)"

preconditions:
  - "EPIC1-BE-001 (Calendar Sync) 완료"

postconditions:
  - "사용자는 자신의 타임존, 업무시간, 공휴일을 설정할 수 있다."
  - "다른 타임존의 사용자와 일정을 조율할 때 자동으로 변환된다."
  - "업무시간 외 또는 공휴일 예약 시도 시 경고가 표시된다."
  - "포커스 블록 시간대에는 미팅 예약이 차단된다."

dependencies: ["EPIC1-BE-001"]

parallelizable: false
estimated_effort: "L"
priority: "Must"
agent_profile: ["backend"]

risk_notes:
  - "DST 전환 시점의 경계 케이스 처리 주의"
  - "공휴일 API 장애 시 Fallback 데이터 필요"
  - "정책이 복잡해질수록 성능 저하 가능 (캐싱 및 최적화 필요)"
```

