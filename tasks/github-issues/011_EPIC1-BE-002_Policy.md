---
issue_number: 011
epic: EPIC-1
task_id: EPIC1-BE-002
title: "[BE] 타임존 정규화 및 업무시간/공휴일 정책 엔진"
labels: ["backend", "calendar", "timezone", "policy", "priority:must"]
assignees: []
milestone: "MVP-Phase-1-Core"
dependencies: ["#010"]
parallel_group: "group-3-calendar"
estimated_effort: "L"
---

# Issue #011: 타임존 정규화 및 업무시간/공휴일 정책 엔진

## 📋 개요
다양한 타임존의 사용자 간 일정 조율을 위한 타임존 정규화 로직과 업무시간, 공휴일 정책 엔진을 구현한다.

## 🎯 목표
- 정확한 타임존 변환
- 업무시간 및 공휴일 정책 적용
- 충돌 감지 및 검증

## 📝 상세 작업 내역

### 1. 타임존 정규화
- [ ] TimezoneService 구현 (UTC <-> User TZ 변환)
- [ ] IANA Timezone DB 활용
- [ ] DST Handling (`java.time.ZonedDateTime`)
- [ ] Timezone Validation

### 2. 업무시간 정책
- [ ] UserPolicy Entity 생성
- [ ] WorkingHoursService 구현
- [ ] 요일별 업무시간 설정
- [ ] Break Time, Buffer Time 관리

### 3. 공휴일 관리
- [ ] Holiday, UserHoliday Entity 생성
- [ ] HolidayService 구현
- [ ] Calendarific API Client 구현
- [ ] 공휴일 데이터 캐싱 (Redis, 연 단위)

### 4. 정책 검증 엔진
- [ ] PolicyEngine 구현
- [ ] TimeOverlapChecker (충돌 감지 알고리즘)
- [ ] Policy Violation DTO 정의
- [ ] 위반 심각도 (ERROR | WARNING | INFO)

### 5. 테스트
- [ ] 타임존 경계 케이스 테스트
- [ ] DST 전환 시점 테스트
- [ ] 단위 테스트 작성

## ✅ 완료 조건
- [ ] 사용자가 타임존, 업무시간, 공휴일 설정 가능
- [ ] 다른 타임존 사용자와 일정 조율 시 자동 변환
- [ ] 업무시간 외 또는 공휴일 예약 시 경고 표시
- [ ] 포커스 블록 시간대에 미팅 예약 차단

## 📚 참고 문서
- SRS: `docs/GPT-SRS-v02.md` (Section 3.4 Interaction Sequences)
- Task Spec: `tasks/functional/EPIC1-BE-002_Policy_Engine.md`
- REQ-IDs: REQ-FUNC-001, REQ-FUNC-002

## 🔗 의존성
**선행 작업**: #010 (Calendar Sync)
**후행 작업**: #015 (Slot Algorithm)
**병렬 가능**: #012 (Focus Blocks), #013 (Time Tracking)

## ⚠️ 주의사항
- DST 전환 시점 경계 케이스 처리 주의
- 공휴일 API 장애 시 Fallback 데이터 필요
- 정책 복잡도에 따른 성능 저하 가능 (캐싱 필수)

## 🏷️ 라벨
`backend`, `calendar`, `timezone`, `policy`, `priority:must`, `size:L`

