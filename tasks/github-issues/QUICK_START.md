# 🚀 Quick Start Guide

GitHub Issues를 활용한 MVP 개발 빠른 시작 가이드입니다.

> **📢 참고**: EPIC-0 FE 이슈들(#004~#006, #008~#009)은 **별도 프론트엔드 프로젝트에서 완료**되어  
> GitHub Issue 발행 대상에서 제외됩니다. BE/Infra 중심으로 진행합니다.

---

## 📋 전체 이슈 한눈에 보기 (BE 중심)

### Level 0: Foundation
```
┌─────────────────────────────────────────────────────────┐
│  🟢 #001 AWS Infra    ✅ #004 FE Layout (완료)          │
│       ↓                    ↓                            │
│  🟡 #002 OAuth        ✅ #005 Schedule UI (완료)        │
│  🟡 #003 CI/CD        ✅ #006 Dashboard (완료)          │
└─────────────────────────────────────────────────────────┘
```

### Level 2: Primary Features (병렬)
```
┌─────────────────────────────────────────────────────────┐
│  🟠 #007 Logging      ✅ #008 Summary UI (완료)         │
│  🟠 #010 Calendar ⭐   ✅ #009 Billing UI (완료)         │
│  🟠 #011 LLM                                           │
└─────────────────────────────────────────────────────────┘
⭐ = Critical Path
```

### Level 3-5: Backend & Billing
```
┌─────────────────────────────────────────────────────────┐
│  🔵 #012 Policy ⭐     🔵 #013 Focus      🔵 #014 Time  │
│       ↓                                        ↓       │
│  🟣 #016 Slot ⭐       🔵 #015 Summary    🟣 #017 Invoice│
│                                                ↓       │
│                                          ⚫ #018 Payment│
└─────────────────────────────────────────────────────────┘
```

---

## ⏱️ 예상 일정 (BE 중심, FE 완료됨)

| 팀 크기 | 예상 기간 | 주당 작업량 |
|---------|----------|------------|
| 1인 (BE) | 6-8주 | 40시간 |
| 2인 (BE×2) | 4-5주 | 40시간/인 |
| 3인 (BE×2+DevOps/AI) | 3-4주 | 40시간/인 |

---

## 🏃 첫 주 권장 작업

### Day 1-2: 환경 설정
```bash
# 1. 저장소 클론
git clone <repository-url>
cd bizplan

# 2. Docker 환경 시작
docker compose up -d

# 3. 로컬 개발 환경 확인
./gradlew bootRun --args='--spring.profiles.active=local'
```

### Day 3-5: 첫 번째 이슈들
- **DevOps/BE**: #001 (AWS 인프라) 착수
- ~~**FE**: #004 (레이아웃) 착수~~ → **완료됨** (별도 프로젝트)

---

## 📊 Effort 가이드

| Size | 예상 시간 | 예시 |
|------|----------|------|
| S | 1-2일 | FE 단순 페이지 |
| M | 3-4일 | CI/CD, 로깅 |
| L | 5-7일 | OAuth, 정책 엔진 |
| XL | 8-10일 | Calendar Sync, Slot 알고리즘 |

---

## 🚨 Critical Path 체크리스트 (BE 중심)

MVP 완료를 위해 **반드시** 순차 완료해야 하는 이슈들:

- [ ] #001 AWS 인프라
- [ ] #002 OAuth 인증
- [ ] #010 Calendar 동기화
- [ ] #012 정책 엔진
- [ ] #016 슬롯 계산 알고리즘

**이 경로가 지연되면 전체 프로젝트가 지연됩니다!**

---

## 📊 발행 대상 이슈 (13개)

| 이슈 | 설명 | Effort |
|------|------|--------|
| #001 | AWS 인프라 | L |
| #002 | OAuth 인증 | L |
| #003 | CI/CD | M |
| #007 | 로깅/모니터링 | M |
| #010 | Calendar Sync | XL |
| #011 | LLM 파이프라인 | XL |
| #012 | 정책 엔진 | L |
| #013 | 포커스 블록 | L |
| #014 | 시간 기록 | XL |
| #015 | 요약 저장 | L |
| #016 | 슬롯 알고리즘 | XL |
| #017 | 인보이스 | XL |
| #018 | 결제 연동 | L |

---

## 🔧 GitHub Issue 생성 방법

### 옵션 A: 스크립트 사용
```bash
# GitHub CLI 설치 필요
./create_issues.sh
```

### 옵션 B: 수동 생성
1. GitHub 저장소 → Issues → New Issue
2. 해당 `.md` 파일 내용 복사
3. 라벨, 마일스톤 설정

### 옵션 C: GitHub API
```bash
gh issue create \
  --title "[INFRA] AWS 인프라 및 데이터베이스 환경 구축" \
  --body-file 001_EPIC4-SYS-001_Infrastructure.md \
  --label "infra,aws,priority:must,size:L" \
  --milestone "MVP-Phase-0-Infrastructure"
```

---

## 📌 권장 라벨

### Priority
- `priority:must` - 필수
- `priority:should` - 권장
- `priority:could` - 선택

### Size
- `size:S` - 1-2일
- `size:M` - 3-4일
- `size:L` - 5-7일
- `size:XL` - 8일+

### Domain
- `frontend`
- `backend`
- `ai`
- `devops`
- `infra`

### Special
- `critical-path` - 최우선 처리
- `blocked` - 차단됨

---

## 📞 도움이 필요할 때

1. **의존성 확인**: `ISSUE_EXECUTION_ORDER.md` 참조
2. **기술 상세**: 각 이슈 파일의 "구현 가이드" 섹션 참조
3. **원본 명세**: `tasks/functional/` 또는 `tasks/non-functional/` 참조

---

## ✅ 완료 체크리스트

각 이슈 완료 시:
1. [ ] 모든 Acceptance Criteria 충족
2. [ ] 코드 리뷰 완료
3. [ ] 테스트 통과
4. [ ] 문서 업데이트 (필요시)
5. [ ] GitHub Issue 닫기
6. [ ] 후행 작업 담당자에게 알림

---

---

**최종 업데이트**: 2025-12-01  
**FE 상태**: 별도 프로젝트에서 완료됨

**Happy Coding! 🎉**
