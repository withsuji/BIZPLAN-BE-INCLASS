---
issue_number: 002
epic: EPIC-4
task_id: EPIC4-SYS-002
title: "[AUTH] OAuth 2.0 인증 서버 구성 및 JWT 핸들링"
labels: ["backend", "auth", "oauth", "jwt", "priority:must"]
assignees: []
milestone: "MVP-Phase-0-Infrastructure"
dependencies: ["#001"]
parallel_group: "group-0-foundation"
estimated_effort: "L"
---

# Issue #002: OAuth 2.0 인증 서버 구성 및 JWT 핸들링

## 📋 개요
Google/Outlook OAuth 2.0 연동 및 JWT 기반 사용자 인증/권한 관리 시스템을 구축한다.

## 🎯 목표
- 안전한 OAuth 토큰 관리
- JWT 기반 API 인증
- 사용자 및 역할 관리

## 📝 상세 작업 내역

### 1. OAuth 2.0 Provider 연동
- [ ] Spring Security OAuth2 Client 의존성 추가
- [ ] Google OAuth Client 설정 (`application.yml`)
- [ ] Microsoft OAuth Client 설정
- [ ] OAuth Callback Controller 구현 (`/oauth/callback/{provider}`)
- [ ] Token 암호화/복호화 유틸리티 구현 (AES-256)

### 2. JWT 기반 세션 관리
- [ ] JWT Provider 클래스 구현 (토큰 생성/검증)
- [ ] JWT Authentication Filter 구현
- [ ] Refresh Token Rotation 전략 구현
- [ ] Claims 구조 정의 (user_id, email, roles, exp, iat)

### 3. User & Role 관리
- [ ] User Entity 생성 (email, name, profile_image, timezone)
- [ ] OAuthConnection Entity 생성
- [ ] UserRepository, OAuthConnectionRepository 생성
- [ ] RBAC 구현 (USER, ADMIN 역할)

### 4. Security 설정
- [ ] Security Config 작성
- [ ] 인증 예외 경로 설정 (Swagger, Health Check 등)
- [ ] CORS 설정
- [ ] AWS Secrets Manager에서 JWT 시크릿 키 로드

## ✅ 완료 조건
- [ ] 사용자가 Google/Outlook으로 로그인 가능
- [ ] JWT 토큰을 헤더에 담아 API 호출 시 인증 통과
- [ ] 토큰 만료 시 401 Unauthorized 응답
- [ ] Refresh Token으로 액세스 토큰 갱신 가능

## 📚 참고 문서
- SRS: `docs/GPT-SRS-v02.md` (Section 3.1 External Systems)
- Task Spec: `tasks/non-functional/EPIC4-SYS-002_OAuth_Authentication.md`
- REQ-IDs: REQ-NF-007, REQ-NF-008, C-TEC-002

## 🔗 의존성
**선행 작업**: #001 (인프라 구축 - DB 필요)
**후행 작업**: #010 (Calendar Sync - OAuth 토큰 필요)
**병렬 가능**: #003 (FE Layout), #004 (CI/CD)

## ⚠️ 주의사항
- OAuth 토큰은 AES-256으로 암호화하여 저장
- JWT 시크릿 키는 코드에 하드코딩 금지
- OAuth Provider API 변경 시 연동 로직 수정 필요
- Token Refresh 실패 시 사용자 재로그인 필요 (UX 고려)

## 🏷️ 라벨
`backend`, `auth`, `oauth`, `jwt`, `security`, `priority:must`, `size:L`

