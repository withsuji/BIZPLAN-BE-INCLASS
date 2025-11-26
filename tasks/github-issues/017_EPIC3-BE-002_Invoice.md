---
issue_number: 017
epic: EPIC-3
task_id: EPIC3-BE-002
title: "[BE] 인보이스 자동 생성 및 상태 관리"
labels: ["backend", "billing", "invoice", "priority:must"]
assignees: []
milestone: "MVP-Phase-2-Billing"
dependencies: ["#013"]
parallel_group: "group-5-billing"
estimated_effort: "XL"
---

# Issue #017: 인보이스 자동 생성 및 상태 관리

## 📋 개요
시간 기록 기반 인보이스를 자동 생성하고, Draft → Sent → Paid 상태 전환을 관리한다.

## 🎯 목표
- 인보이스 자동 생성
- 생명주기 관리 (상태 전환)
- 이메일 발송 및 PDF 생성

## 📝 상세 작업 내역

### 1. 데이터 모델
- [ ] Invoice, InvoiceLineItem, InvoiceSendLog Entity 생성
- [ ] Repository 생성
- [ ] 인보이스 번호 시퀀스 설정

### 2. Invoice Service
- [ ] InvoiceService 구현 (생성, 상태 전환, 발송)
- [ ] InvoiceNumberGenerator (시퀀스 관리)
- [ ] InvoicePdfGenerator (iText 활용)
- [ ] InvoiceEmailService (SMTP 발송)

### 3. 자동 생성
- [ ] InvoiceScheduler (주간/월간 자동 생성)
- [ ] 청구 가능 TimeEntry 조회 및 그룹핑
- [ ] Line Item 생성 및 총액 계산
- [ ] 세금, 할인 적용

### 4. API 엔드포인트
- [ ] POST `/api/v1/invoices` (수동 생성)
- [ ] GET `/api/v1/invoices` (조회 및 필터링)
- [ ] PATCH `/api/v1/invoices/{id}/status` (상태 변경)
- [ ] POST `/api/v1/invoices/{id}/send` (발송)
- [ ] GET `/api/v1/invoices/{id}/pdf` (PDF 다운로드)

### 5. Overdue Detection
- [ ] OverdueDetectionScheduler (매일 00시 실행)
- [ ] 기한 경과 인보이스 OVERDUE 상태 변경
- [ ] 알림 발송

### 6. 테스트
- [ ] 단위 테스트 (생성 로직, 계산 로직)
- [ ] 통합 테스트 (이메일 발송)
- [ ] PDF 생성 테스트

## ✅ 완료 조건
- [ ] 주간/월간 자동으로 인보이스 생성
- [ ] 사용자가 인보이스를 조회/수정/발송 가능
- [ ] 인보이스를 이메일로 발송 가능
- [ ] PDF 형식으로 다운로드 가능
- [ ] 기한 경과 시 자동으로 OVERDUE 상태 변경

## 📚 참고 문서
- SRS: `docs/GPT-SRS-v02.md` (Section 3.3 Billing Service)
- Task Spec: `tasks/functional/EPIC3-BE-002_Invoice_Generation.md`
- REQ-IDs: REQ-FUNC-021

## 🔗 의존성
**선행 작업**: #013 (Time Tracking)
**후행 작업**: #018 (Payment Gateway)
**병렬 가능**: 없음

## ⚠️ 주의사항
- PDF 생성 시 한글 폰트 문제 발생 가능 (폰트 임베딩 필요)
- 대량 이메일 발송 시 SMTP 제한 (SES 등 고려)
- SENT 상태 이후 인보이스는 수정 불가

## 🏷️ 라벨
`backend`, `billing`, `invoice`, `pdf`, `email`, `priority:must`, `size:XL`

