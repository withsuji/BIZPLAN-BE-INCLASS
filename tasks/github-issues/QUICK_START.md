# GitHub Issue 빠른 발행 가이드

## ✅ 발행 체크리스트

### 완료된 이슈 (발행 불필요)
- [ ] ~~#003~~ EPIC0-FE-001 (Layout) ✅ 완료
- [ ] ~~#005~~ EPIC0-FE-002 (Schedule) ✅ 완료
- [ ] ~~#006~~ EPIC0-FE-003 (Dashboard) ✅ 완료
- [ ] ~~#008~~ EPIC0-FE-004 (Summary) ✅ 완료
- [ ] ~~#009~~ EPIC0-FE-005 (Billing) ✅ 완료

### 발행 필요한 이슈 (13개)

#### Phase 0: Infrastructure (4개)
- [ ] #001 EPIC4-SYS-001 (Infrastructure) - 시작점
- [ ] #002 EPIC4-SYS-002 (OAuth) - #001 의존
- [ ] #004 EPIC4-SYS-003 (CI/CD) - #001 의존
- [ ] #007 EPIC4-NFR-001 (Logging) - #001 의존

#### Phase 1: Calendar Core (3개)
- [ ] #010 EPIC1-BE-001 (Calendar Sync) - #001, #002 의존 **Critical Path**
- [ ] #011 EPIC1-BE-002 (Policy Engine) - #010 의존 **Critical Path**
- [ ] #015 EPIC1-BE-003 (Slot Algorithm) - #011 의존 **Critical Path**

#### Phase 2: Advanced Features (3개)
- [ ] #012 EPIC2-BE-001 (Focus Blocks) - #010 의존
- [ ] #014 EPIC2-AI-001 (LLM Pipeline) - #001 의존
- [ ] #016 EPIC2-BE-002 (Summary Storage) - #014 의존

#### Phase 3: Billing (3개)
- [ ] #013 EPIC3-BE-001 (Time Tracking) - #010 의존
- [ ] #017 EPIC3-BE-002 (Invoice Generation) - #013 의존
- [ ] #018 EPIC3-BE-003 (Payment Gateway) - #017 의존

---

## 🚀 GitHub CLI로 일괄 발행

```bash
#!/bin/bash
# 발행 필요한 이슈만 생성하는 스크립트

cd tasks/github-issues

# 발행할 이슈 목록 (EPIC-0 제외)
ISSUES=(
  "001_EPIC4-SYS-001_Infrastructure.md"
  "002_EPIC4-SYS-002_OAuth.md"
  "004_EPIC4-SYS-003_CICD.md"
  "007_EPIC4-NFR-001_Logging.md"
  "010_EPIC1-BE-001_Calendar_Sync.md"
  "011_EPIC1-BE-002_Policy.md"
  "012_EPIC2-BE-001_Focus.md"
  "013_EPIC3-BE-001_Time_Tracking.md"
  "014_EPIC2-AI-001_LLM.md"
  "015_EPIC1-BE-003_Slot.md"
  "016_EPIC2-BE-002_Summary_Storage.md"
  "017_EPIC3-BE-002_Invoice.md"
  "018_EPIC3-BE-003_Payment.md"
)

for file in "${ISSUES[@]}"; do
  if [ ! -f "$file" ]; then
    echo "⚠️  파일을 찾을 수 없습니다: $file"
    continue
  fi
  
  echo "📝 이슈 생성 중: $file"
  
  # 제목 추출
  title=$(grep "^title:" "$file" | sed 's/title: //' | tr -d '"[]')
  
  # 라벨 추출
  labels=$(grep "^labels:" "$file" | sed 's/labels: //' | tr -d '[]"' | sed 's/, /,/g')
  
  # 마일스톤 추출
  milestone=$(grep "^milestone:" "$file" | sed 's/milestone: //' | tr -d '"')
  
  # 이슈 생성
  gh issue create \
    --title "$title" \
    --body-file "$file" \
    --label "$labels" \
    --milestone "$milestone"
  
  echo "✅ 완료: $title"
  echo ""
done

echo "🎉 총 ${#ISSUES[@]}개 이슈 발행 완료!"
```

### 스크립트 사용법

```bash
# 1. 실행 권한 부여
chmod +x create_issues.sh

# 2. GitHub CLI 로그인 (최초 1회)
gh auth login

# 3. 이슈 발행
./create_issues.sh
```

---

## 📌 단계별 수동 발행 (권장)

자동화 스크립트보다는 단계별로 검토하며 발행하는 것을 권장합니다.

### Step 1: Infrastructure 발행 (4개)
```bash
gh issue create --title "[INFRA] AWS 인프라 및 데이터베이스 환경 구축" \
  --body-file 001_EPIC4-SYS-001_Infrastructure.md \
  --label "infra,database,terraform,aws,priority:must" \
  --milestone "MVP-Phase-0-Infrastructure"

gh issue create --title "[AUTH] OAuth 2.0 인증 서버 구성 및 JWT 핸들링" \
  --body-file 002_EPIC4-SYS-002_OAuth.md \
  --label "backend,auth,oauth,jwt,priority:must" \
  --milestone "MVP-Phase-0-Infrastructure"

gh issue create --title "[DevOps] CI/CD 파이프라인 구성 (GitHub Actions)" \
  --body-file 004_EPIC4-SYS-003_CICD.md \
  --label "devops,cicd,github-actions,priority:must" \
  --milestone "MVP-Phase-0-Infrastructure"

gh issue create --title "[NFR] 구조화 로깅 및 모니터링 설정" \
  --body-file 007_EPIC4-NFR-001_Logging.md \
  --label "backend,nfr,logging,monitoring,cloudwatch,priority:must" \
  --milestone "MVP-Phase-1-Core"
```

### Step 2: Calendar Core 발행 (3개) - #001, #002 완료 후
```bash
gh issue create --title "[BE] Google/Outlook Calendar 양방향 동기화 및 Webhook" \
  --body-file 010_EPIC1-BE-001_Calendar_Sync.md \
  --label "backend,calendar,integration,webhook,priority:must" \
  --milestone "MVP-Phase-1-Core"

# #010 완료 후
gh issue create --title "[BE] 타임존 정규화 및 업무시간/공휴일 정책 엔진" \
  --body-file 011_EPIC1-BE-002_Policy.md \
  --label "backend,calendar,timezone,policy,priority:must" \
  --milestone "MVP-Phase-1-Core"

# #011 완료 후
gh issue create --title "[BE] 가용 슬롯 계산 알고리즘 및 최적 시간대 제안" \
  --body-file 015_EPIC1-BE-003_Slot.md \
  --label "backend,calendar,algorithm,priority:must" \
  --milestone "MVP-Phase-1-Core"
```

### Step 3: Advanced & Billing 발행 (6개) - #010 완료 후
```bash
# Focus & AI (병렬 가능)
gh issue create --title "[BE] 포커스 블록 생성 및 동적 차단 규칙" \
  --body-file 012_EPIC2-BE-001_Focus.md \
  --label "backend,focus,dnd,priority:must" \
  --milestone "MVP-Phase-2-Advanced"

gh issue create --title "[AI] 회의 녹취/노트 LLM 요약 파이프라인" \
  --body-file 014_EPIC2-AI-001_LLM.md \
  --label "ai,llm,python,langchain,priority:must" \
  --milestone "MVP-Phase-2-Advanced"

# #014 완료 후
gh issue create --title "[BE] LLM 요약 결과 파싱 및 구조화 저장" \
  --body-file 016_EPIC2-BE-002_Summary_Storage.md \
  --label "backend,meeting,summary,priority:must" \
  --milestone "MVP-Phase-2-Advanced"

# Time Tracking & Billing (순차)
gh issue create --title "[BE] 캘린더 이벤트 기반 자동 시간 기록" \
  --body-file 013_EPIC3-BE-001_Time_Tracking.md \
  --label "backend,billing,time-tracking,priority:must" \
  --milestone "MVP-Phase-2-Billing"

# #013 완료 후
gh issue create --title "[BE] 인보이스 자동 생성 및 상태 관리" \
  --body-file 017_EPIC3-BE-002_Invoice.md \
  --label "backend,billing,invoice,priority:must" \
  --milestone "MVP-Phase-2-Billing"

# #017 완료 후
gh issue create --title "[BE] Stripe 결제 게이트웨이 연동 및 Webhook 처리" \
  --body-file 018_EPIC3-BE-003_Payment.md \
  --label "backend,billing,payment,stripe,priority:must" \
  --milestone "MVP-Phase-2-Billing"
```

---

## 🎯 발행 우선순위

### 1순위 (즉시 발행)
- #001 (Infrastructure) - 모든 작업의 기반
- #002 (OAuth) - 인증 시스템

### 2순위 (#001, #002 완료 후)
- #004 (CI/CD)
- #007 (Logging)
- #010 (Calendar Sync) - **Critical Path**

### 3순위 (#010 완료 후)
- #011 (Policy Engine) - **Critical Path**
- #012 (Focus Blocks)
- #013 (Time Tracking)
- #014 (LLM Pipeline)

### 4순위 (#011 완료 후)
- #015 (Slot Algorithm) - **Critical Path**
- #016 (Summary Storage)
- #017 (Invoice Generation)

### 5순위 (마지막)
- #018 (Payment Gateway)

---

## ⚠️ 주의사항

1. **EPIC-0 이슈(#003, #005, #006, #008, #009)는 절대 발행하지 마세요!**
2. **의존성을 준수**하여 순서대로 발행하세요.
3. **Critical Path(#001→#002→#010→#011→#015)**는 최우선 처리하세요.
4. 발행 전에 각 이슈의 내용을 검토하세요.
5. 마일스톤과 라벨을 정확히 설정하세요.

---

**문서 버전**: v1.0  
**최종 업데이트**: 2025-11-26

