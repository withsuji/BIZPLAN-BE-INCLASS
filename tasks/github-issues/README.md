# GitHub Issues - MVP Task Management

## 📋 개요
본 디렉토리는 `docs/MVP_Task_WBS_and_DAG.md`의 의존성 그래프를 기반으로 생성된 GitHub Issue 문서들을 포함합니다.

---

## 📂 파일 구조

```
github-issues/
├── README.md                        # 본 파일
├── ISSUE_EXECUTION_GUIDE.md         # 이슈 수행 순서 및 병렬 개발 가이드
│
├── 001_EPIC4-SYS-001_Infrastructure.md  ✅ 발행 필요
├── 002_EPIC4-SYS-002_OAuth.md           ✅ 발행 필요
├── ~~003_EPIC0-FE-001_Layout.md~~       ❌ 완료됨 (발행 불필요)
├── 004_EPIC4-SYS-003_CICD.md            ✅ 발행 필요
├── ~~005_EPIC0-FE-002_Schedule.md~~     ❌ 완료됨 (발행 불필요)
├── ~~006_EPIC0-FE-003_Dashboard.md~~    ❌ 완료됨 (발행 불필요)
├── 007_EPIC4-NFR-001_Logging.md         ✅ 발행 필요
├── ~~008_EPIC0-FE-004_Summary.md~~      ❌ 완료됨 (발행 불필요)
├── ~~009_EPIC0-FE-005_Billing.md~~      ❌ 완료됨 (발행 불필요)
├── 010_EPIC1-BE-001_Calendar_Sync.md    ✅ 발행 필요
├── 011_EPIC1-BE-002_Policy.md           ✅ 발행 필요
├── 012_EPIC2-BE-001_Focus.md            ✅ 발행 필요
├── 013_EPIC3-BE-001_Time_Tracking.md    ✅ 발행 필요
├── 014_EPIC2-AI-001_LLM.md              ✅ 발행 필요
├── 015_EPIC1-BE-003_Slot.md             ✅ 발행 필요
├── 016_EPIC2-BE-002_Summary_Storage.md  ✅ 발행 필요
├── 017_EPIC3-BE-002_Invoice.md          ✅ 발행 필요
└── 018_EPIC3-BE-003_Payment.md          ✅ 발행 필요
```

**발행 필요한 이슈**: 13개 (001, 002, 004, 007, 010~018)

---

## 🎯 이슈 목록 (전체 18개, 활성 13개)

### ~~EPIC-0: FE PoC Prototype (5개)~~ ✅ **완료됨**
> 별도 프로젝트에서 완수되어 GitHub Issue 생성 불필요

| Issue | Title | Size | Status |
|-------|-------|------|--------|
| ~~#003~~ | ~~공통 레이아웃 및 내비게이션 구현~~ | S | ✅ 완료 |
| ~~#005~~ | ~~스케줄링 링크 및 예약 페이지 UI~~ | M | ✅ 완료 |
| ~~#006~~ | ~~대시보드 및 캘린더 메인 뷰 UI~~ | M | ✅ 완료 |
| ~~#008~~ | ~~회의 요약 및 액션 아이템 뷰 UI~~ | S | ✅ 완료 |
| ~~#009~~ | ~~시간 기록 및 인보이스 관리 UI~~ | M | ✅ 완료 |

### EPIC-1: Calendar Core Service (3개)
| Issue | Title | Size | Dependencies |
|-------|-------|------|--------------|
| #010 | Google/Outlook Calendar 양방향 동기화 | XL | #001, #002 |
| #011 | 타임존 정규화 및 업무시간/공휴일 정책 엔진 | L | #010 |
| #015 | 가용 슬롯 계산 알고리즘 및 최적 시간대 제안 | XL | #011 |

### EPIC-2: Focus & AI Service (3개)
| Issue | Title | Size | Dependencies |
|-------|-------|------|--------------|
| #012 | 포커스 블록 생성 및 동적 차단 규칙 | L | #010 |
| #014 | 회의 녹취/노트 LLM 요약 파이프라인 | XL | #001 |
| #016 | LLM 요약 결과 파싱 및 구조화 저장 | L | #014 |

### EPIC-3: Billing & Time Tracking (3개)
| Issue | Title | Size | Dependencies |
|-------|-------|------|--------------|
| #013 | 캘린더 이벤트 기반 자동 시간 기록 | XL | #010 |
| #017 | 인보이스 자동 생성 및 상태 관리 | XL | #013 |
| #018 | Stripe 결제 게이트웨이 연동 및 Webhook | L | #017 |

### EPIC-4: System & NFR (4개)
| Issue | Title | Size | Dependencies |
|-------|-------|------|--------------|
| #001 | AWS 인프라 및 데이터베이스 환경 구축 | L | - |
| #002 | OAuth 2.0 인증 서버 구성 및 JWT 핸들링 | L | #001 |
| #004 | CI/CD 파이프라인 구성 (GitHub Actions) | M | #001 |
| #007 | 구조화 로깅 및 모니터링 설정 | M | #001 |

---

## 🔗 주요 의존성 체인

### Critical Path (가장 긴 경로)
```
#001 → #002 → #010 → #011 → #015
 (L)    (L)    (XL)    (L)    (XL)
```
**예상 소요 시간**: 25-30일

### Calendar Core Path
```
#001 → #002 → #010 → #011 → #015
                  ↓
                #012 (Focus Blocks)
                  ↓
                #013 (Time Tracking) → #017 (Invoice) → #018 (Payment)
```

### AI Path
```
#001 → #014 (LLM Pipeline) → #016 (Summary Storage)
```

### ~~FE Path~~ ✅ **완료됨**
```
✅ #003 → ✅ #005 (Schedule UI)
       → ✅ #006 (Dashboard UI) → ✅ #008 (Summary UI)
                                → ✅ #009 (Billing UI)
```
> FE PoC는 별도 프로젝트에서 완료되었으므로 백엔드 API 연동만 남음

---

## 📊 통계

- **총 이슈 수**: 18개
- **완료된 이슈**: 5개 (EPIC-0 전체)
- **발행 필요 이슈**: 13개
- **Size 분포** (발행 필요):
  - S (Small): 0개
  - M (Medium): 2개 (#004, #007)
  - L (Large): 7개 (#001, #002, #011, #012, #016, #017, #018)
  - XL (Extra Large): 4개 (#010, #013, #014, #015)
- **Epic 분포** (발행 필요):
  - ~~EPIC-0 (FE PoC): 5개~~ ✅ 완료
  - EPIC-1 (Calendar): 3개
  - EPIC-2 (Focus & AI): 3개
  - EPIC-3 (Billing): 3개
  - EPIC-4 (System): 4개

---

## 🚀 GitHub Issue 생성 방법

> ⚠️ **주의**: EPIC-0 (FE PoC) 이슈들(#003, #005, #006, #008, #009)은 이미 완료되어 생성 불필요

### 방법 1: 수동 생성
각 Markdown 파일의 내용을 복사하여 GitHub Issues에 직접 생성합니다.
**EPIC-0 파일들(003, 005, 006, 008, 009)은 제외하세요.**

### 방법 2: GitHub CLI 사용 (권장)
```bash
# gh CLI 설치 및 로그인
gh auth login

# EPIC-0 제외하고 이슈 생성
cd tasks/github-issues

# 백엔드 및 인프라 이슈만 생성 (EPIC-0 제외)
for file in 001_*.md 002_*.md 004_*.md 007_*.md 010_*.md 011_*.md 012_*.md 013_*.md 014_*.md 015_*.md 016_*.md 017_*.md 018_*.md; do
  [ -f "$file" ] || continue
  title=$(grep "^title:" $file | sed 's/title: //' | tr -d '"[]')
  labels=$(grep "^labels:" $file | sed 's/labels: //' | tr -d '[]"' | tr ',' ' ')
  gh issue create --title "$title" --body-file "$file" --label "$labels"
done
```

### 방법 3: GitHub API 사용
Python 스크립트 등으로 자동화 가능 (별도 스크립트 작성 필요)

---

## 📖 사용 가이드

### 1. 이슈 수행 순서 확인
`ISSUE_EXECUTION_GUIDE.md` 문서를 참고하여 작업 순서와 병렬 가능 작업을 확인합니다.

### 2. 이슈 시작 전 체크리스트
- [ ] 선행 작업(Dependencies) 완료 확인
- [ ] 필요한 리소스 및 권한 확보
- [ ] 팀원과 병렬 작업 조율

### 3. 이슈 진행 중
- [ ] "In Progress" 상태로 변경
- [ ] 진행 상황을 Issue Comment로 업데이트
- [ ] 블로커 발생 시 즉시 공유

### 4. 이슈 완료 시
- [ ] 완료 조건(Completion Criteria) 모두 충족
- [ ] 코드 리뷰 완료
- [ ] 테스트 통과
- [ ] 문서 업데이트 (필요 시)
- [ ] Issue Close

---

## ⚠️ 주의사항

1. **의존성 준수**: Dependencies가 완료되기 전에 작업 시작 금지
2. **Critical Path 우선**: #001 → #002 → #010 → #011 → #015 경로 최우선 처리
3. **인터페이스 합의**: 병렬 작업 시 API 인터페이스를 먼저 합의
4. **Mock 활용**: FE는 Mock API로 먼저 개발, BE 완성 후 연동

---

## 📞 문의 및 지원
- 의존성 관련 질문: 프로젝트 매니저에게 문의
- 기술적 질문: 해당 Epic 리드에게 문의
- 문서 업데이트: Pull Request 제출

---

**문서 버전**: v1.0  
**최종 업데이트**: 2025-11-26  
**작성자**: AI Agent

