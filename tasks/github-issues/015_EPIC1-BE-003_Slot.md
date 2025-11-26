---
issue_number: 015
epic: EPIC-1
task_id: EPIC1-BE-003
title: "[BE] 가용 슬롯 계산 알고리즘 및 최적 시간대 제안"
labels: ["backend", "calendar", "algorithm", "priority:must"]
assignees: []
milestone: "MVP-Phase-1-Core"
dependencies: ["#011"]
parallel_group: "group-4-slot"
estimated_effort: "XL"
---

# Issue #015: 가용 슬롯 계산 알고리즘 및 최적 시간대 제안

## 📋 개요
여러 참석자의 제약 조건을 고려하여 미팅 가능한 시간대를 계산하고, 우선순위 스코어링을 통해 최적의 슬롯을 제안한다.

## 🎯 목표
- 정확한 가용 슬롯 계산
- 최적 시간대 제안 (스코어링)
- 빠른 응답 시간 (< 500ms)

## 📝 상세 작업 내역

### 1. Slot Calculator
- [ ] SlotCalculatorService 클래스 생성
- [ ] `calculateAvailableSlots` 메서드 구현 (메인 알고리즘)
- [ ] `scoreSlot` 메서드 구현 (스코어링 로직)
- [ ] `mergeBusyPeriods` 유틸리티 (겹치는 시간대 병합)
- [ ] `splitIntoSlots` 유틸리티 (연속 시간을 슬롯으로 분할)

### 2. 스코어링 기준
- [ ] 선호 시간대 매치 (+50점)
- [ ] 업무시간 중앙 (+30점)
- [ ] 버퍼 타임 확보 (+20점)
- [ ] 참석자 타임존 분산 (-20점)
- [ ] 금요일 오후 (-10점)
- [ ] 이른 아침/늦은 저녁 (-30점)

### 3. API 엔드포인트
- [ ] GET `/api/v1/schedule/slots` 구현
- [ ] Query Parameter 검증:
  - `start`, `end` (날짜 범위)
  - `attendees[]` (참석자 목록)
  - `duration` (미팅 소요 시간)
  - `timezone` (타임존)
- [ ] SlotDTO 및 SlotScoreDTO 정의

### 4. 성능 최적화
- [ ] Redis 캐싱 적용 (캐시 키: attendees + date_range)
- [ ] 병렬 처리 (참석자별 이벤트 조회)
- [ ] DB 인덱싱 (시간 범위 쿼리)
- [ ] JMH Benchmark 테스트

### 5. 테스트
- [ ] 단위 테스트 (알고리즘 정확도)
- [ ] 성능 테스트 (500ms 이하)
- [ ] 경계 케이스 테스트

## ✅ 완료 조건
- [ ] 외부 사용자가 스케줄링 링크로 가용 슬롯 조회 가능
- [ ] 가용 슬롯이 우선순위 순으로 정렬되어 반환
- [ ] 캐싱으로 반복 조회 시 응답 시간 50ms 이하
- [ ] 참석자 5명, 2주 범위에서 500ms 이하 응답

## 📚 참고 문서
- SRS: `docs/GPT-SRS-v02.md` (Section 3.3 API Overview)
- Task Spec: `tasks/functional/EPIC1-BE-003_Slot_Algorithm.md`
- REQ-IDs: REQ-FUNC-003

## 🔗 의존성
**선행 작업**: #011 (Policy Engine)
**후행 작업**: #005 (Schedule UI - API 연동)
**병렬 가능**: #016 (Summary Storage)

## ⚠️ 주의사항
- 참석자 수가 많을수록 조합 폭발로 성능 저하 (병렬 처리 필수)
- 복잡한 정책이 추가될수록 알고리즘 복잡도 증가
- 타임존 변환 과정에서 경계 케이스 버그 발생 가능

## 🏷️ 라벨
`backend`, `calendar`, `algorithm`, `performance`, `priority:must`, `size:XL`

