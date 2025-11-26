# EPIC4-SYS-001: AWS 인프라 및 데이터베이스 환경 구축

## 1. 개요 및 목적
본 Task는 AI 생산성행동 트래킹 서비스의 백엔드와 데이터베이스가 구동될 **AWS 클라우드 인프라를 프로비저닝**하는 것을 목적으로 한다.
코드형 인프라(IaC) 도구인 **Terraform**을 사용하여 VPC, 네트워크, 컴퓨팅(ECS/Fargate), 데이터베이스(Aurora MySQL) 리소스를 생성하고 관리 가능한 상태로 만든다.

## 2. 상세 요구사항

### 2.1. 네트워크 구성 (VPC)
- **VPC**: 프로덕션용 격리된 VPC 생성 (CIDR `10.0.0.0/16`).
- **Subnets**:
    - **Public Subnet**: ALB(Application Load Balancer), NAT Gateway 배치.
    - **Private Subnet**: ECS 워크로드, RDS, Redis 배치 (외부 접근 차단).
- **Security Groups**: 최소 권한 원칙에 따라 포트 및 소스 IP 제한.

### 2.2. 데이터베이스 (RDS Aurora)
- **Engine**: Amazon Aurora MySQL 3.x (MySQL 8.0 호환).
- **Instance**: 개발/테스트용 `db.t3.medium` (또는 Serverless v2).
- **Config**: `utf8mb4` 캐릭터셋, UTC 타임존 기본 설정.
- **Credentials**: AWS Secrets Manager로 마스터 계정 정보 관리.

### 2.3. 컴퓨팅 및 배포 환경 (ECS)
- **Cluster**: Amazon ECS 클러스터 생성.
- **Task Definition**: Spring Boot 앱을 위한 기본 템플릿 구성 (LogDriver=awslogs).
- **IAM Roles**: ECS Task Execution Role (ECR Pull, CloudWatch Logs 권한).

## 3. Task Definition (YAML)

```yaml
task_id: "EPIC4-SYS-001"
title: "Terraform 기반 AWS 인프라(VPC, RDS, ECS) 프로비저닝"
summary: >
  서비스 운영을 위한 AWS 기본 인프라(VPC, Subnet, Aurora MySQL, ECS Cluster)를
  Terraform을 사용하여 코드로 정의하고 배포한다.
type: "non_functional"

epic: "EPIC_4_SYSTEM_NFR"
req_ids: ["REQ-NF-005", "REQ-NF-011", "C-TEC-003"]
component: ["infra.aws", "infra.db"]

category: "operations"
labels:
  - "infra:provisioning"
  - "db:mysql"
  - "aws:ecs"

context:
  srs_section: "1.2 Scope - Constraints (C-TEC-003)"
  tech_stack: ["Terraform", "AWS", "MySQL"]

requirements:
  description: "보안 모범 사례(Private Subnet 원칙)를 준수하는 인프라 구성"
  kpis:
    - "Terraform Apply 성공 및 상태 파일(State) 백엔드(S3) 저장"
    - "Private Subnet 내 인스턴스가 NAT를 통해 아웃바운드 통신 가능"

design_constraints:
  - "DB는 반드시 Public Access가 차단된 Private Subnet에 위치해야 한다."
  - "모든 리소스에는 `Environment`, `Project` 태그를 부착한다."

steps_hint:
  - "Terraform Backend(S3 + DynamoDB) 설정"
  - "VPC 및 네트워크(Subnet, Route Table, NAT GW) 모듈 작성"
  - "Security Group 정의 (ALB -> App -> DB 체인)"
  - "RDS Aurora MySQL 클러스터 프로비저닝"
  - "ECS 클러스터 및 기본 IAM Role 생성"
  - "Outputs 출력 (DB Endpoint, VPC ID 등)"

preconditions:
  - "AWS CLI 자격 증명이 설정된 환경"

postconditions:
  - "AWS 콘솔에서 VPC, RDS, ECS 리소스가 생성된 것을 확인할 수 있다."
  - "Bastion Host 또는 VPN을 통해 DB 접속이 가능하다."

dependencies: []

parallelizable: true
estimated_effort: "L"
priority: "Must"
agent_profile: ["infra", "devops"]

risk_notes:
  - "RDS 프로비저닝은 시간이 오래 걸리므로(15분+), 타임아웃 설정에 유의한다."
  - "비용 발생 리소스이므로 사용 후 삭제하거나 개발용 인스턴스 타입을 준수한다."
```









