# EPIC2-AI-001: 회의 녹취/노트 수신 및 LLM 프롬프트 파이프라인 (LangChain)

## 1. 개요 및 목적
회의 종료 후 녹취록 또는 노트를 입력받아 LLM(Large Language Model)을 통해 회의 요약, 결정 사항, 액션 아이템을 자동으로 추출하는 AI 파이프라인을 구축한다.

## 2. 상세 요구사항

### 2.1. 입력 소스
- **녹취록 (Transcript)**: 
  - Zoom, Google Meet, MS Teams 등의 녹취 파일
  - 텍스트 파일 (.txt, .vtt, .srt) 지원
  - 최대 파일 크기: 10MB
- **노트 (Notes)**: 
  - 사용자가 직접 입력한 회의 노트 (Markdown 지원)
  - 최대 길이: 50,000자
- **언어 지원**: 
  - 한국어, 영어 우선 지원
  - 자동 언어 감지

### 2.2. LLM 프롬프트 파이프라인
- **아키텍처**: Python FastAPI + LangChain
- **LLM Gateway 연동**: 
  - 사내 LLM Gateway를 통한 Google Gemini 호출
  - OpenAI API 호환 인터페이스
- **프롬프트 구조**:
  ```
  System: 당신은 회의 요약 전문가입니다.
  
  User: 다음 회의 녹취록을 분석하여 JSON 형식으로 요약해주세요:
  - summary: 한 줄 요약
  - key_decisions: 결정된 사항 목록
  - action_items: 액션 아이템 목록 (담당자, 기한 포함)
  - participants: 참석자 목록
  
  Transcript:
  {transcript_text}
  ```
- **프롬프트 템플릿**: 
  - 회의 유형별 템플릿 (정기 회의, 브레인스토밍, 1:1 등)
  - 산업/도메인별 커스터마이징 가능

### 2.3. 출력 구조화
- **JSON Schema 검증**: 
  - LLM 출력을 Pydantic Model로 검증
  - 필수 필드 누락 시 재시도
- **출력 예시**:
  ```json
  {
    "summary": "Q4 마케팅 전략 회의, 예산 증액 및 채널 다변화 결정",
    "key_decisions": [
      "마케팅 예산 20% 증액 승인",
      "인플루언서 마케팅 채널 추가"
    ],
    "action_items": [
      {
        "text": "예산안 작성 및 기안",
        "owner": "김철수",
        "due_date": "2025-12-01"
      },
      {
        "text": "인플루언서 대행사 리스트업",
        "owner": "이영희",
        "due_date": "2025-11-30"
      }
    ],
    "participants": ["김철수", "이영희", "박민수"],
    "confidence_score": 0.95
  }
  ```

### 2.4. 비동기 처리 및 재시도
- **비동기 Job Queue**: 
  - Celery + Redis 또는 AWS SQS
  - 요약 요청은 즉시 202 Accepted 반환, Job ID 발급
- **재시도 전략**: 
  - LLM 호출 실패 시 지수 백오프(최대 5회)
  - 타임아웃: 60초
- **상태 추적**: 
  - Job Status: PENDING | PROCESSING | COMPLETED | FAILED
  - Webhook 또는 Polling으로 결과 조회

### 2.5. 품질 관리
- **Confidence Score**: 
  - LLM이 생성한 요약의 신뢰도 점수 (0.0 ~ 1.0)
  - 0.7 이하일 경우 사용자에게 검토 요청
- **Human-in-the-Loop**: 
  - 사용자가 요약 결과를 수정할 수 있는 UI 제공
  - 수정 데이터를 Fine-tuning에 활용 (향후)

## 3. Task Definition (YAML)

```yaml
task_id: "EPIC2-AI-001"
title: "회의 녹취/노트 LLM 요약 파이프라인 구현 (LangChain)"
summary: >
  회의 녹취록을 입력받아 LLM을 통해 요약, 결정사항, 액션아이템을 추출하는
  Python 기반 AI 파이프라인을 구축한다.
type: "functional"

epic: "EPIC_2_FOCUS_AI"
req_ids: ["REQ-FUNC-011"]
component: ["ai.llm-orchestrator", "ai.summary-service"]

context:
  srs_section: "3.1 External Systems (LLM Gateway)"
  tech_stack: ["Python", "FastAPI", "LangChain", "Celery", "Redis", "Pydantic"]

requirements:
  description: "정확하고 구조화된 회의 요약 자동 생성"
  kpis:
    - "요약 생성 성공률 95% 이상"
    - "요약 정확도: 사용자 만족도 80% 이상"
    - "요약 생성 시간 < 30초 (5,000자 기준)"
    - "Confidence Score 평균 0.85 이상"

design_constraints:
  - "LLM 호출은 반드시 사내 LLM Gateway를 통해 수행"
  - "개인정보(PII)는 LLM에 전송 전 마스킹 처리"
  - "프롬프트는 외부 파일 또는 DB에 저장하여 버전 관리"

steps_hint:
  - "Python FastAPI 프로젝트 생성 (llm-orchestrator)"
  - "LangChain 설치 및 LLM Gateway Client 구현"
  - "프롬프트 템플릿 정의 (PromptTemplate)"
  - "Pydantic Model 정의 (SummaryOutput)"
  - "SummaryService 구현 (LLM 호출 및 파싱)"
  - "Celery Task 구현 (비동기 처리)"
  - "POST /api/v1/meetings/{id}/summary API 구현"
  - "GET /api/v1/meetings/{id}/summary/status API 구현"
  - "PII Masking 유틸리티 구현 (정규표현식 기반)"
  - "에러 핸들링 및 재시도 로직 구현"
  - "로깅 및 모니터링 (LLM 호출 비용 추적)"
  - "단위 테스트 및 E2E 테스트 작성"

preconditions:
  - "EPIC4-SYS-001 (Infra) 완료"
  - "사내 LLM Gateway 접근 권한 확보"
  - "Celery + Redis 환경 구성"

postconditions:
  - "회의 녹취록을 입력하면 30초 이내에 요약이 생성된다."
  - "요약 결과가 JSON 형식으로 반환된다."
  - "신뢰도가 낮은 경우 경고가 표시된다."
  - "비동기 처리 상태를 조회할 수 있다."

dependencies: ["EPIC4-SYS-001"]

parallelizable: true
estimated_effort: "XL"
priority: "Must"
agent_profile: ["ai", "backend"]

risk_notes:
  - "LLM 출력이 불안정하여 JSON 파싱 실패 가능 (재시도 및 폴백 필요)"
  - "프롬프트 엔지니어링이 품질에 큰 영향 (지속적인 개선 필요)"
  - "LLM 호출 비용이 높을 수 있음 (캐싱 및 배치 처리 고려)"
  - "한국어 처리 품질이 영어보다 낮을 수 있음 (모델 선택 중요)"
```

