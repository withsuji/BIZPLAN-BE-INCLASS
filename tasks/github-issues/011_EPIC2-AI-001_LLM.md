---
issue_number: 011
epic: EPIC-2
task_id: EPIC2-AI-001
title: "[AI] 회의 녹취/노트 LLM 요약 파이프라인"
labels: ["ai", "llm", "python", "fastapi", "langchain", "priority:must", "size:XL"]
assignees: []
milestone: "MVP-Phase-1-Core-Features"
dependencies: ["#001"]
parallel_group: "group-2-ai"
estimated_effort: "XL"
difficulty: "상"
estimated_duration: "8일"
---

# Issue #011: 회의 녹취/노트 LLM 요약 파이프라인

## 📋 개요
회의 종료 후 녹취록 또는 노트를 입력받아 LLM(Large Language Model)을 통해 회의 요약, 결정 사항, 액션 아이템을 자동으로 추출하는 AI 파이프라인을 구축한다.

## 🎯 목표
- Python FastAPI 기반 LLM 서비스 구축
- LangChain을 활용한 프롬프트 파이프라인
- 비동기 작업 처리 (Celery)
- 구조화된 JSON 출력

---

## 📝 상세 요구사항

### 1. 입력 소스

#### 지원 형식
- **녹취록 (Transcript)**: `.txt`, `.vtt`, `.srt`
- **노트 (Notes)**: 일반 텍스트, Markdown
- **최대 크기**: 10MB, 50,000자

#### 언어 지원
- 한국어, 영어 (자동 감지)

### 2. LLM 파이프라인

#### 아키텍처
```
┌─────────────┐    ┌─────────────┐    ┌─────────────┐
│   FastAPI   │───▶│  LangChain  │───▶│ LLM Gateway │
│   (API)     │    │  (Prompt)   │    │  (Gemini)   │
└─────────────┘    └─────────────┘    └─────────────┘
       │                                     │
       ▼                                     ▼
┌─────────────┐                      ┌─────────────┐
│   Celery    │                      │  Pydantic   │
│   (Queue)   │                      │  (Output)   │
└─────────────┘                      └─────────────┘
```

#### 프롬프트 구조
```python
SYSTEM_PROMPT = """당신은 회의 요약 전문가입니다. 
주어진 회의 녹취록을 분석하여 핵심 내용을 추출합니다.
반드시 JSON 형식으로 응답해야 합니다."""

USER_PROMPT = """다음 회의 녹취록을 분석하여 JSON 형식으로 요약해주세요:

출력 형식:
{
  "summary": "한 줄 요약 (50자 이내)",
  "key_decisions": ["결정사항1", "결정사항2"],
  "action_items": [
    {"text": "액션 내용", "owner": "담당자명", "due_date": "YYYY-MM-DD 또는 null"}
  ],
  "participants": ["참석자1", "참석자2"],
  "confidence_score": 0.0~1.0
}

녹취록:
{transcript}
"""
```

### 3. 출력 구조

```python
from pydantic import BaseModel
from typing import List, Optional

class ActionItem(BaseModel):
    text: str
    owner: Optional[str] = None
    due_date: Optional[str] = None

class SummaryOutput(BaseModel):
    summary: str
    key_decisions: List[str]
    action_items: List[ActionItem]
    participants: List[str]
    confidence_score: float
```

### 4. 비동기 처리

#### Job Status
- `PENDING`: 대기 중
- `PROCESSING`: 처리 중
- `COMPLETED`: 완료
- `FAILED`: 실패

#### API Flow
1. `POST /api/v1/meetings/{id}/summary` → 202 Accepted + Job ID
2. `GET /api/v1/meetings/{id}/summary/status` → Job Status
3. `GET /api/v1/meetings/{id}/summary` → 결과 조회

---

## 🔧 구현 가이드

### Phase 1: 프로젝트 설정 (Day 1)

```bash
# 프로젝트 생성
mkdir ai-service && cd ai-service
python -m venv venv
source venv/bin/activate

# 의존성 설치
pip install fastapi uvicorn langchain langchain-google-genai
pip install celery redis pydantic python-dotenv
pip install pytest httpx
```

#### requirements.txt
```
fastapi==0.109.0
uvicorn==0.27.0
langchain==0.1.0
langchain-google-genai==0.0.6
celery==5.3.6
redis==5.0.1
pydantic==2.5.3
python-dotenv==1.0.0
httpx==0.26.0
pytest==7.4.4
```

#### 프로젝트 구조
```
ai-service/
├── app/
│   ├── __init__.py
│   ├── main.py
│   ├── config.py
│   ├── api/
│   │   ├── __init__.py
│   │   └── routes/
│   │       └── summary.py
│   ├── core/
│   │   ├── __init__.py
│   │   └── llm_client.py
│   ├── models/
│   │   ├── __init__.py
│   │   └── summary.py
│   ├── services/
│   │   ├── __init__.py
│   │   └── summary_service.py
│   └── tasks/
│       ├── __init__.py
│       └── celery_app.py
├── prompts/
│   └── meeting_summary.txt
├── tests/
│   └── test_summary.py
├── requirements.txt
├── Dockerfile
└── docker-compose.yml
```

### Phase 2: LLM Client 구현 (Day 2-3)

#### app/core/llm_client.py
```python
from langchain_google_genai import ChatGoogleGenerativeAI
from langchain.prompts import ChatPromptTemplate
from langchain.output_parsers import PydanticOutputParser
from app.models.summary import SummaryOutput
from app.config import settings
import logging

logger = logging.getLogger(__name__)

class LLMClient:
    def __init__(self):
        self.llm = ChatGoogleGenerativeAI(
            model="gemini-pro",
            google_api_key=settings.LLM_GATEWAY_API_KEY,
            temperature=0.3,
            max_output_tokens=2048,
        )
        self.parser = PydanticOutputParser(pydantic_object=SummaryOutput)
        
    def generate_summary(self, transcript: str, language: str = "ko") -> SummaryOutput:
        prompt = ChatPromptTemplate.from_messages([
            ("system", self._get_system_prompt(language)),
            ("human", self._get_user_prompt()),
        ])
        
        chain = prompt | self.llm | self.parser
        
        try:
            result = chain.invoke({
                "transcript": transcript,
                "format_instructions": self.parser.get_format_instructions(),
            })
            return result
        except Exception as e:
            logger.error(f"LLM generation failed: {e}")
            raise
    
    def _get_system_prompt(self, language: str) -> str:
        if language == "ko":
            return """당신은 회의 요약 전문가입니다.
주어진 회의 녹취록을 분석하여 핵심 내용을 추출합니다.
반드시 지정된 JSON 형식으로만 응답해야 합니다.
액션 아이템의 담당자는 녹취록에서 언급된 이름을 사용하고,
기한이 명시되지 않은 경우 null로 표시합니다."""
        else:
            return """You are a meeting summary expert.
Analyze the given meeting transcript and extract key information.
You must respond only in the specified JSON format."""
    
    def _get_user_prompt(self) -> str:
        return """다음 회의 녹취록을 분석하여 요약해주세요.

{format_instructions}

녹취록:
{transcript}
"""
```

### Phase 3: Service 및 Task 구현 (Day 3-4)

#### app/services/summary_service.py
```python
from app.core.llm_client import LLMClient
from app.models.summary import SummaryOutput, SummaryJob, JobStatus
import hashlib
import redis
import json
from datetime import datetime
import logging

logger = logging.getLogger(__name__)

class SummaryService:
    def __init__(self):
        self.llm_client = LLMClient()
        self.redis = redis.Redis.from_url(settings.REDIS_URL)
    
    def create_job(self, meeting_id: str, transcript: str) -> SummaryJob:
        job_id = self._generate_job_id(meeting_id)
        
        job = SummaryJob(
            id=job_id,
            meeting_id=meeting_id,
            status=JobStatus.PENDING,
            created_at=datetime.utcnow(),
        )
        
        self._save_job(job)
        
        # Celery 태스크 실행
        from app.tasks.summary_tasks import process_summary
        process_summary.delay(job_id, meeting_id, transcript)
        
        return job
    
    def get_job_status(self, job_id: str) -> SummaryJob:
        job_data = self.redis.get(f"summary_job:{job_id}")
        if not job_data:
            raise ValueError(f"Job not found: {job_id}")
        return SummaryJob(**json.loads(job_data))
    
    def get_summary(self, meeting_id: str) -> SummaryOutput:
        result_data = self.redis.get(f"summary_result:{meeting_id}")
        if not result_data:
            raise ValueError(f"Summary not found for meeting: {meeting_id}")
        return SummaryOutput(**json.loads(result_data))
    
    def process_transcript(self, job_id: str, meeting_id: str, transcript: str):
        try:
            self._update_job_status(job_id, JobStatus.PROCESSING)
            
            # PII 마스킹
            masked_transcript = self._mask_pii(transcript)
            
            # LLM 호출
            result = self.llm_client.generate_summary(masked_transcript)
            
            # 결과 저장
            self._save_result(meeting_id, result)
            self._update_job_status(job_id, JobStatus.COMPLETED)
            
            logger.info(f"Summary completed for meeting {meeting_id}")
            
        except Exception as e:
            logger.error(f"Summary failed for meeting {meeting_id}: {e}")
            self._update_job_status(job_id, JobStatus.FAILED, str(e))
            raise
    
    def _mask_pii(self, text: str) -> str:
        """이메일, 전화번호 등 PII 마스킹"""
        import re
        # 이메일 마스킹
        text = re.sub(r'[\w\.-]+@[\w\.-]+\.\w+', '[EMAIL]', text)
        # 전화번호 마스킹
        text = re.sub(r'\d{2,3}[-.\s]?\d{3,4}[-.\s]?\d{4}', '[PHONE]', text)
        return text
    
    def _generate_job_id(self, meeting_id: str) -> str:
        timestamp = datetime.utcnow().isoformat()
        return hashlib.md5(f"{meeting_id}:{timestamp}".encode()).hexdigest()[:12]
    
    def _save_job(self, job: SummaryJob):
        self.redis.setex(
            f"summary_job:{job.id}",
            3600,  # 1시간 TTL
            job.json()
        )
    
    def _update_job_status(self, job_id: str, status: JobStatus, error: str = None):
        job = self.get_job_status(job_id)
        job.status = status
        job.error = error
        if status == JobStatus.COMPLETED:
            job.completed_at = datetime.utcnow()
        self._save_job(job)
    
    def _save_result(self, meeting_id: str, result: SummaryOutput):
        self.redis.setex(
            f"summary_result:{meeting_id}",
            86400 * 7,  # 7일 TTL
            result.json()
        )
```

#### app/tasks/summary_tasks.py
```python
from celery import Celery
from app.config import settings

celery_app = Celery(
    "summary_tasks",
    broker=settings.REDIS_URL,
    backend=settings.REDIS_URL,
)

celery_app.conf.update(
    task_serializer='json',
    accept_content=['json'],
    result_serializer='json',
    timezone='UTC',
    enable_utc=True,
    task_time_limit=120,  # 2분 타임아웃
    task_soft_time_limit=90,
)

@celery_app.task(bind=True, max_retries=3)
def process_summary(self, job_id: str, meeting_id: str, transcript: str):
    from app.services.summary_service import SummaryService
    
    try:
        service = SummaryService()
        service.process_transcript(job_id, meeting_id, transcript)
    except Exception as e:
        # 지수 백오프로 재시도
        raise self.retry(exc=e, countdown=2 ** self.request.retries)
```

### Phase 4: API Routes (Day 4-5)

#### app/api/routes/summary.py
```python
from fastapi import APIRouter, HTTPException, BackgroundTasks
from pydantic import BaseModel
from app.services.summary_service import SummaryService
from app.models.summary import SummaryOutput, SummaryJob

router = APIRouter(prefix="/api/v1/meetings", tags=["summary"])

class SummaryRequest(BaseModel):
    transcript: str
    language: str = "ko"

service = SummaryService()

@router.post("/{meeting_id}/summary", status_code=202)
async def create_summary(meeting_id: str, request: SummaryRequest) -> dict:
    """회의 요약 생성 요청 (비동기)"""
    if len(request.transcript) > 50000:
        raise HTTPException(400, "Transcript too long (max 50,000 chars)")
    
    job = service.create_job(meeting_id, request.transcript)
    
    return {
        "job_id": job.id,
        "status": job.status.value,
        "message": "Summary generation started",
    }

@router.get("/{meeting_id}/summary/status")
async def get_summary_status(meeting_id: str, job_id: str) -> dict:
    """요약 작업 상태 조회"""
    try:
        job = service.get_job_status(job_id)
        return {
            "job_id": job.id,
            "status": job.status.value,
            "created_at": job.created_at.isoformat(),
            "completed_at": job.completed_at.isoformat() if job.completed_at else None,
            "error": job.error,
        }
    except ValueError as e:
        raise HTTPException(404, str(e))

@router.get("/{meeting_id}/summary")
async def get_summary(meeting_id: str) -> SummaryOutput:
    """요약 결과 조회"""
    try:
        return service.get_summary(meeting_id)
    except ValueError as e:
        raise HTTPException(404, str(e))

@router.post("/{meeting_id}/summary/regenerate", status_code=202)
async def regenerate_summary(meeting_id: str, request: SummaryRequest) -> dict:
    """요약 재생성 요청"""
    job = service.create_job(meeting_id, request.transcript)
    return {
        "job_id": job.id,
        "status": job.status.value,
        "message": "Summary regeneration started",
    }
```

### Phase 5: Main Application (Day 5-6)

#### app/main.py
```python
from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from app.api.routes import summary
from app.config import settings
import logging

logging.basicConfig(level=logging.INFO)

app = FastAPI(
    title="BizPlan AI Service",
    description="LLM-based meeting summary service",
    version="1.0.0",
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=settings.CORS_ORIGINS,
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

app.include_router(summary.router)

@app.get("/health")
async def health_check():
    return {"status": "healthy"}
```

### Phase 6: Docker 및 테스트 (Day 6-8)

#### Dockerfile
```dockerfile
FROM python:3.11-slim

WORKDIR /app

COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

COPY . .

CMD ["uvicorn", "app.main:app", "--host", "0.0.0.0", "--port", "8000"]
```

#### docker-compose.yml
```yaml
version: '3.8'

services:
  ai-service:
    build: .
    ports:
      - "8001:8000"
    environment:
      - LLM_GATEWAY_API_KEY=${LLM_GATEWAY_API_KEY}
      - REDIS_URL=redis://redis:6379/0
    depends_on:
      - redis

  celery-worker:
    build: .
    command: celery -A app.tasks.summary_tasks worker --loglevel=info
    environment:
      - LLM_GATEWAY_API_KEY=${LLM_GATEWAY_API_KEY}
      - REDIS_URL=redis://redis:6379/0
    depends_on:
      - redis

  redis:
    image: redis:7-alpine
    ports:
      - "6380:6379"
```

---

## 📁 산출물 (Artifacts)

```
ai-service/
├── app/
│   ├── main.py
│   ├── config.py
│   ├── api/routes/summary.py
│   ├── core/llm_client.py
│   ├── models/summary.py
│   ├── services/summary_service.py
│   └── tasks/summary_tasks.py
├── prompts/
│   └── meeting_summary.txt
├── tests/
│   ├── test_llm_client.py
│   └── test_summary_service.py
├── Dockerfile
├── docker-compose.yml
└── requirements.txt
```

---

## ✅ 완료 조건 (Acceptance Criteria)

| # | 조건 | 검증 방법 |
|---|------|----------|
| AC-1 | 요약 생성 API 동작 | POST 요청 시 202 + Job ID 반환 |
| AC-2 | 상태 조회 API 동작 | GET 요청 시 현재 상태 반환 |
| AC-3 | 결과 조회 API 동작 | 완료 후 GET 요청 시 요약 반환 |
| AC-4 | 요약 생성 시간 < 30초 | 5,000자 기준 |
| AC-5 | JSON 출력 준수 | Pydantic 모델 검증 통과 |
| AC-6 | PII 마스킹 | 이메일, 전화번호 마스킹 확인 |
| AC-7 | 재시도 동작 | LLM 실패 시 최대 3회 재시도 |
| AC-8 | 한국어/영어 지원 | 언어별 정상 요약 생성 |

---

## 🔗 의존성

**선행 작업**:
- #001 (AWS 인프라) - Redis 필요

**후행 작업**:
- #015 (Summary Storage) - 요약 결과 저장

**병렬 가능**:
- #007 (Logging)
- #010 (Calendar Sync)

---

## 🏷️ 라벨
`ai`, `llm`, `python`, `fastapi`, `langchain`, `priority:must`, `size:XL`, `difficulty:hard`

---

## ⚠️ 주의사항 및 리스크

| 리스크 | 영향 | 완화 방안 |
|--------|------|----------|
| LLM 출력 불안정 | JSON 파싱 실패 | 재시도 + 폴백 프롬프트 |
| API 비용 | 비용 증가 | 캐싱, 입력 길이 제한 |
| 한국어 품질 | 요약 정확도 저하 | 프롬프트 튜닝, 모델 선택 |
| 처리 시간 | 사용자 경험 저하 | 비동기 처리, 타임아웃 |

---

## 📚 참고 문서
- Task Spec: `tasks/functional/EPIC2-AI-001_LLM_Pipeline.md`
- REQ-IDs: REQ-FUNC-011
- [LangChain 문서](https://python.langchain.com/)
- [Google Generative AI](https://ai.google.dev/)



