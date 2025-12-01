---
issue_number: 007
epic: EPIC-4
task_id: EPIC4-NFR-001
title: "[OPS] 구조화 로깅 및 중앙 집중식 모니터링 설정"
labels: ["ops", "logging", "monitoring", "observability", "priority:must", "size:M"]
assignees: []
milestone: "MVP-Phase-1-Core-Features"
dependencies: ["#001"]
parallel_group: "group-2-infra"
estimated_effort: "M"
difficulty: "중"
estimated_duration: "3일"
---

# Issue #007: 구조화 로깅 및 중앙 집중식 모니터링 설정

## 📋 개요
시스템의 **관측성(Observability)**을 확보하기 위해, 모든 애플리케이션 로그를 JSON 포맷으로 구조화하고 중앙 로그 저장소(CloudWatch Logs)로 수집하는 파이프라인을 구축한다. 주요 비즈니스 이벤트와 시스템 에러에 대한 기본 모니터링 대시보드를 구성한다.

## 🎯 목표
- JSON 포맷의 구조화된 로깅 표준 적용
- 요청별 Trace ID 자동 주입
- CloudWatch 로그 수집 및 대시보드 구성
- 에러율/응답시간 알림 설정

---

## 📝 상세 요구사항

### 1. 구조화 로깅 (Structured Logging)

#### JSON 로그 포맷
```json
{
  "timestamp": "2025-12-01T10:00:00.123Z",
  "level": "INFO",
  "logger": "vibe.bizplan.calendar.CalendarService",
  "message": "Event created successfully",
  "trace_id": "abc123xyz",
  "span_id": "def456",
  "service_name": "bizplan-api",
  "environment": "production",
  "user_id": "12345",
  "request_path": "/api/v1/events",
  "duration_ms": 45,
  "extra": {
    "event_id": "evt_001",
    "attendee_count": 3
  }
}
```

#### 필수 필드
| 필드 | 설명 | 소스 |
|------|------|------|
| `timestamp` | ISO8601 형식 시간 | 자동 |
| `level` | INFO, WARN, ERROR | Logback |
| `trace_id` | 분산 추적 ID | MDC |
| `span_id` | 스팬 ID | MDC |
| `service_name` | 서비스 이름 | 설정 |
| `environment` | 환경 (dev/prod) | 설정 |
| `user_id` | 로그인 사용자 ID | MDC |
| `request_path` | API 경로 | MDC |

### 2. MDC (Mapped Diagnostic Context) 설정

#### 자동 주입 정보
- **Trace ID**: 요청당 고유 ID (UUID)
- **User ID**: JWT에서 추출
- **Request Path**: HTTP 요청 경로
- **Client IP**: 클라이언트 IP
- **Duration**: 요청 처리 시간

### 3. 로그 수집 및 보존

| 환경 | 보존 기간 | 로그 레벨 |
|------|----------|----------|
| 개발 (dev) | 7일 | DEBUG |
| 운영 (prod) | 90일 | INFO |

### 4. 모니터링 대시보드

#### Golden Signals
- **Latency**: p50, p95, p99 응답 시간
- **Traffic**: 분당 요청 수 (RPM)
- **Errors**: 5xx 에러율
- **Saturation**: CPU/Memory 사용률

#### 알림 규칙
| 조건 | 심각도 | 채널 |
|------|--------|------|
| 에러율 > 1% | WARNING | Slack |
| 에러율 > 5% | CRITICAL | Slack + Email |
| p95 응답시간 > 1s | WARNING | Slack |
| p95 응답시간 > 3s | CRITICAL | Slack + Email |

---

## 🔧 구현 가이드

### Phase 1: Logback JSON 설정 (Day 1)

#### build.gradle 의존성
```groovy
dependencies {
    // Logback JSON Encoder
    implementation 'net.logstash.logback:logstash-logback-encoder:7.4'
    
    // Micrometer (메트릭)
    implementation 'io.micrometer:micrometer-registry-cloudwatch2'
}
```

#### logback-spring.xml
```xml
<?xml version="1.0" encoding="UTF-8"?>
<configuration>
    <springProperty scope="context" name="SERVICE_NAME" source="spring.application.name" defaultValue="bizplan-api"/>
    <springProperty scope="context" name="ENVIRONMENT" source="spring.profiles.active" defaultValue="local"/>

    <!-- Console Appender for Local Development -->
    <appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
        <encoder class="net.logstash.logback.encoder.LogstashEncoder">
            <fieldNames>
                <timestamp>timestamp</timestamp>
                <levelValue>[ignore]</levelValue>
            </fieldNames>
            <customFields>
                {"service_name":"${SERVICE_NAME}","environment":"${ENVIRONMENT}"}
            </customFields>
            <includeMdcKeyName>trace_id</includeMdcKeyName>
            <includeMdcKeyName>span_id</includeMdcKeyName>
            <includeMdcKeyName>user_id</includeMdcKeyName>
            <includeMdcKeyName>request_path</includeMdcKeyName>
        </encoder>
    </appender>

    <!-- JSON File Appender for Production -->
    <appender name="JSON_FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <file>logs/application.json</file>
        <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
            <fileNamePattern>logs/application.%d{yyyy-MM-dd}.json</fileNamePattern>
            <maxHistory>7</maxHistory>
        </rollingPolicy>
        <encoder class="net.logstash.logback.encoder.LogstashEncoder">
            <customFields>
                {"service_name":"${SERVICE_NAME}","environment":"${ENVIRONMENT}"}
            </customFields>
        </encoder>
    </appender>

    <!-- Profile-specific Configuration -->
    <springProfile name="local">
        <root level="DEBUG">
            <appender-ref ref="CONSOLE"/>
        </root>
    </springProfile>

    <springProfile name="dev,prod">
        <root level="INFO">
            <appender-ref ref="CONSOLE"/>
            <appender-ref ref="JSON_FILE"/>
        </root>
    </springProfile>

    <!-- Package-specific Log Levels -->
    <logger name="vibe.bizplan" level="DEBUG"/>
    <logger name="org.springframework" level="INFO"/>
    <logger name="org.hibernate.SQL" level="DEBUG"/>
</configuration>
```

### Phase 2: MDC Filter 구현 (Day 1-2)

#### MdcLoggingFilter.java
```java
package vibe.bizplan.common.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class MdcLoggingFilter implements Filter {

    private static final String TRACE_ID = "trace_id";
    private static final String SPAN_ID = "span_id";
    private static final String USER_ID = "user_id";
    private static final String REQUEST_PATH = "request_path";
    private static final String CLIENT_IP = "client_ip";
    private static final String REQUEST_START = "request_start";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        
        try {
            // Trace ID: 헤더에서 가져오거나 새로 생성
            String traceId = httpRequest.getHeader("X-Trace-ID");
            if (traceId == null || traceId.isEmpty()) {
                traceId = UUID.randomUUID().toString().replace("-", "").substring(0, 16);
            }
            
            // MDC 설정
            MDC.put(TRACE_ID, traceId);
            MDC.put(SPAN_ID, UUID.randomUUID().toString().substring(0, 8));
            MDC.put(REQUEST_PATH, httpRequest.getRequestURI());
            MDC.put(CLIENT_IP, getClientIp(httpRequest));
            MDC.put(REQUEST_START, String.valueOf(System.currentTimeMillis()));
            
            // 응답 헤더에 Trace ID 추가 (디버깅용)
            httpResponse.setHeader("X-Trace-ID", traceId);
            
            chain.doFilter(request, response);
            
        } finally {
            MDC.clear();
        }
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty()) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}
```

#### UserIdMdcInterceptor.java
```java
package vibe.bizplan.common.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class UserIdMdcInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()) {
            // JWT에서 추출한 사용자 ID 설정
            MDC.put("user_id", auth.getName());
        }
        return true;
    }
}
```

### Phase 3: CloudWatch 설정 (Day 2-3)

#### Terraform - CloudWatch Log Group
```hcl
# infra/modules/monitoring/main.tf

resource "aws_cloudwatch_log_group" "app_logs" {
  name              = "/ecs/bizplan-api"
  retention_in_days = var.environment == "prod" ? 90 : 7

  tags = {
    Environment = var.environment
    Service     = "bizplan-api"
  }
}

# Metric Filter for Error Rate
resource "aws_cloudwatch_log_metric_filter" "error_count" {
  name           = "bizplan-error-count"
  pattern        = "{ $.level = \"ERROR\" }"
  log_group_name = aws_cloudwatch_log_group.app_logs.name

  metric_transformation {
    name      = "ErrorCount"
    namespace = "BizPlan/Application"
    value     = "1"
  }
}

# Metric Filter for 5xx Errors
resource "aws_cloudwatch_log_metric_filter" "http_5xx" {
  name           = "bizplan-http-5xx"
  pattern        = "{ $.status >= 500 }"
  log_group_name = aws_cloudwatch_log_group.app_logs.name

  metric_transformation {
    name      = "Http5xxCount"
    namespace = "BizPlan/Application"
    value     = "1"
  }
}

# CloudWatch Alarm for High Error Rate
resource "aws_cloudwatch_metric_alarm" "high_error_rate" {
  alarm_name          = "bizplan-high-error-rate"
  comparison_operator = "GreaterThanThreshold"
  evaluation_periods  = 2
  metric_name         = "ErrorCount"
  namespace           = "BizPlan/Application"
  period              = 300  # 5분
  statistic           = "Sum"
  threshold           = 10
  alarm_description   = "Error count exceeds threshold"
  alarm_actions       = [aws_sns_topic.alerts.arn]
}
```

### Phase 4: 로깅 유틸리티 (Day 3)

#### LoggingAspect.java
```java
package vibe.bizplan.common.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
public class LoggingAspect {

    @Around("@within(org.springframework.web.bind.annotation.RestController)")
    public Object logApiCall(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        String methodName = joinPoint.getSignature().toShortString();
        
        log.info("API Request started: {}", methodName);
        
        try {
            Object result = joinPoint.proceed();
            long duration = System.currentTimeMillis() - startTime;
            
            log.info("API Request completed: {} ({}ms)", methodName, duration);
            return result;
            
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("API Request failed: {} ({}ms) - {}", methodName, duration, e.getMessage());
            throw e;
        }
    }
}
```

---

## 📁 산출물 (Artifacts)

```
src/main/
├── java/vibe/bizplan/common/
│   ├── filter/
│   │   └── MdcLoggingFilter.java
│   ├── interceptor/
│   │   └── UserIdMdcInterceptor.java
│   ├── aspect/
│   │   └── LoggingAspect.java
│   └── config/
│       └── WebMvcConfig.java
├── resources/
│   └── logback-spring.xml

infra/modules/monitoring/
├── main.tf
├── variables.tf
├── outputs.tf
└── dashboard.tf
```

---

## ✅ 완료 조건 (Acceptance Criteria)

| # | 조건 | 검증 방법 |
|---|------|----------|
| AC-1 | JSON 로그 출력 | 앱 실행 후 콘솔에 JSON 형식 로그 확인 |
| AC-2 | Trace ID 자동 주입 | 모든 로그에 `trace_id` 필드 존재 |
| AC-3 | User ID 포함 | 인증된 요청의 로그에 `user_id` 필드 존재 |
| AC-4 | CloudWatch 수집 | CloudWatch 콘솔에서 로그 스트림 확인 |
| AC-5 | Metric Filter 동작 | ErrorCount 메트릭 CloudWatch에서 확인 |
| AC-6 | 알림 설정 | 테스트 에러 발생 시 Slack 알림 수신 |
| AC-7 | PII 마스킹 | 이메일, 전화번호 등 로그에 마스킹 처리 |

---

## 🔗 의존성

**선행 작업**:
- #001 (AWS 인프라) - CloudWatch, ECS 필요

**후행 작업**:
- 없음 (모든 서비스에 적용)

**병렬 가능**:
- #010 (Calendar Sync), #011 (LLM Pipeline)

---

## 🏷️ 라벨
`ops`, `logging`, `monitoring`, `observability`, `priority:must`, `size:M`, `difficulty:medium`

---

## 📚 참고 문서
- Task Spec: `tasks/non-functional/EPIC4-NFR-001_Logging_and_Monitoring.md`
- REQ-IDs: REQ-NF-012, REQ-NF-006, REQ-NF-002
- [Logstash Logback Encoder](https://github.com/logfellow/logstash-logback-encoder)
- [AWS CloudWatch 문서](https://docs.aws.amazon.com/cloudwatch/)
