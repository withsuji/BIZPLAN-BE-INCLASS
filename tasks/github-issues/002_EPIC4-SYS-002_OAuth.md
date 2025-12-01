---
issue_number: 002
epic: EPIC-4
task_id: EPIC4-SYS-002
title: "[AUTH] OAuth 2.0 인증 서버 구성 및 JWT 핸들링"
labels: ["auth", "oauth2", "security", "jwt", "priority:must", "size:L", "critical-path"]
assignees: []
milestone: "MVP-Phase-0-Infrastructure"
dependencies: ["#001"]
parallel_group: "group-0-foundation"
estimated_effort: "L"
difficulty: "중"
estimated_duration: "5일"
---

# Issue #002: OAuth 2.0 인증 서버 구성 및 JWT 핸들링

## 📋 개요
사용자 인증 및 권한 관리를 위한 OAuth 2.0 기반 인증 시스템을 구축한다. Google/Outlook 연동 시 필요한 OAuth 토큰 관리와 JWT 기반 세션 관리를 구현한다.

## 🎯 목표
- Google/Outlook OAuth 2.0 인증 플로우 구현
- JWT 기반 사용자 세션 관리
- 안전한 토큰 저장 및 갱신 메커니즘

---

## 📝 상세 요구사항

### 1. OAuth 2.0 Provider 연동

#### Google OAuth
- Google Calendar API 접근을 위한 OAuth 2.0 인증 플로우 구현
- Scope: `calendar.readonly`, `calendar.events`, `userinfo.email`, `userinfo.profile`
- Redirect URI: `/oauth/callback/google`

#### Microsoft OAuth
- Outlook Calendar API 접근을 위한 OAuth 2.0 인증 플로우 구현
- Azure AD B2C 또는 Microsoft Identity Platform 사용
- Scope: `Calendars.ReadWrite`, `User.Read`

#### Token Management
- 액세스 토큰 및 리프레시 토큰을 **AES-256 암호화**하여 DB에 저장
- 토큰 만료 15분 전 자동 갱신 메커니즘 구현
- Refresh Token Rotation 전략 적용

### 2. JWT 기반 세션 관리

#### JWT 구조
```json
{
  "sub": "user_id",
  "email": "user@example.com",
  "roles": ["USER"],
  "iat": 1700000000,
  "exp": 1700003600
}
```

#### 토큰 정책
- Access Token: 1시간 유효
- Refresh Token: 7일 유효
- 알고리즘: RS256 (비대칭키) 또는 HS256

#### 보안 설정
- JWT Secret은 AWS Secrets Manager에 저장
- HTTPS 필수 (Secure Cookie)
- CORS 설정 적용

### 3. User & Role 관리

#### User Entity
```yaml
User:
  - id: BIGINT (PK)
  - email: VARCHAR(255) UNIQUE
  - name: VARCHAR(100)
  - profile_image: VARCHAR(500)
  - timezone: VARCHAR(64)
  - role: ENUM('USER', 'ADMIN')
  - created_at: TIMESTAMP
  - updated_at: TIMESTAMP

OAuthConnection:
  - id: BIGINT (PK)
  - user_id: FK
  - provider: ENUM('GOOGLE', 'OUTLOOK')
  - provider_user_id: VARCHAR(255)
  - access_token: TEXT (encrypted)
  - refresh_token: TEXT (encrypted)
  - token_expires_at: TIMESTAMP
  - created_at: TIMESTAMP
  - updated_at: TIMESTAMP
```

---

## 🔧 구현 가이드

### Phase 1: 의존성 및 설정 (Day 1)

#### build.gradle 의존성 추가
```groovy
dependencies {
    // Spring Security OAuth2
    implementation 'org.springframework.boot:spring-boot-starter-oauth2-client'
    implementation 'org.springframework.boot:spring-boot-starter-security'
    
    // JWT
    implementation 'io.jsonwebtoken:jjwt-api:0.12.3'
    runtimeOnly 'io.jsonwebtoken:jjwt-impl:0.12.3'
    runtimeOnly 'io.jsonwebtoken:jjwt-jackson:0.12.3'
    
    // 암호화
    implementation 'org.bouncycastle:bcprov-jdk18on:1.77'
}
```

#### application.yml OAuth 설정
```yaml
spring:
  security:
    oauth2:
      client:
        registration:
          google:
            client-id: ${GOOGLE_CLIENT_ID}
            client-secret: ${GOOGLE_CLIENT_SECRET}
            scope: 
              - email
              - profile
              - https://www.googleapis.com/auth/calendar.events
            redirect-uri: "{baseUrl}/oauth/callback/{registrationId}"
          microsoft:
            client-id: ${AZURE_CLIENT_ID}
            client-secret: ${AZURE_CLIENT_SECRET}
            scope:
              - openid
              - email
              - profile
              - Calendars.ReadWrite
            authorization-grant-type: authorization_code
            redirect-uri: "{baseUrl}/oauth/callback/{registrationId}"
        provider:
          microsoft:
            authorization-uri: https://login.microsoftonline.com/common/oauth2/v2.0/authorize
            token-uri: https://login.microsoftonline.com/common/oauth2/v2.0/token
            user-info-uri: https://graph.microsoft.com/v1.0/me
            user-name-attribute: id

jwt:
  secret: ${JWT_SECRET}
  access-token-validity: 3600  # 1시간
  refresh-token-validity: 604800  # 7일
```

### Phase 2: Entity 및 Repository 구현 (Day 1-2)
- [ ] User Entity 생성
- [ ] OAuthConnection Entity 생성
- [ ] UserRepository Interface 구현
- [ ] OAuthConnectionRepository Interface 구현

### Phase 3: Security Config 구현 (Day 2-3)
- [ ] SecurityConfig 클래스 작성
- [ ] JWT Authentication Filter 구현
- [ ] OAuth2 Success/Failure Handler 구현
- [ ] Token Provider (생성/검증) 구현

### Phase 4: Controller 및 Service 구현 (Day 3-4)
- [ ] AuthController 구현 (로그인, 로그아웃, 토큰 갱신)
- [ ] OAuth2UserService 커스터마이징
- [ ] Token Encryption/Decryption Utility 구현
- [ ] User Profile API 구현

### Phase 5: 테스트 및 문서화 (Day 4-5)
- [ ] 단위 테스트 작성 (JWT 생성/검증)
- [ ] 통합 테스트 작성 (OAuth 플로우)
- [ ] API 문서 작성 (Swagger)
- [ ] 보안 점검

---

## 📁 산출물 (Artifacts)

```
src/main/java/vibe/bizplan/
├── auth/
│   ├── config/
│   │   ├── SecurityConfig.java
│   │   └── JwtConfig.java
│   ├── controller/
│   │   └── AuthController.java
│   ├── dto/
│   │   ├── LoginRequest.java
│   │   ├── TokenResponse.java
│   │   └── UserProfileResponse.java
│   ├── entity/
│   │   ├── User.java
│   │   └── OAuthConnection.java
│   ├── filter/
│   │   └── JwtAuthenticationFilter.java
│   ├── handler/
│   │   ├── OAuth2SuccessHandler.java
│   │   └── OAuth2FailureHandler.java
│   ├── provider/
│   │   └── JwtTokenProvider.java
│   ├── repository/
│   │   ├── UserRepository.java
│   │   └── OAuthConnectionRepository.java
│   ├── service/
│   │   ├── AuthService.java
│   │   ├── CustomOAuth2UserService.java
│   │   └── TokenEncryptionService.java
│   └── util/
│       └── CryptoUtil.java
```

---

## ✅ 완료 조건 (Acceptance Criteria)

| # | 조건 | 검증 방법 |
|---|------|----------|
| AC-1 | Google OAuth 로그인 성공 | `/oauth/login/google` 접속 → Google 인증 → JWT 발급 |
| AC-2 | Microsoft OAuth 로그인 성공 | `/oauth/login/microsoft` 접속 → MS 인증 → JWT 발급 |
| AC-3 | JWT 인증 동작 | Authorization 헤더로 API 호출 시 인증 통과 |
| AC-4 | 토큰 갱신 동작 | `/api/v1/auth/refresh` 호출 시 새 토큰 발급 |
| AC-5 | 토큰 만료 처리 | 만료된 토큰으로 호출 시 401 Unauthorized 반환 |
| AC-6 | OAuth 토큰 암호화 저장 | DB에 암호화된 토큰 확인 |
| AC-7 | 보안 경로 설정 | `/api/**` 경로는 인증 필수, `/oauth/**`는 허용 |

---

## 🔗 의존성

**선행 작업**:
- #001 (AWS 인프라 및 DB 구축) - DB 연결 필요

**후행 작업**:
- #010 (Calendar Sync) - OAuth 토큰으로 외부 API 호출

**병렬 가능**:
- #003 (CI/CD) - 인프라 완료 후 병렬 착수 가능

---

## 🏷️ 라벨
`auth`, `oauth2`, `security`, `jwt`, `priority:must`, `size:L`, `difficulty:medium`, `critical-path`

---

## ⚠️ 주의사항 및 리스크

| 리스크 | 영향 | 완화 방안 |
|--------|------|----------|
| OAuth Provider API 변경 | 연동 실패 | Provider 문서 정기 확인 |
| Token Refresh 실패 | 사용자 재로그인 필요 | Retry 로직 및 알림 구현 |
| JWT Secret 노출 | 보안 사고 | Secrets Manager 사용 필수 |
| CORS 설정 오류 | 프론트엔드 연동 실패 | 사전 테스트 환경에서 검증 |

---

## 📚 참고 문서
- Task Spec: `tasks/non-functional/EPIC4-SYS-002_OAuth_Authentication.md`
- SRS: `docs/GPT-SRS-v02.md` (Section 3.1 External Systems)
- REQ-IDs: REQ-NF-007, REQ-NF-008, C-TEC-002
- [Spring Security OAuth2 문서](https://docs.spring.io/spring-security/reference/servlet/oauth2/index.html)
- [Google OAuth2 가이드](https://developers.google.com/identity/protocols/oauth2)
- [Microsoft Identity Platform](https://docs.microsoft.com/en-us/azure/active-directory/develop/)
