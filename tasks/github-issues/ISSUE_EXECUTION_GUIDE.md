# GitHub Issue 수행 순서 및 병렬 개발 가이드

## 📋 문서 개요
본 문서는 `docs/MVP_Task_WBS_and_DAG.md`에 정의된 의존성 그래프(DAG)를 기반으로, GitHub Issue의 수행 순서와 병렬 개발 가능 작업을 명시합니다.

> ✅ **업데이트 (2025-11-26)**: EPIC-0 (FE PoC) 작업들은 별도 프로젝트에서 완료되었습니다. 본 가이드는 남은 백엔드 및 인프라 작업(13개)에 집중합니다.

---

## 🎯 이슈 실행 전략
1. **병렬 그룹(Parallel Group)**: 동일한 그룹 내 작업들은 병렬로 진행 가능
2. **의존성(Dependencies)**: 선행 작업이 완료되어야 시작 가능
3. **마일스톤(Milestone)**: 개발 단계별 그룹핑

---

## 📊 이슈 수행 순서 (레벨별)

### Level 0: Foundation (병렬 시작 가능)
> **병렬 그룹**: `group-0-foundation`  
> **의존성**: 없음

| Issue # | Task ID | Title | Estimated Effort | Status |
|---------|---------|-------|------------------|--------|
| #001 | EPIC4-SYS-001 | AWS 인프라 및 데이터베이스 환경 구축 | L | 🔴 TODO |
| #002 | EPIC4-SYS-002 | OAuth 2.0 인증 서버 구성 및 JWT 핸들링 | L | 🔴 TODO (depends on #001) |
| ~~#003~~ | ~~EPIC0-FE-001~~ | ~~공통 레이아웃 및 내비게이션 구현~~ | S | ✅ 완료 |

**진행 방법**:
- #001부터 시작 (FE는 이미 완료)
- #002는 #001 완료 후 시작

---

### Level 1: Support Systems
> **병렬 그룹**: `group-1-support`  
> **의존성**: Level 0 완료

| Issue # | Task ID | Title | Estimated Effort | Dependencies | Status |
|---------|---------|-------|------------------|--------------|--------|
| #004 | EPIC4-SYS-003 | CI/CD 파이프라인 구성 (GitHub Actions) | M | #001 | 🔴 TODO |
| ~~#005~~ | ~~EPIC0-FE-002~~ | ~~스케줄링 링크 및 예약 페이지 UI~~ | M | ~~#003~~ | ✅ 완료 |
| ~~#006~~ | ~~EPIC0-FE-003~~ | ~~대시보드 및 캘린더 메인 뷰 UI~~ | M | ~~#003~~ | ✅ 완료 |

**진행 방법**:
- #004는 #001 완료 후 시작 (FE는 이미 완료되어 DevOps만 진행)

---

### Level 2: Core Features (병렬 진행 가능)
> **병렬 그룹**: `group-2-core`  
> **의존성**: Level 0, 1 완료

| Issue # | Task ID | Title | Estimated Effort | Dependencies | Status |
|---------|---------|-------|------------------|--------------|--------|
| #007 | EPIC4-NFR-001 | 구조화 로깅 및 모니터링 설정 | M | #001 | 🔴 TODO |
| ~~#008~~ | ~~EPIC0-FE-004~~ | ~~회의 요약 및 액션 아이템 뷰 UI~~ | S | ~~#006~~ | ✅ 완료 |
| ~~#009~~ | ~~EPIC0-FE-005~~ | ~~시간 기록 및 인보이스 관리 UI~~ | M | ~~#006~~ | ✅ 완료 |
| #010 | EPIC1-BE-001 | Google/Outlook Calendar 양방향 동기화 | XL | #001, #002 | 🔴 TODO |

**진행 방법**:
- #007은 #001 완료 후 시작 (백엔드 작업과 병렬 가능)
- #010은 #001, #002 완료 후 시작 (**Critical Path**)
- #007 ↔ #010은 병렬 진행 가능 (FE는 이미 완료)

---

### Level 3: Advanced Features (병렬 진행 가능)
> **병렬 그룹**: `group-3-calendar`, `group-3-advanced`  
> **의존성**: #010 (Calendar Sync) 완료 필수

| Issue # | Task ID | Title | Estimated Effort | Dependencies |
|---------|---------|-------|------------------|--------------|
| #011 | EPIC1-BE-002 | 타임존 정규화 및 업무시간/공휴일 정책 엔진 | L | #010 |
| #012 | EPIC2-BE-001 | 포커스 블록 생성 및 동적 차단 규칙 | L | #010 |
| #013 | EPIC3-BE-001 | 캘린더 이벤트 기반 자동 시간 기록 | XL | #010 |
| #014 | EPIC2-AI-001 | 회의 녹취/노트 LLM 요약 파이프라인 | XL | #001 |

**진행 방법**:
- #010 완료 후 #011, #012, #013 병렬 시작 가능
- #014는 #001만 필요하므로 더 일찍 시작 가능 (#010과 병렬)
- **권장**: #011 → #015 경로가 Critical Path이므로 우선 처리

---

### Level 4: Optimization & Integration (병렬 진행 가능)
> **병렬 그룹**: `group-4-slot`, `group-4-summary`  
> **의존성**: Level 3 일부 완료

| Issue # | Task ID | Title | Estimated Effort | Dependencies |
|---------|---------|-------|------------------|--------------|
| #015 | EPIC1-BE-003 | 가용 슬롯 계산 알고리즘 및 최적 시간대 제안 | XL | #011 |
| #016 | EPIC2-BE-002 | LLM 요약 결과 파싱 및 구조화 저장 | L | #014 |

**진행 방법**:
- #015는 #011 완료 후 시작
- #016은 #014 완료 후 시작
- #015와 #016은 서로 병렬 진행 가능

---

### Level 5: Billing System (순차 진행)
> **병렬 그룹**: `group-5-billing`, `group-6-payment`  
> **의존성**: #013 (Time Tracking) 완료 필수

| Issue # | Task ID | Title | Estimated Effort | Dependencies |
|---------|---------|-------|------------------|--------------|
| #017 | EPIC3-BE-002 | 인보이스 자동 생성 및 상태 관리 | XL | #013 |
| #018 | EPIC3-BE-003 | Stripe 결제 게이트웨이 연동 및 Webhook 처리 | L | #017 |

**진행 방법**:
- #017은 #013 완료 후 시작
- #018은 #017 완료 후 시작 (순차 진행)
- 다른 작업들과는 병렬 진행 가능

---

## 🔄 Critical Path (가장 긴 경로)

```
#001 → #002 → #010 → #011 → #015
  (L)    (L)    (XL)    (L)    (XL)

총 예상 소요 시간: ~25-30일 (L=5일, XL=8일 가정)
```

**Critical Path 작업 우선 처리 권장**

---

## 🚀 병렬 개발 시나리오

### Scenario 1: 팀 크기 - 2명 (BE 1, DevOps 1)
> FE는 이미 완료되어 백엔드 중심으로 재구성

**Week 1-2**:
- BE: #001, #002 → #010 시작
- DevOps: #001 → #004, #007

**Week 3-4**:
- BE: #010 (계속) → #011, #012
- DevOps: 인프라 모니터링 및 #010 지원

**Week 5-6**:
- BE: #011 → #015 (Critical Path), #013, #014 병렬
- DevOps: 성능 최적화 지원, DB 튜닝

**Week 7-8**:
- BE: #016, #017, #018 순차 진행
- DevOps: 배포 자동화, 모니터링 고도화

---

### Scenario 2: 팀 크기 - 3명 (BE 2, DevOps/AI 1)
> FE는 이미 완료되어 백엔드 중심으로 재구성

**Week 1-2**:
- BE1: #001, #002 → #010 시작
- BE2: #001 지원 → #014 (AI 파이프라인)
- DevOps: #001, #004, #007

**Week 3-4**:
- BE1: #010 (계속) → #011 → #015 (Critical Path)
- BE2: #014 (계속) → #016
- DevOps: #012 (Focus) 지원

**Week 5-6**:
- BE1: #015 완료 → #013 → #017
- BE2: #016 완료 → #017 지원
- DevOps: 성능 테스트 및 모니터링 강화

**Week 7-8**:
- BE1: #017 → #018
- BE2: 통합 테스트, 버그 픽스, API 문서화
- DevOps: 프로덕션 배포 준비, FE-BE 통합 지원

---

## 📌 병렬 개발 원칙

### ✅ 병렬 진행 가능한 조건
1. **의존성이 없는 작업**: 서로 다른 Epic이나 컴포넌트
2. **인터페이스가 정의된 경우**: Mock API로 FE/BE 분리 개발
3. **다른 도메인**: Calendar ↔ Billing, FE ↔ BE

### ❌ 병렬 진행 불가능한 조건
1. **직접 의존성**: A → B (B는 A 완료 후에만 시작)
2. **동일 코드베이스 충돌**: 같은 파일/모듈 수정
3. **Critical Path**: #010 → #011 → #015 (순차 진행 권장)

---

## 🎯 마일스톤별 완료 기준

### Milestone 1: MVP-Phase-0 (Infrastructure)
- **완료 조건**: #001, #002, #004, #007 완료
- **기간**: 2주
- **목표**: 인프라 구축 + 인증 시스템 (FE는 이미 완료)

### Milestone 2: MVP-Phase-1 (Core Calendar Features)
- **완료 조건**: #010, #011, #015 완료
- **기간**: 3-4주
- **목표**: 캘린더 동기화 및 슬롯 계산 엔진

### Milestone 3: MVP-Phase-2 (Advanced & Billing)
- **완료 조건**: #012-#018 완료
- **기간**: 3-4주
- **목표**: 포커스 블록, AI 요약, 청구 시스템

**전체 MVP 예상 기간**: 6-8주 (FE 완료로 2-3주 단축)

---

## 📊 이슈 상태 추적 방법

### GitHub Project Board 구성 제안
```
Backlog | Ready | In Progress | Review | Done
  #001    #002      #003        #004    #005
  #006    #007      #010        #011    #012
  ...
```

### 라벨 활용
- `priority:must`: 필수 작업
- `size:S|M|L|XL`: 작업 크기
- `parallel-group-N`: 병렬 그룹 식별
- `epic-N`: Epic 그룹핑
- `frontend|backend|ai|devops`: 담당 팀

---

## ⚠️ 주의사항 및 권장사항

### 1. Critical Path 우선 처리
- #001 → #002 → #010 → #011 → #015 경로는 최우선 처리
- 이 경로가 지연되면 전체 프로젝트가 지연됨

### 2. FE-BE 연동 준비
- ✅ FE는 이미 Mock API로 완료됨
- BE API 완성 시 API 문서 우선 작성 (OpenAPI/Swagger)
- FE 팀과 API 인터페이스 검증 및 통합 테스트 수행

### 3. 통합 테스트 타이밍
- Phase 0 완료 후: 인프라 및 인증 통합 테스트
- Phase 1 완료 후: Calendar Core API 완성 → FE 연동 테스트
- Phase 2 완료 후: 전체 E2E 테스트 (FE + BE 통합)

### 4. 주간 동기화
- 매주 Sprint Planning에서 병렬 작업 조율
- 의존성 있는 작업은 인터페이스 먼저 합의

---

## 📞 문의 및 업데이트
- 의존성 변경 시 본 문서 업데이트 필수
- 작업 순서 조정 시 팀 전체 공유
- Critical Path 지연 시 즉시 에스컬레이션

---

**문서 버전**: v1.0  
**최종 업데이트**: 2025-11-26  
**작성자**: AI Agent

