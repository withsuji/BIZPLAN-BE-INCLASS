#!/bin/bash

# GitHub Issues 자동 생성 스크립트
# EPIC-0 (FE PoC)는 제외하고 백엔드/인프라 이슈만 생성

set -e  # 에러 발생 시 중단

echo "🚀 GitHub Issues 생성 시작..."
echo ""

# 현재 디렉토리 확인
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
cd "$SCRIPT_DIR"

# GitHub CLI 인증 확인
if ! gh auth status &>/dev/null; then
    echo "❌ GitHub CLI 인증이 필요합니다."
    echo "다음 명령어로 먼저 로그인해주세요:"
    echo "  gh auth login"
    exit 1
fi

echo "✅ GitHub CLI 인증 확인 완료"
echo ""

# 발행할 이슈 목록 (EPIC-0 제외)
declare -a ISSUES=(
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

TOTAL=${#ISSUES[@]}
CREATED=0
FAILED=0

echo "📋 발행할 이슈: $TOTAL 개"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""

for file in "${ISSUES[@]}"; do
    if [ ! -f "$file" ]; then
        echo "⚠️  파일을 찾을 수 없습니다: $file"
        ((FAILED++))
        continue
    fi
    
    echo "📝 처리 중: $file"
    
    # Frontmatter에서 정보 추출
    title=$(grep "^title:" "$file" | head -1 | sed 's/title: //' | tr -d '"' | sed 's/\[/\\[/g' | sed 's/\]/\\]/g')
    labels=$(grep "^labels:" "$file" | head -1 | sed 's/labels: //' | tr -d '[]"' | sed 's/, */,/g')
    milestone=$(grep "^milestone:" "$file" | head -1 | sed 's/milestone: //' | tr -d '"')
    
    if [ -z "$title" ]; then
        echo "  ❌ 제목을 추출할 수 없습니다."
        ((FAILED++))
        continue
    fi
    
    echo "  제목: $title"
    echo "  라벨: $labels"
    echo "  마일스톤: $milestone"
    
    # 이슈 생성
    if gh issue create \
        --title "$title" \
        --body-file "$file" \
        --label "$labels" \
        ${milestone:+--milestone "$milestone"} 2>&1; then
        echo "  ✅ 생성 완료!"
        ((CREATED++))
    else
        echo "  ❌ 생성 실패"
        ((FAILED++))
    fi
    
    echo ""
    
    # API Rate Limit 방지를 위한 대기
    sleep 2
done

echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "🎉 이슈 생성 완료!"
echo ""
echo "📊 결과:"
echo "  - 총 이슈: $TOTAL 개"
echo "  - 생성 성공: $CREATED 개"
echo "  - 생성 실패: $FAILED 개"
echo ""

if [ $FAILED -eq 0 ]; then
    echo "✅ 모든 이슈가 성공적으로 생성되었습니다!"
else
    echo "⚠️  일부 이슈 생성에 실패했습니다. 위의 로그를 확인해주세요."
fi

echo ""
echo "🔗 이슈 확인: https://github.com/withsuji/BIZPLAN-BE-INCLASS/issues"



