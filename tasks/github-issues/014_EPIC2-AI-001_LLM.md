---
issue_number: 014
epic: EPIC-2
task_id: EPIC2-AI-001
title: "[AI] 회의 녹취/노트 LLM 요약 파이프라인"
labels: ["ai", "llm", "python", "langchain", "priority:must"]
assignees: []
milestone: "MVP-Phase-2-Advanced"
dependencies: ["#001"]
parallel_group: "group-3-advanced"
estimated_effort: "XL"
---

# Issue #014: 회의 녹취/노트 LLM 요약 파이프라인

## 📋 개요
회의 녹취록을 입력받아 LLM을 통해 요약, 결정사항, 액션아이템을 추출하는 Python 기반 AI 파이프라인을 구축한다.

## 🎯 목표
- LLM 기반 회의 요약 자동 생성
- 구조화된 출력 (JSON)
- 비동기 처리

## 📝 상세 작업 내역

### 1. Python FastAPI 프로젝트
- [ ] llm-orchestrator 프로젝트 생성
- [ ] LangChain, FastAPI, Celery, Redis, Pydantic 설치
- [ ] 프로젝트 구조 설정

### 2. LLM 연동
- [ ] LLM Gateway Client 구현 (OpenAI 호환 인터페이스)
- [ ] 프롬프트 템플릿 정의 (`PromptTemplate`)
- [ ] Pydantic Model 정의 (`SummaryOutput`)
- [ ] PII Masking 유틸리티 구현

### 3. Summary Service
- [ ] SummaryService 구현 (LLM 호출 및 파싱)
- [ ] Celery Task 구현 (비동기 처리)
- [ ] JSON Schema 검증
- [ ] Confidence Score 계산

### 4. API 엔드포인트
- [ ] POST `/api/v1/meetings/{id}/summary` (요약 생성 요청)
- [ ] GET `/api/v1/meetings/{id}/summary/status` (상태 조회)
- [ ] Webhook Callback (Java Backend로 결과 전송)

### 5. 에러 핸들링 및 재시도
- [ ] 지수 백오프 재시도 (최대 5회)
- [ ] 타임아웃 설정 (60초)
- [ ] 에러 로깅 및 모니터링
- [ ] LLM 호출 비용 추적

### 6. 테스트
- [ ] 단위 테스트 작성
- [ ] E2E 테스트 작성
- [ ] Mock LLM Response 테스트

## ✅ 완료 조건
- [ ] 회의 녹취록 입력 시 30초 이내에 요약 생성
- [ ] 요약 결과가 JSON 형식으로 반환
- [ ] 신뢰도가 낮은 경우 경고 표시
- [ ] 비동기 처리 상태 조회 가능

## 📚 참고 문서
- SRS: `docs/GPT-SRS-v02.md` (Section 3.1 External Systems - LLM Gateway)
- Task Spec: `tasks/functional/EPIC2-AI-001_LLM_Pipeline.md`
- REQ-IDs: REQ-FUNC-011

## 🔗 의존성
**선행 작업**: #001 (Infra - Celery + Redis 환경)
**후행 작업**: #016 (Summary Storage)
**병렬 가능**: #010 (Calendar Sync), #012 (Focus Blocks), #013 (Time Tracking)

## ⚠️ 주의사항
- LLM 출력이 불안정하여 JSON 파싱 실패 가능 (재시도 필수)
- 프롬프트 엔지니어링이 품질에 큰 영향
- LLM 호출 비용이 높을 수 있음 (캐싱 고려)
- 한국어 처리 품질이 영어보다 낮을 수 있음

## 🏷️ 라벨
`ai`, `llm`, `python`, `langchain`, `fastapi`, `priority:must`, `size:XL`

