---
issue_number: 004
epic: EPIC-4
task_id: EPIC4-SYS-003
title: "[DevOps] CI/CD 파이프라인 구성 (GitHub Actions)"
labels: ["devops", "cicd", "github-actions", "priority:must"]
assignees: []
milestone: "MVP-Phase-0-Infrastructure"
dependencies: ["#001"]
parallel_group: "group-1-support"
estimated_effort: "M"
---

# Issue #004: CI/CD 파이프라인 구성 (GitHub Actions)

## 📋 개요
GitHub Actions를 통해 자동화된 빌드, 테스트, 배포 파이프라인을 구축한다.

## 🎯 목표
- 안정적이고 자동화된 배포 프로세스
- 개발/프로덕션 환경 분리
- 빠른 피드백 루프

## 📝 상세 작업 내역

### 1. CI (Continuous Integration)
- [ ] `.github/workflows/ci.yml` 생성
- [ ] PR 시 자동 빌드/테스트 실행
- [ ] Gradle 빌드 설정
- [ ] JUnit 테스트 자동 실행
- [ ] JaCoCo 코드 커버리지 측정

### 2. CD (Continuous Deployment)
- [ ] `.github/workflows/deploy-dev.yml` 생성 (develop 브랜치)
- [ ] `.github/workflows/deploy-prod.yml` 생성 (main 브랜치)
- [ ] Docker 멀티 스테이지 빌드 작성
- [ ] ECR 이미지 푸시 설정
- [ ] ECS Task Definition 업데이트
- [ ] Health Check 확인 로직

### 3. 환경 설정
- [ ] GitHub Secrets 등록 (AWS Credentials, DB 정보 등)
- [ ] 환경별 설정 분리 (dev, staging, prod)
- [ ] Slack Webhook 연동 (알림)

### 4. Deployment Scripts
- [ ] ECS 배포 스크립트 작성
- [ ] Rollback 스크립트 작성
- [ ] Health Check Endpoint 구현 (`/actuator/health`)

## ✅ 완료 조건
- [ ] PR 생성 시 자동으로 빌드/테스트 실행
- [ ] develop 브랜치 푸시 시 Dev 환경에 자동 배포
- [ ] main 브랜치 푸시 시 승인 후 Prod 환경에 배포
- [ ] 배포 상태가 Slack으로 통보

## 📚 참고 문서
- SRS: `docs/GPT-SRS-v02.md` (Section 3.2 Deployment)
- Task Spec: `tasks/non-functional/EPIC4-SYS-003_CI_CD_Pipeline.md`
- REQ-IDs: REQ-NF-005, REQ-NF-011

## 🔗 의존성
**선행 작업**: #001 (ECS Cluster 필요)
**후행 작업**: 모든 백엔드 작업에 영향
**병렬 가능**: #002 (OAuth), #007 (Logging)

## ⚠️ 주의사항
- 프로덕션 배포는 수동 승인 단계 포함
- 민감한 정보는 GitHub Secrets에만 저장
- ECS 배포 중 다운타임 최소화 (Blue-Green 전략)

## 🏷️ 라벨
`devops`, `cicd`, `github-actions`, `docker`, `priority:must`, `size:M`

