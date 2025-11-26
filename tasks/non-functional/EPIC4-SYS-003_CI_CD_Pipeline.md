# EPIC4-SYS-003: CI/CD 파이프라인 구성 (GitHub Actions)

## 1. 개요 및 목적
코드 변경 사항을 자동으로 빌드, 테스트, 배포하는 CI/CD 파이프라인을 GitHub Actions를 통해 구축한다. 개발 환경과 프로덕션 환경을 분리하여 안전한 배포 프로세스를 확립한다.

## 2. 상세 요구사항

### 2.1. CI (Continuous Integration)
- **Trigger**: PR 생성/업데이트 시 자동 실행
- **Build**: Gradle을 사용한 Spring Boot 애플리케이션 빌드
- **Test**: 단위 테스트 및 통합 테스트 자동 실행
- **Code Quality**: Checkstyle, SpotBugs 등 정적 분석 도구 실행
- **Coverage**: JaCoCo를 통한 테스트 커버리지 측정 (목표: 70% 이상)

### 2.2. CD (Continuous Deployment)
- **Dev Environment**: develop 브랜치 푸시 시 자동 배포
- **Prod Environment**: main 브랜치 푸시 또는 Release Tag 생성 시 배포
- **Docker Image**: 
  - Docker 이미지 빌드 및 ECR(Elastic Container Registry)에 푸시
  - 이미지 태그: git commit SHA + timestamp
- **ECS Deployment**: 
  - ECS Task Definition 업데이트
  - Blue-Green 또는 Rolling Update 전략 적용
  - Health Check 통과 확인 후 배포 완료

### 2.3. 환경 변수 및 시크릿 관리
- **GitHub Secrets**: AWS Credentials, DB 접속 정보 등
- **Parameter Store/Secrets Manager**: 런타임 환경 변수 관리
- **Multi-Environment**: dev, staging, prod 환경별 설정 분리

### 2.4. Notification & Monitoring
- **Slack Integration**: 빌드/배포 성공/실패 알림
- **Rollback Strategy**: 배포 실패 시 이전 버전으로 자동 롤백

## 3. Task Definition (YAML)

```yaml
task_id: "EPIC4-SYS-003"
title: "GitHub Actions 기반 CI/CD 파이프라인 구축"
summary: >
  자동화된 빌드, 테스트, 배포 파이프라인을 구성하여
  안전하고 빠른 배포 프로세스를 확립한다.
type: "non_functional"

epic: "EPIC_4_SYSTEM_NFR"
req_ids: ["REQ-NF-005", "REQ-NF-011"]
component: ["infra.cicd", "infra.deployment"]

category: "operations"
labels:
  - "cicd:github-actions"
  - "deployment:ecs"
  - "docker:ecr"

context:
  srs_section: "3.2 Deployment & Operations"
  tech_stack: ["GitHub Actions", "Docker", "AWS ECR", "AWS ECS", "Gradle"]

requirements:
  description: "안정적이고 자동화된 배포 파이프라인"
  kpis:
    - "빌드 시간 < 5분"
    - "배포 성공률 95% 이상"
    - "배포 실패 시 5분 이내 롤백"

design_constraints:
  - "민감한 정보는 GitHub Secrets에만 저장"
  - "프로덕션 배포는 수동 승인(approval) 단계 포함"
  - "Docker 이미지는 멀티 스테이지 빌드로 최적화"

steps_hint:
  - ".github/workflows/ci.yml 생성 (PR 시 빌드/테스트)"
  - ".github/workflows/deploy-dev.yml 생성 (develop 브랜치 자동 배포)"
  - ".github/workflows/deploy-prod.yml 생성 (main 브랜치 수동 승인 배포)"
  - "Dockerfile 작성 (멀티 스테이지: builder + runtime)"
  - "ECR Repository 생성 (Terraform)"
  - "ECS Task Definition 템플릿 작성"
  - "배포 스크립트 작성 (aws ecs update-service)"
  - "Health Check Endpoint 구현 (/actuator/health)"
  - "Slack Webhook 연동 (성공/실패 알림)"

preconditions:
  - "EPIC4-SYS-001 (ECS Cluster) 완료"
  - "GitHub Repository에 Secrets 등록 완료"
  - "AWS IAM User/Role (GitHub Actions용) 생성 완료"

postconditions:
  - "PR 생성 시 자동으로 빌드/테스트가 실행된다."
  - "develop 브랜치 푸시 시 Dev 환경에 자동 배포된다."
  - "main 브랜치 푸시 시 승인 후 Prod 환경에 배포된다."
  - "배포 상태가 Slack으로 통보된다."

dependencies: ["EPIC4-SYS-001"]

parallelizable: true
estimated_effort: "M"
priority: "Must"
agent_profile: ["devops", "backend"]

risk_notes:
  - "ECS 배포 중 다운타임 발생 가능 (Blue-Green 전략으로 최소화)"
  - "ECR 이미지 용량 증가 시 빌드 시간 증가 (캐싱 전략 필요)"
```

