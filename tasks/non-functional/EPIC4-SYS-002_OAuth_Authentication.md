# EPIC4-SYS-002: OAuth 2.0 인증 서버 구성 및 JWT 핸들링

## 1. 개요 및 목적
사용자 인증 및 권한 관리를 위한 OAuth 2.0 기반 인증 시스템을 구축한다. Google/Outlook 연동 시 필요한 OAuth 토큰 관리와 JWT 기반 세션 관리를 구현한다.

## 2. 상세 요구사항

### 2.1. OAuth 2.0 Provider 연동
- **Google OAuth**: Google Calendar API 접근을 위한 OAuth 2.0 인증 플로우 구현
- **Microsoft OAuth**: Outlook Calendar API 접근을 위한 OAuth 2.0 인증 플로우 구현
- **Token Storage**: 액세스 토큰 및 리프레시 토큰을 암호화하여 DB에 저장
- **Token Refresh**: 만료된 토큰 자동 갱신 메커니즘 구현

### 2.2. JWT 기반 세션 관리
- **JWT 생성**: 사용자 인증 후 액세스 토큰 및 리프레시 토큰 발급
- **JWT 검증**: 모든 API 요청에서 JWT 검증 필터 적용
- **Claims**: user_id, email, roles, exp, iat 포함
- **Security**: HS256 또는 RS256 알고리즘 사용, 시크릿 키는 AWS Secrets Manager에 저장

### 2.3. User & Role 관리
- **User Entity**: 사용자 기본 정보 (email, name, profile_image, timezone)
- **Role-Based Access Control**: USER, ADMIN 역할 구분
- **OAuth Provider Mapping**: 사용자가 여러 OAuth Provider 연결 가능 (Google + Outlook)

## 3. Task Definition (YAML)

```yaml
task_id: "EPIC4-SYS-002"
title: "OAuth 2.0 인증 서버 구성 및 JWT 핸들링 구현"
summary: >
  Google/Outlook OAuth 2.0 연동 및 JWT 기반 사용자 인증/권한 관리 시스템을 구축한다.
type: "non_functional"

epic: "EPIC_4_SYSTEM_NFR"
req_ids: ["REQ-NF-007", "REQ-NF-008", "C-TEC-002"]
component: ["backend.auth", "backend.user"]

category: "security"
labels:
  - "auth:oauth2"
  - "security:jwt"
  - "integration:google"
  - "integration:outlook"

context:
  srs_section: "3.1 External Systems (Google/Outlook OAuth)"
  tech_stack: ["Spring Security", "Spring Security OAuth2", "JWT", "MySQL"]

requirements:
  description: "안전한 OAuth 토큰 관리 및 JWT 기반 API 인증"
  kpis:
    - "OAuth 토큰 갱신 성공률 99% 이상"
    - "JWT 검증 시간 < 10ms"

design_constraints:
  - "OAuth 토큰은 AES-256으로 암호화하여 저장해야 한다."
  - "JWT 시크릿 키는 코드에 하드코딩하지 않고 외부 시크릿 관리 시스템 사용"
  - "Refresh Token Rotation 전략 적용"

steps_hint:
  - "Spring Security OAuth2 Client 의존성 추가"
  - "Google/Microsoft OAuth Client 설정 (application.yml)"
  - "OAuth Callback Controller 구현 (/oauth/callback/{provider})"
  - "Token 암호화/복호화 유틸리티 구현"
  - "User, OAuthConnection Entity 및 Repository 생성"
  - "JWT Provider 클래스 구현 (토큰 생성/검증)"
  - "JWT Authentication Filter 구현"
  - "Security Config에서 인증 예외 경로 설정 (Swagger, Health Check 등)"

preconditions:
  - "EPIC4-SYS-001 (DB 구축) 완료"
  - "Google Cloud Console 및 Azure AD에서 OAuth Client 등록 완료"

postconditions:
  - "사용자가 Google/Outlook으로 로그인할 수 있다."
  - "JWT 토큰을 헤더에 담아 API 호출 시 인증이 통과된다."
  - "토큰이 만료되면 401 Unauthorized 응답을 받는다."

dependencies: ["EPIC4-SYS-001"]

parallelizable: true
estimated_effort: "L"
priority: "Must"
agent_profile: ["backend", "security"]

risk_notes:
  - "OAuth Provider의 API 변경 시 연동 로직 수정 필요"
  - "Token Refresh 실패 시 사용자 재로그인 필요 (UX 고려)"
```

