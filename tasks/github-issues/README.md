# GitHub Issues 관리 문서

## 📋 개요

이 디렉토리는 `docs/MVP_Task_WBS_and_DAG.md`에 정의된 **의존성 그래프(DAG)**를 기반으로 작성된 GitHub Issue 문서들을 포함합니다.

각 이슈는 **작업 순서(토폴로지 정렬)**에 맞게 번호가 부여되어 있으며, 선후행 관계와 병렬 개발 가능 여부가 명확하게 정의되어 있습니다.

---

## 📁 파일 구조

```
tasks/github-issues/
├── README.md                      # 본 문서
├── ISSUE_EXECUTION_ORDER.md       # ⭐ 이슈 실행 순서 및 병렬 개발 가이드
├── QUICK_START.md                 # 빠른 시작 가이드
├── create_issues.sh               # GitHub Issue 자동 생성 스크립트
│
├── 001_EPIC4-SYS-001_Infrastructure.md    # Level 0
├── 002_EPIC4-SYS-002_OAuth.md             # Level 0
├── 003_EPIC4-SYS-003_CICD.md              # Level 0
├── 004_EPIC0-FE-001_Layout.md             # Level 0
│
├── 005_EPIC0-FE-002_Schedule.md           # Level 1
├── 006_EPIC0-FE-003_Dashboard.md          # Level 1
│
├── 007_EPIC4-NFR-001_Logging.md           # Level 2
├── 008_EPIC0-FE-004_Summary.md            # Level 2
├── 009_EPIC0-FE-005_Billing.md            # Level 2
├── 010_EPIC1-BE-001_Calendar_Sync.md      # Level 2 (Critical Path)
├── 011_EPIC2-AI-001_LLM.md                # Level 2
│
├── 012_EPIC1-BE-002_Policy.md             # Level 3 (Critical Path)
├── 013_EPIC2-BE-001_Focus.md              # Level 3
├── 014_EPIC3-BE-001_Time_Tracking.md      # Level 3
├── 015_EPIC2-BE-002_Summary_Storage.md    # Level 3
│
├── 016_EPIC1-BE-003_Slot.md               # Level 4 (Critical Path)
├── 017_EPIC3-BE-002_Invoice.md            # Level 4
│
└── 018_EPIC3-BE-003_Payment.md            # Level 5
```

---

## 📊 이슈 목록

> **📢 참고**: EPIC-0 FE 이슈들(#004~#006, #008~#009)은 **별도 프론트엔드 프로젝트에서 완료**되어  
> GitHub Issue 발행 대상에서 제외됩니다.

| Issue # | Epic | Task ID | Title | Effort | Dependencies | 상태 |
|---------|------|---------|-------|--------|--------------|------|
| #001 | EPIC-4 | SYS-001 | AWS 인프라 및 DB 구축 | L | - | 📋 발행 대상 |
| #002 | EPIC-4 | SYS-002 | OAuth 2.0 인증 구현 | L | #001 | 📋 발행 대상 |
| #003 | EPIC-4 | SYS-003 | CI/CD 파이프라인 구성 | M | #001 | 📋 발행 대상 |
| ~~#004~~ | EPIC-0 | FE-001 | 공통 레이아웃 및 내비게이션 | S | - | ✅ **완료** |
| ~~#005~~ | EPIC-0 | FE-002 | 스케줄링 예약 페이지 UI | M | #004 | ✅ **완료** |
| ~~#006~~ | EPIC-0 | FE-003 | 대시보드 및 캘린더 뷰 | M | #004 | ✅ **완료** |
| #007 | EPIC-4 | NFR-001 | 구조화 로깅 및 모니터링 | M | #001 | 📋 발행 대상 |
| ~~#008~~ | EPIC-0 | FE-004 | 회의 요약 및 액션 아이템 뷰 | S | #006 | ✅ **완료** |
| ~~#009~~ | EPIC-0 | FE-005 | 시간 기록 및 인보이스 UI | M | #006 | ✅ **완료** |
| #010 | EPIC-1 | BE-001 | Calendar 양방향 동기화 | XL | #001, #002 | 📋 발행 대상 |
| #011 | EPIC-2 | AI-001 | LLM 요약 파이프라인 | XL | #001 | 📋 발행 대상 |
| #012 | EPIC-1 | BE-002 | 정책 엔진 (타임존/업무시간) | L | #010 | 📋 발행 대상 |
| #013 | EPIC-2 | BE-001 | 포커스 블록 및 차단 규칙 | L | #010 | 📋 발행 대상 |
| #014 | EPIC-3 | BE-001 | 자동 시간 기록 | XL | #010 | 📋 발행 대상 |
| #015 | EPIC-2 | BE-002 | LLM 요약 저장 및 Action Item | L | #011 | 📋 발행 대상 |
| #016 | EPIC-1 | BE-003 | 가용 슬롯 계산 알고리즘 | XL | #012 | 📋 발행 대상 |
| #017 | EPIC-3 | BE-002 | 인보이스 자동 생성 | XL | #014 | 📋 발행 대상 |
| #018 | EPIC-3 | BE-003 | Stripe 결제 연동 | L | #017 | 📋 발행 대상 |

---

## 🚨 Critical Path

MVP 완료까지의 **최장 경로**입니다. 이 경로의 지연은 전체 프로젝트 지연으로 이어집니다.

```
#001 → #002 → #010 → #012 → #016
  L       L      XL      L      XL    = ~31일
```

**해당 이슈들은 최우선 처리가 필요합니다.**

---

## 🔄 병렬 개발 그룹 (BE 중심)

> **참고**: FE 그룹은 별도 프로젝트에서 완료되어 제외됨

| 그룹 | 이슈들 | 병렬 가능 조건 | 상태 |
|------|--------|---------------|------|
| group-0 | #001 | 인프라 기반 | 📋 발행 대상 |
| ~~group-0-fe~~ | ~~#004~~ | ~~FE 기반~~ | ✅ 완료 |
| ~~group-1-fe~~ | ~~#005, #006~~ | ~~둘 다 #004 의존~~ | ✅ 완료 |
| group-2-be | #007, #010, #011 | 모두 #001 의존 | 📋 발행 대상 |
| ~~group-2-fe~~ | ~~#008, #009~~ | ~~둘 다 #006 의존~~ | ✅ 완료 |
| group-3 | #012, #013, #014 | 모두 #010 의존 | 📋 발행 대상 |
| group-4 | #016, #017 | 서로 다른 경로 | 📋 발행 대상 |

---

## 📌 사용법

### 1. 이슈 실행 순서 확인
```bash
# 상세 실행 순서 및 병렬 개발 가이드
cat ISSUE_EXECUTION_ORDER.md
```

### 2. GitHub Issue 자동 생성
```bash
# GitHub CLI 필요
chmod +x create_issues.sh
./create_issues.sh
```

### 3. 개별 이슈 확인
각 `NNN_*.md` 파일에는 다음 정보가 포함되어 있습니다:
- YAML Front Matter (메타데이터)
- 상세 요구사항
- 구현 가이드
- 완료 조건 (Acceptance Criteria)
- 의존성 정보

---

## 📚 관련 문서

- **WBS & DAG**: `docs/MVP_Task_WBS_and_DAG.md`
- **작업 명세 (Functional)**: `tasks/functional/`
- **작업 명세 (Non-Functional)**: `tasks/non-functional/`
- **SRS 문서**: `docs/GPT-SRS-v02.md`

---

## ⚠️ 주의사항

1. **순서 준수**: 이슈 번호 순서는 DAG 의존성을 반영합니다.
2. **의존성 확인**: 이슈 시작 전 `dependencies` 필드의 모든 이슈가 완료되었는지 확인하세요.
3. **병렬 개발**: `parallel_group`이 같은 이슈들은 동시 진행 가능합니다.
4. **Critical Path**: `critical-path` 라벨이 있는 이슈는 최우선 처리하세요.
5. **FE 이슈 제외**: EPIC-0 FE 이슈들(`skip_github_issue: true`)은 별도 프로젝트에서 완료되어 GitHub Issue 발행에서 제외됩니다.

---

## 📈 발행 현황

| 구분 | 개수 | 비고 |
|------|------|------|
| 총 이슈 문서 | 18개 | |
| 발행 대상 | **13개** | BE/Infra/AI |
| 완료 (제외) | 5개 | FE - 별도 프로젝트 |

---

**최종 업데이트**: 2025-12-01  
**기반 문서**: `docs/MVP_Task_WBS_and_DAG.md`
