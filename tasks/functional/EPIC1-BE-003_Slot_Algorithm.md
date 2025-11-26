# EPIC1-BE-003: 가용 슬롯 계산 알고리즘 (충돌 회피 로직)

## 1. 개요 및 목적
여러 참석자의 캘린더, 업무시간, 포커스 블록을 고려하여 미팅 가능한 시간대(슬롯)를 계산하고, 우선순위에 따라 최적의 슬롯을 제안하는 알고리즘을 구현한다.

## 2. 상세 요구사항

### 2.1. 가용 슬롯 계산
- **Input**:
  - 참석자 목록 (user_ids)
  - 원하는 기간 (start_date, end_date)
  - 미팅 소요 시간 (duration_minutes)
  - 타임존 (timezone)
- **Process**:
  1. 각 참석자의 캘린더 이벤트 조회
  2. 각 참석자의 업무시간 정책 조회
  3. 공휴일 및 휴일 조회
  4. 포커스 블록 조회
  5. 모든 제약 조건을 만족하는 시간대 추출
  6. 연속된 가용 시간을 duration_minutes 단위로 슬롯 생성
- **Output**:
  - 가용 슬롯 리스트 (start_time, end_time, score)

### 2.2. 슬롯 스코어링
각 슬롯에 대해 우선순위 점수를 계산하여 최적의 시간대를 제안한다.

**스코어링 기준**:
- **선호 시간대 매치** (+50점): 참석자의 preferred_meeting_hours 내
- **업무시간 중앙** (+30점): 업무시간의 중간 시간대 (컨텍스트 스위칭 최소화)
- **버퍼 타임 확보** (+20점): 이전/이후 미팅과 충분한 간격
- **참석자 타임존 분산** (-20점): 참석자 간 타임존 차이가 큰 경우 감점
- **금요일 오후** (-10점): 금요일 오후는 선호도 낮음
- **이른 아침/늦은 저녁** (-30점): 업무시간 경계에 가까운 시간

### 2.3. 대체 슬롯 제안
- **우선 제안**: 상위 3개 슬롯 제안
- **대체 제안**: 일부 제약 조건을 완화한 추가 슬롯 제안
  - 선택적 참석자 제외
  - 포커스 블록 이동 가능 시 포함
  - 업무시간 1시간 연장 허용

### 2.4. 성능 최적화
- **캐싱**: 동일 참석자 조합에 대한 슬롯 계산 결과 캐싱 (5분)
- **병렬 처리**: 참석자별 이벤트 조회를 병렬로 수행
- **인덱싱**: 시간 범위 쿼리 최적화를 위한 DB 인덱스 설정

### 2.5. 알고리즘 예시
```
1. Initialize available_slots = []
2. For each day in [start_date, end_date]:
   a. If day is holiday for any attendee: continue
   b. Get common working hours for all attendees (intersection)
   c. Get all events for all attendees on this day
   d. Get all focus blocks for all attendees on this day
   e. Merge events + focus blocks → busy_periods
   f. Calculate free_periods = working_hours - busy_periods
   g. For each free_period:
      - If free_period.duration >= duration_minutes + buffer:
        - Split into slots of duration_minutes
        - Calculate score for each slot
        - Add to available_slots
3. Sort available_slots by score (descending)
4. Return top 10 slots
```

## 3. Task Definition (YAML)

```yaml
task_id: "EPIC1-BE-003"
title: "가용 슬롯 계산 알고리즘 및 최적 시간대 제안 구현"
summary: >
  여러 참석자의 제약 조건을 고려하여 미팅 가능한 시간대를 계산하고,
  우선순위 스코어링을 통해 최적의 슬롯을 제안하는 엔진을 구현한다.
type: "functional"

epic: "EPIC_1_CALENDAR_CORE"
req_ids: ["REQ-FUNC-003"]
component: ["backend.calendar", "backend.slot-engine"]

context:
  srs_section: "3.3 API Overview (GET /api/v1/schedule/slots)"
  tech_stack: ["Spring Boot", "Java Streams", "Redis", "MySQL"]

requirements:
  description: "정확하고 빠른 가용 슬롯 계산 및 최적화된 제안"
  kpis:
    - "슬롯 계산 시간 < 500ms (참석자 5명 이하, 2주 범위)"
    - "슬롯 정확도 100% (false positive 0%)"
    - "스코어링 정확도: 사용자 만족도 80% 이상"

design_constraints:
  - "최대 조회 범위: 3개월"
  - "최대 참석자 수: 10명"
  - "슬롯 단위: 15분 (00, 15, 30, 45)"
  - "최소 미팅 시간: 15분, 최대: 4시간"

steps_hint:
  - "SlotCalculatorService 클래스 생성"
  - "calculateAvailableSlots 메서드 구현 (메인 알고리즘)"
  - "scoreSlot 메서드 구현 (스코어링 로직)"
  - "mergeBusyPeriods 유틸리티 메서드 (겹치는 시간대 병합)"
  - "splitIntoSlots 유틸리티 메서드 (연속 시간을 슬롯으로 분할)"
  - "SlotDTO 및 SlotScoreDTO 정의"
  - "GET /api/v1/schedule/slots API 엔드포인트 구현"
  - "Query Parameter 검증 로직 (날짜 범위, 참석자 수 등)"
  - "Redis 캐싱 적용 (캐시 키: attendees + date_range)"
  - "성능 테스트 및 최적화 (JMH Benchmark)"

preconditions:
  - "EPIC1-BE-001 (Calendar Sync) 완료"
  - "EPIC1-BE-002 (Policy Engine) 완료"

postconditions:
  - "외부 사용자가 스케줄링 링크를 통해 가용 슬롯을 조회할 수 있다."
  - "가용 슬롯이 우선순위 순으로 정렬되어 반환된다."
  - "캐싱을 통해 반복 조회 시 응답 시간이 50ms 이하로 단축된다."

dependencies: ["EPIC1-BE-002"]

parallelizable: false
estimated_effort: "XL"
priority: "Must"
agent_profile: ["backend"]

risk_notes:
  - "참석자 수가 많을수록 조합 폭발로 성능 저하 (병렬 처리 및 조기 종료 전략 필요)"
  - "복잡한 정책이 추가될수록 알고리즘 복잡도 증가"
  - "타임존 변환 과정에서 경계 케이스 버그 발생 가능"
```

