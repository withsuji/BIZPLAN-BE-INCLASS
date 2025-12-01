---
issue_number: 003
epic: EPIC-4
task_id: EPIC4-SYS-003
title: "[DEVOPS] CI/CD 파이프라인 구성 (GitHub Actions)"
labels: ["devops", "cicd", "github-actions", "docker", "priority:must", "size:M"]
assignees: []
milestone: "MVP-Phase-0-Infrastructure"
dependencies: ["#001"]
parallel_group: "group-0-foundation"
estimated_effort: "M"
difficulty: "중"
estimated_duration: "3일"
---

# Issue #003: CI/CD 파이프라인 구성 (GitHub Actions)

## 📋 개요
코드 변경 사항을 자동으로 빌드, 테스트, 배포하는 CI/CD 파이프라인을 GitHub Actions를 통해 구축한다. 개발 환경과 프로덕션 환경을 분리하여 안전한 배포 프로세스를 확립한다.

## 🎯 목표
- PR 시 자동 빌드 및 테스트 실행
- develop 브랜치 자동 배포 (Dev 환경)
- main 브랜치 수동 승인 배포 (Prod 환경)
- Docker 이미지 빌드 및 ECR 푸시

---

## 📝 상세 요구사항

### 1. CI (Continuous Integration)

#### 트리거 조건
- PR 생성/업데이트 시 자동 실행
- `main`, `develop` 브랜치 대상 PR

#### CI 파이프라인 단계
1. **Checkout**: 코드 체크아웃
2. **Setup**: JDK 17 설치, Gradle 캐시 설정
3. **Build**: `./gradlew build -x test`
4. **Test**: `./gradlew test`
5. **Code Quality**: Checkstyle 실행
6. **Coverage**: JaCoCo 리포트 생성 (목표: 70%)

### 2. CD (Continuous Deployment)

#### Dev 환경 배포
- **트리거**: develop 브랜치 push
- **자동 배포**: 승인 없이 자동 진행
- **환경**: ECS Dev Cluster

#### Prod 환경 배포
- **트리거**: main 브랜치 push 또는 Release Tag
- **수동 승인**: `environment: production` 승인 필요
- **환경**: ECS Prod Cluster

### 3. Docker 이미지 관리

#### Dockerfile (Multi-stage Build)
```dockerfile
# Stage 1: Build
FROM gradle:8.5-jdk17 AS builder
WORKDIR /app
COPY build.gradle settings.gradle ./
COPY src ./src
RUN gradle build -x test --no-daemon

# Stage 2: Runtime
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=builder /app/build/libs/*.jar app.jar

# Security
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

#### 이미지 태그 전략
- Dev: `{repository}:dev-{short-sha}-{timestamp}`
- Prod: `{repository}:v{version}-{short-sha}`

---

## 🔧 구현 가이드

### Phase 1: GitHub Secrets 설정 (사전 작업)

다음 Secrets를 GitHub Repository에 등록:

| Secret Name | 설명 |
|-------------|------|
| `AWS_ACCESS_KEY_ID` | AWS IAM 액세스 키 |
| `AWS_SECRET_ACCESS_KEY` | AWS IAM 시크릿 키 |
| `AWS_REGION` | AWS 리전 (ap-northeast-2) |
| `ECR_REPOSITORY` | ECR 저장소 URI |
| `ECS_CLUSTER_DEV` | ECS Dev 클러스터 이름 |
| `ECS_CLUSTER_PROD` | ECS Prod 클러스터 이름 |
| `ECS_SERVICE_DEV` | ECS Dev 서비스 이름 |
| `ECS_SERVICE_PROD` | ECS Prod 서비스 이름 |
| `SLACK_WEBHOOK_URL` | Slack 알림용 Webhook URL |

### Phase 2: CI Workflow 작성 (Day 1)

`.github/workflows/ci.yml`:
```yaml
name: CI

on:
  pull_request:
    branches: [main, develop]
  push:
    branches: [develop]

jobs:
  build-and-test:
    runs-on: ubuntu-latest
    
    steps:
      - name: Checkout
        uses: actions/checkout@v4
        
      - name: Setup JDK 17
        uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'temurin'
          
      - name: Cache Gradle
        uses: actions/cache@v4
        with:
          path: |
            ~/.gradle/caches
            ~/.gradle/wrapper
          key: ${{ runner.os }}-gradle-${{ hashFiles('**/*.gradle*', '**/gradle-wrapper.properties') }}
          restore-keys: |
            ${{ runner.os }}-gradle-
            
      - name: Grant execute permission
        run: chmod +x gradlew
        
      - name: Build
        run: ./gradlew build -x test
        
      - name: Run Tests
        run: ./gradlew test
        
      - name: Code Coverage Report
        run: ./gradlew jacocoTestReport
        
      - name: Upload Coverage to Codecov
        uses: codecov/codecov-action@v4
        with:
          file: ./build/reports/jacoco/test/jacocoTestReport.xml
          fail_ci_if_error: false
          
      - name: Checkstyle
        run: ./gradlew checkstyleMain checkstyleTest
        continue-on-error: true
```

### Phase 3: CD Workflow - Dev (Day 2)

`.github/workflows/deploy-dev.yml`:
```yaml
name: Deploy to Dev

on:
  push:
    branches: [develop]

env:
  AWS_REGION: ${{ secrets.AWS_REGION }}

jobs:
  deploy:
    runs-on: ubuntu-latest
    
    steps:
      - name: Checkout
        uses: actions/checkout@v4
        
      - name: Configure AWS credentials
        uses: aws-actions/configure-aws-credentials@v4
        with:
          aws-access-key-id: ${{ secrets.AWS_ACCESS_KEY_ID }}
          aws-secret-access-key: ${{ secrets.AWS_SECRET_ACCESS_KEY }}
          aws-region: ${{ env.AWS_REGION }}
          
      - name: Login to Amazon ECR
        id: login-ecr
        uses: aws-actions/amazon-ecr-login@v2
        
      - name: Build and push Docker image
        env:
          ECR_REGISTRY: ${{ steps.login-ecr.outputs.registry }}
          ECR_REPOSITORY: ${{ secrets.ECR_REPOSITORY }}
          IMAGE_TAG: dev-${{ github.sha }}-${{ github.run_number }}
        run: |
          docker build -t $ECR_REGISTRY/$ECR_REPOSITORY:$IMAGE_TAG .
          docker push $ECR_REGISTRY/$ECR_REPOSITORY:$IMAGE_TAG
          echo "IMAGE=$ECR_REGISTRY/$ECR_REPOSITORY:$IMAGE_TAG" >> $GITHUB_ENV
          
      - name: Deploy to ECS
        run: |
          aws ecs update-service \
            --cluster ${{ secrets.ECS_CLUSTER_DEV }} \
            --service ${{ secrets.ECS_SERVICE_DEV }} \
            --force-new-deployment
            
      - name: Notify Slack
        uses: 8398a7/action-slack@v3
        with:
          status: ${{ job.status }}
          fields: repo,message,commit,author,action,eventName,workflow
        env:
          SLACK_WEBHOOK_URL: ${{ secrets.SLACK_WEBHOOK_URL }}
        if: always()
```

### Phase 4: CD Workflow - Prod (Day 2-3)

`.github/workflows/deploy-prod.yml`:
```yaml
name: Deploy to Production

on:
  push:
    branches: [main]
  workflow_dispatch:

env:
  AWS_REGION: ${{ secrets.AWS_REGION }}

jobs:
  deploy:
    runs-on: ubuntu-latest
    environment: production  # 수동 승인 필요
    
    steps:
      - name: Checkout
        uses: actions/checkout@v4
        
      - name: Configure AWS credentials
        uses: aws-actions/configure-aws-credentials@v4
        with:
          aws-access-key-id: ${{ secrets.AWS_ACCESS_KEY_ID }}
          aws-secret-access-key: ${{ secrets.AWS_SECRET_ACCESS_KEY }}
          aws-region: ${{ env.AWS_REGION }}
          
      - name: Login to Amazon ECR
        id: login-ecr
        uses: aws-actions/amazon-ecr-login@v2
        
      - name: Get version
        id: version
        run: echo "VERSION=$(date +'%Y%m%d.%H%M')" >> $GITHUB_OUTPUT
        
      - name: Build and push Docker image
        env:
          ECR_REGISTRY: ${{ steps.login-ecr.outputs.registry }}
          ECR_REPOSITORY: ${{ secrets.ECR_REPOSITORY }}
          IMAGE_TAG: v${{ steps.version.outputs.VERSION }}-${{ github.sha }}
        run: |
          docker build -t $ECR_REGISTRY/$ECR_REPOSITORY:$IMAGE_TAG .
          docker build -t $ECR_REGISTRY/$ECR_REPOSITORY:latest .
          docker push $ECR_REGISTRY/$ECR_REPOSITORY:$IMAGE_TAG
          docker push $ECR_REGISTRY/$ECR_REPOSITORY:latest
          
      - name: Deploy to ECS
        run: |
          aws ecs update-service \
            --cluster ${{ secrets.ECS_CLUSTER_PROD }} \
            --service ${{ secrets.ECS_SERVICE_PROD }} \
            --force-new-deployment
            
      - name: Wait for deployment
        run: |
          aws ecs wait services-stable \
            --cluster ${{ secrets.ECS_CLUSTER_PROD }} \
            --services ${{ secrets.ECS_SERVICE_PROD }}
            
      - name: Notify Slack - Success
        uses: 8398a7/action-slack@v3
        with:
          status: success
          text: '🚀 Production deployment completed!'
        env:
          SLACK_WEBHOOK_URL: ${{ secrets.SLACK_WEBHOOK_URL }}
        if: success()
        
      - name: Notify Slack - Failure
        uses: 8398a7/action-slack@v3
        with:
          status: failure
          text: '❌ Production deployment failed!'
        env:
          SLACK_WEBHOOK_URL: ${{ secrets.SLACK_WEBHOOK_URL }}
        if: failure()
```

### Phase 5: 검증 (Day 3)
- [ ] PR 생성 시 CI 자동 실행 확인
- [ ] develop 브랜치 push 시 Dev 배포 확인
- [ ] main 브랜치 push 시 승인 대기 확인
- [ ] Slack 알림 수신 확인

---

## 📁 산출물 (Artifacts)

```
.github/
├── workflows/
│   ├── ci.yml
│   ├── deploy-dev.yml
│   └── deploy-prod.yml
├── CODEOWNERS
└── pull_request_template.md

Dockerfile
.dockerignore
```

---

## ✅ 완료 조건 (Acceptance Criteria)

| # | 조건 | 검증 방법 |
|---|------|----------|
| AC-1 | PR 시 CI 자동 실행 | PR 생성 후 Actions 탭에서 워크플로우 실행 확인 |
| AC-2 | 빌드 성공 | CI 워크플로우에서 Build 단계 성공 |
| AC-3 | 테스트 실행 | Test 결과 리포트 생성 확인 |
| AC-4 | Docker 이미지 빌드 | ECR에 이미지 푸시 확인 |
| AC-5 | Dev 자동 배포 | develop push 후 ECS 서비스 업데이트 확인 |
| AC-6 | Prod 수동 승인 | main push 후 승인 대기 상태 확인 |
| AC-7 | Slack 알림 | 배포 성공/실패 시 Slack 메시지 수신 |
| AC-8 | 빌드 시간 < 5분 | CI 워크플로우 전체 소요 시간 확인 |

---

## 🔗 의존성

**선행 작업**:
- #001 (AWS 인프라 구축) - ECR, ECS 클러스터 필요

**후행 작업**:
- 없음 (모든 작업에 CI/CD 적용)

**병렬 가능**:
- #002 (OAuth) - 인프라 완료 후 병렬 가능

---

## 🏷️ 라벨
`devops`, `cicd`, `github-actions`, `docker`, `priority:must`, `size:M`, `difficulty:medium`

---

## ⚠️ 주의사항 및 리스크

| 리스크 | 영향 | 완화 방안 |
|--------|------|----------|
| ECR 이미지 용량 증가 | 빌드 시간 증가 | 이미지 정리 정책 (Lifecycle Policy) 설정 |
| ECS 배포 중 다운타임 | 서비스 중단 | Rolling Update 또는 Blue-Green 전략 |
| Secrets 노출 | 보안 사고 | GitHub Environments 활용, 로그 마스킹 |
| 배포 실패 | 서비스 장애 | 자동 롤백 설정, Health Check 강화 |

---

## 📚 참고 문서
- Task Spec: `tasks/non-functional/EPIC4-SYS-003_CI_CD_Pipeline.md`
- REQ-IDs: REQ-NF-005, REQ-NF-011
- [GitHub Actions 문서](https://docs.github.com/en/actions)
- [AWS ECS 배포 가이드](https://docs.aws.amazon.com/AmazonECS/latest/developerguide/ecs_services.html)



