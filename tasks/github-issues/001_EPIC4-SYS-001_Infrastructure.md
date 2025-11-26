---
issue_number: 001
epic: EPIC-4
task_id: EPIC4-SYS-001
title: "[INFRA] AWS 인프라 및 데이터베이스 환경 구축"
labels: ["infra", "database", "terraform", "aws", "priority:must"]
assignees: []
milestone: "MVP-Phase-0-Infrastructure"
dependencies: []
parallel_group: "group-0-foundation"
estimated_effort: "L"
---

# Issue #001: AWS 인프라 및 데이터베이스 환경 구축

## 📋 개요
Terraform을 사용하여 서비스 운영을 위한 AWS 클라우드 인프라(VPC, Subnet, Aurora MySQL, ECS Cluster)를 프로비저닝한다.

## 🎯 목표
- 보안 모범 사례를 준수하는 AWS 인프라 구성
- 코드형 인프라(IaC)로 재현 가능한 환경 구축
- 개발/운영 환경 분리

## 📝 상세 작업 내역

### 1. 네트워크 구성 (VPC)
- [ ] VPC 생성 (CIDR `10.0.0.0/16`)
- [ ] Public Subnet 구성 (ALB, NAT Gateway)
- [ ] Private Subnet 구성 (ECS, RDS, Redis)
- [ ] Security Groups 설정 (최소 권한 원칙)
- [ ] Route Tables 및 NAT Gateway 설정

### 2. 데이터베이스 (RDS Aurora)
- [ ] Aurora MySQL 3.x 클러스터 생성
- [ ] Instance Type 설정 (`db.t3.medium` 개발용)
- [ ] `utf8mb4` 캐릭터셋, UTC 타임존 설정
- [ ] AWS Secrets Manager로 마스터 계정 정보 관리
- [ ] 백업 정책 설정

### 3. 컴퓨팅 환경 (ECS)
- [ ] ECS Cluster 생성
- [ ] Task Definition 기본 템플릿 구성
- [ ] IAM Roles 생성 (ECS Task Execution Role)
- [ ] ECR Repository 생성

### 4. Terraform 설정
- [ ] Backend 설정 (S3 + DynamoDB)
- [ ] Terraform 모듈 작성
- [ ] Variables 및 Outputs 정의
- [ ] 환경별 tfvars 파일 작성 (dev, prod)

## ✅ 완료 조건
- [ ] `terraform apply` 성공 및 State 파일이 S3에 저장됨
- [ ] AWS 콘솔에서 VPC, RDS, ECS 리소스 확인 가능
- [ ] Bastion Host 또는 VPN을 통해 DB 접속 가능
- [ ] Private Subnet 내 인스턴스가 NAT를 통해 아웃바운드 통신 가능

## 📚 참고 문서
- SRS: `docs/GPT-SRS-v02.md` (Section 1.2 Scope - Constraints)
- Task Spec: `tasks/non-functional/EPIC4-SYS-001_AWS_Infra_Provisioning.md`
- REQ-IDs: REQ-NF-005, REQ-NF-011, C-TEC-003

## 🔗 의존성
**선행 작업**: 없음 (프로젝트 시작점)
**후행 작업**: #002 (OAuth), #010 (Calendar Sync), #014 (LLM Pipeline)
**병렬 가능**: #002 (OAuth), #003 (FE Layout)

## ⚠️ 주의사항
- RDS 프로비저닝은 시간이 오래 걸림(15분+), 타임아웃 설정 유의
- 비용 발생 리소스이므로 개발용 인스턴스 타입 준수
- DB는 반드시 Private Subnet에 위치
- 모든 리소스에 `Environment`, `Project` 태그 부착

## 🏷️ 라벨
`infra`, `terraform`, `aws`, `database`, `priority:must`, `size:L`

