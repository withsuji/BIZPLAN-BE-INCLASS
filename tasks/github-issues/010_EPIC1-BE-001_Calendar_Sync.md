---
issue_number: 010
epic: EPIC-1
task_id: EPIC1-BE-001
title: "[BE] Google/Outlook Calendar 양방향 동기화"
labels: ["backend", "calendar", "sync", "google", "outlook", "priority:must", "size:XL", "critical-path"]
assignees: []
milestone: "MVP-Phase-1-Core-Features"
dependencies: ["#001", "#002"]
parallel_group: "group-2-backend"
estimated_effort: "XL"
difficulty: "상"
estimated_duration: "8일"
---

# Issue #010: Google/Outlook Calendar 양방향 동기화

## 📋 개요
외부 캘린더 시스템(Google Calendar, Microsoft Outlook)과의 양방향 동기화를 구현하고, 실시간 이벤트 변경 사항을 Webhook을 통해 수신하는 기능을 구축한다.

## 🎯 목표
- Google Calendar API 연동 (Events CRUD + Webhook)
- Microsoft Graph API 연동 (Calendar Events + Subscription)
- Pull/Push 양방향 동기화 메커니즘
- 충돌 해결 전략 구현

---

## 📝 상세 요구사항

### 1. Calendar API 연동

#### Google Calendar API
- **Scope**: `calendar.events`, `calendar.readonly`
- **API**: Events List, Get, Insert, Update, Delete
- **Webhook**: Watch API를 통한 실시간 알림
- **Rate Limit**: 1,000 QPM (Queries Per Minute)

#### Microsoft Graph API
- **Scope**: `Calendars.ReadWrite`
- **API**: Calendar Events CRUD
- **Webhook**: Subscription API
- **Rate Limit**: 10,000 RPM (Requests Per Minute)

### 2. 동기화 전략

#### Pull Sync (Polling)
- 주기: 5분마다 실행
- Delta Sync: `syncToken` 활용 (변경분만 조회)
- 전체 Sync: 일 1회 또는 강제 동기화 시

#### Push Sync (Webhook)
- 내부 이벤트 생성/수정 시 외부 캘린더 반영
- 비동기 처리 (큐 사용)

#### Conflict Resolution
- **Last-Write-Wins**: 최신 `updated_at` 타임스탬프 우선
- **Conflict Log**: 충돌 발생 시 기록 후 알림

### 3. 데이터 모델

```sql
-- Calendar Connection (OAuth 토큰 저장)
CREATE TABLE calendar_connections (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    provider ENUM('GOOGLE', 'OUTLOOK') NOT NULL,
    calendar_id VARCHAR(255) NOT NULL,
    access_token TEXT NOT NULL,  -- encrypted
    refresh_token TEXT NOT NULL, -- encrypted
    token_expires_at TIMESTAMP NOT NULL,
    webhook_channel_id VARCHAR(255),
    webhook_expiration TIMESTAMP,
    last_sync_at TIMESTAMP,
    sync_token VARCHAR(500),
    status ENUM('ACTIVE', 'ERROR', 'DISABLED') DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id),
    INDEX idx_user_provider (user_id, provider)
);

-- Calendar Events (로컬 캐시)
CREATE TABLE calendar_events (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    external_id VARCHAR(255) UNIQUE,
    calendar_connection_id BIGINT NOT NULL,
    team_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    project_id BIGINT,
    title VARCHAR(500),
    description TEXT,
    location VARCHAR(500),
    start_at TIMESTAMP NOT NULL,
    end_at TIMESTAMP NOT NULL,
    timezone VARCHAR(64) DEFAULT 'UTC',
    is_all_day BOOLEAN DEFAULT FALSE,
    status ENUM('TENTATIVE', 'CONFIRMED', 'CANCELLED') DEFAULT 'TENTATIVE',
    attendees JSON,
    is_focus_block BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    synced_at TIMESTAMP,
    FOREIGN KEY (calendar_connection_id) REFERENCES calendar_connections(id),
    FOREIGN KEY (user_id) REFERENCES users(id),
    INDEX idx_user_time (user_id, start_at, end_at),
    INDEX idx_external (external_id)
);
```

### 4. Webhook 처리

#### Endpoint
- Google: `POST /api/v1/calendar/webhook/google`
- Microsoft: `POST /api/v1/calendar/webhook/microsoft`

#### 검증
- Google: `X-Goog-Channel-Token`, `X-Goog-Resource-State`
- Microsoft: `clientState`, `validationToken`

---

## 🔧 구현 가이드

### Phase 1: Entity 및 Repository (Day 1)

```java
// CalendarConnection.java
@Entity
@Table(name = "calendar_connections")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CalendarConnection extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CalendarProvider provider;
    
    @Column(name = "calendar_id", nullable = false)
    private String calendarId;
    
    @Column(name = "access_token", columnDefinition = "TEXT")
    private String accessToken;
    
    @Column(name = "refresh_token", columnDefinition = "TEXT")
    private String refreshToken;
    
    @Column(name = "token_expires_at")
    private LocalDateTime tokenExpiresAt;
    
    @Column(name = "webhook_channel_id")
    private String webhookChannelId;
    
    @Column(name = "webhook_expiration")
    private LocalDateTime webhookExpiration;
    
    @Column(name = "last_sync_at")
    private LocalDateTime lastSyncAt;
    
    @Column(name = "sync_token", length = 500)
    private String syncToken;
    
    @Enumerated(EnumType.STRING)
    private ConnectionStatus status = ConnectionStatus.ACTIVE;
    
    public boolean isTokenExpired() {
        return tokenExpiresAt != null && tokenExpiresAt.isBefore(LocalDateTime.now());
    }
    
    public void updateTokens(String accessToken, String refreshToken, LocalDateTime expiresAt) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.tokenExpiresAt = expiresAt;
    }
}
```

### Phase 2: Google Calendar Client (Day 2-3)

```java
// GoogleCalendarClient.java
@Component
@RequiredArgsConstructor
@Slf4j
public class GoogleCalendarClient {
    
    private final TokenEncryptionService tokenService;
    
    public List<CalendarEventDto> listEvents(CalendarConnection connection, LocalDateTime start, LocalDateTime end) {
        try {
            Calendar calendarService = getCalendarService(connection);
            
            Events events = calendarService.events()
                .list(connection.getCalendarId())
                .setTimeMin(toDateTime(start))
                .setTimeMax(toDateTime(end))
                .setSingleEvents(true)
                .setOrderBy("startTime")
                .execute();
            
            return events.getItems().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
                
        } catch (IOException e) {
            log.error("Failed to list Google Calendar events", e);
            throw new CalendarSyncException("Failed to sync with Google Calendar", e);
        }
    }
    
    public CalendarEventDto createEvent(CalendarConnection connection, CalendarEventDto dto) {
        try {
            Calendar calendarService = getCalendarService(connection);
            Event event = toGoogleEvent(dto);
            
            Event created = calendarService.events()
                .insert(connection.getCalendarId(), event)
                .execute();
            
            return toDto(created);
            
        } catch (IOException e) {
            log.error("Failed to create Google Calendar event", e);
            throw new CalendarSyncException("Failed to create event", e);
        }
    }
    
    public void setupWebhook(CalendarConnection connection, String webhookUrl) {
        try {
            Calendar calendarService = getCalendarService(connection);
            
            Channel channel = new Channel()
                .setId(UUID.randomUUID().toString())
                .setType("web_hook")
                .setAddress(webhookUrl)
                .setToken(connection.getId().toString())
                .setExpiration(System.currentTimeMillis() + TimeUnit.DAYS.toMillis(7));
            
            Channel result = calendarService.events()
                .watch(connection.getCalendarId(), channel)
                .execute();
            
            connection.setWebhookChannelId(result.getId());
            connection.setWebhookExpiration(
                Instant.ofEpochMilli(result.getExpiration())
                    .atZone(ZoneId.systemDefault())
                    .toLocalDateTime()
            );
            
        } catch (IOException e) {
            log.error("Failed to setup Google webhook", e);
            throw new CalendarSyncException("Failed to setup webhook", e);
        }
    }
    
    private Calendar getCalendarService(CalendarConnection connection) {
        String accessToken = tokenService.decrypt(connection.getAccessToken());
        
        GoogleCredentials credentials = GoogleCredentials.create(
            new AccessToken(accessToken, Date.from(
                connection.getTokenExpiresAt().atZone(ZoneId.systemDefault()).toInstant()
            ))
        );
        
        return new Calendar.Builder(
            GoogleNetHttpTransport.newTrustedTransport(),
            GsonFactory.getDefaultInstance(),
            new HttpCredentialsAdapter(credentials)
        ).setApplicationName("BizPlan").build();
    }
}
```

### Phase 3: Sync Service (Day 3-5)

```java
// CalendarSyncService.java
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CalendarSyncService {
    
    private final CalendarConnectionRepository connectionRepository;
    private final CalendarEventRepository eventRepository;
    private final GoogleCalendarClient googleClient;
    private final MicrosoftCalendarClient microsoftClient;
    
    /**
     * Pull Sync: 외부 캘린더 → 내부 DB
     */
    public SyncResult pullSync(Long connectionId) {
        CalendarConnection connection = connectionRepository.findById(connectionId)
            .orElseThrow(() -> new NotFoundException("Connection not found"));
        
        LocalDateTime syncStart = connection.getLastSyncAt() != null 
            ? connection.getLastSyncAt() 
            : LocalDateTime.now().minusDays(30);
        LocalDateTime syncEnd = LocalDateTime.now().plusMonths(3);
        
        List<CalendarEventDto> externalEvents = fetchExternalEvents(connection, syncStart, syncEnd);
        
        int created = 0, updated = 0, deleted = 0;
        
        for (CalendarEventDto dto : externalEvents) {
            Optional<CalendarEvent> existing = eventRepository.findByExternalId(dto.getExternalId());
            
            if (existing.isPresent()) {
                CalendarEvent event = existing.get();
                if (dto.getUpdatedAt().isAfter(event.getUpdatedAt())) {
                    updateEventFromDto(event, dto);
                    updated++;
                }
            } else {
                CalendarEvent newEvent = createEventFromDto(connection, dto);
                eventRepository.save(newEvent);
                created++;
            }
        }
        
        connection.setLastSyncAt(LocalDateTime.now());
        connectionRepository.save(connection);
        
        log.info("Pull sync completed: connection={}, created={}, updated={}, deleted={}", 
            connectionId, created, updated, deleted);
        
        return new SyncResult(created, updated, deleted);
    }
    
    /**
     * Push Sync: 내부 이벤트 → 외부 캘린더
     */
    public void pushEvent(CalendarEvent event, SyncAction action) {
        CalendarConnection connection = event.getCalendarConnection();
        
        switch (action) {
            case CREATE -> {
                CalendarEventDto created = createExternalEvent(connection, toDto(event));
                event.setExternalId(created.getExternalId());
                eventRepository.save(event);
            }
            case UPDATE -> updateExternalEvent(connection, toDto(event));
            case DELETE -> deleteExternalEvent(connection, event.getExternalId());
        }
    }
    
    /**
     * Webhook 이벤트 처리
     */
    public void handleWebhook(CalendarProvider provider, String channelId, String resourceState) {
        CalendarConnection connection = connectionRepository.findByWebhookChannelId(channelId)
            .orElseThrow(() -> new NotFoundException("Connection not found"));
        
        if ("sync".equals(resourceState)) {
            // 초기 sync 요청, 무시
            return;
        }
        
        // 비동기로 Pull Sync 실행
        asyncPullSync(connection.getId());
    }
    
    @Async
    public void asyncPullSync(Long connectionId) {
        pullSync(connectionId);
    }
}
```

### Phase 4: Webhook Controller (Day 5-6)

```java
// CalendarWebhookController.java
@RestController
@RequestMapping("/api/v1/calendar/webhook")
@RequiredArgsConstructor
@Slf4j
public class CalendarWebhookController {
    
    private final CalendarSyncService syncService;
    
    @PostMapping("/google")
    public ResponseEntity<Void> handleGoogleWebhook(
            @RequestHeader("X-Goog-Channel-ID") String channelId,
            @RequestHeader("X-Goog-Resource-State") String resourceState,
            @RequestHeader(value = "X-Goog-Channel-Token", required = false) String token) {
        
        log.info("Google webhook received: channelId={}, state={}", channelId, resourceState);
        
        try {
            syncService.handleWebhook(CalendarProvider.GOOGLE, channelId, resourceState);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Failed to handle Google webhook", e);
            return ResponseEntity.ok().build(); // Always return 200 to prevent retries
        }
    }
    
    @PostMapping("/microsoft")
    public ResponseEntity<String> handleMicrosoftWebhook(
            @RequestParam(value = "validationToken", required = false) String validationToken,
            @RequestBody(required = false) MicrosoftWebhookPayload payload) {
        
        // Validation request
        if (validationToken != null) {
            return ResponseEntity.ok(validationToken);
        }
        
        log.info("Microsoft webhook received: {}", payload);
        
        if (payload != null && payload.getValue() != null) {
            for (var notification : payload.getValue()) {
                syncService.handleWebhook(
                    CalendarProvider.OUTLOOK, 
                    notification.getSubscriptionId(),
                    notification.getChangeType()
                );
            }
        }
        
        return ResponseEntity.accepted().build();
    }
}
```

### Phase 5: Scheduled Sync (Day 6-7)

```java
// CalendarSyncScheduler.java
@Component
@RequiredArgsConstructor
@Slf4j
public class CalendarSyncScheduler {
    
    private final CalendarConnectionRepository connectionRepository;
    private final CalendarSyncService syncService;
    
    /**
     * 5분마다 모든 활성 연결에 대해 Pull Sync 실행
     */
    @Scheduled(fixedRate = 300000) // 5분
    public void scheduledPullSync() {
        List<CalendarConnection> activeConnections = connectionRepository.findByStatus(ConnectionStatus.ACTIVE);
        
        log.info("Starting scheduled sync for {} connections", activeConnections.size());
        
        for (CalendarConnection connection : activeConnections) {
            try {
                syncService.pullSync(connection.getId());
            } catch (Exception e) {
                log.error("Failed to sync connection {}", connection.getId(), e);
                // 에러 카운트 증가, 임계치 초과 시 비활성화
            }
        }
    }
    
    /**
     * Webhook 갱신 (만료 1일 전)
     */
    @Scheduled(cron = "0 0 0 * * *") // 매일 자정
    public void renewWebhooks() {
        LocalDateTime threshold = LocalDateTime.now().plusDays(1);
        List<CalendarConnection> expiringWebhooks = connectionRepository
            .findByWebhookExpirationBefore(threshold);
        
        for (CalendarConnection connection : expiringWebhooks) {
            try {
                syncService.setupWebhook(connection);
            } catch (Exception e) {
                log.error("Failed to renew webhook for connection {}", connection.getId(), e);
            }
        }
    }
}
```

### Phase 6: 테스트 (Day 7-8)
- [ ] 단위 테스트 작성
- [ ] 통합 테스트 작성
- [ ] E2E 테스트 작성

---

## 📁 산출물 (Artifacts)

```
src/main/java/vibe/bizplan/
├── calendar/
│   ├── controller/
│   │   ├── CalendarController.java
│   │   └── CalendarWebhookController.java
│   ├── client/
│   │   ├── GoogleCalendarClient.java
│   │   └── MicrosoftCalendarClient.java
│   ├── dto/
│   │   ├── CalendarEventDto.java
│   │   ├── SyncResult.java
│   │   └── MicrosoftWebhookPayload.java
│   ├── entity/
│   │   ├── CalendarConnection.java
│   │   ├── CalendarEvent.java
│   │   ├── CalendarProvider.java
│   │   └── ConnectionStatus.java
│   ├── repository/
│   │   ├── CalendarConnectionRepository.java
│   │   └── CalendarEventRepository.java
│   ├── service/
│   │   └── CalendarSyncService.java
│   └── scheduler/
│       └── CalendarSyncScheduler.java
```

---

## ✅ 완료 조건 (Acceptance Criteria)

| # | 조건 | 검증 방법 |
|---|------|----------|
| AC-1 | Google Calendar 연결 | OAuth 로그인 후 연결 성공 |
| AC-2 | Outlook Calendar 연결 | OAuth 로그인 후 연결 성공 |
| AC-3 | 이벤트 Pull Sync | 외부 캘린더 이벤트가 DB에 저장됨 |
| AC-4 | 이벤트 Push Sync | 내부 이벤트 생성 시 외부 캘린더에 반영 |
| AC-5 | Webhook 수신 | 외부 변경 시 Webhook 수신 및 동기화 |
| AC-6 | 동기화 지연 < 30초 | Webhook 수신 후 30초 이내 DB 반영 |
| AC-7 | 충돌 해결 | 동시 수정 시 최신 데이터 유지 |
| AC-8 | 에러 복구 | API 실패 시 재시도 로직 동작 |

---

## 🔗 의존성

**선행 작업**:
- #001 (AWS 인프라) - DB 필요
- #002 (OAuth) - 토큰 관리 필요

**후행 작업**:
- #012 (Policy Engine)
- #013 (Focus Blocks)
- #014 (Time Tracking)

**병렬 가능**:
- #007 (Logging)
- #011 (LLM Pipeline)

---

## 🏷️ 라벨
`backend`, `calendar`, `sync`, `google`, `outlook`, `priority:must`, `size:XL`, `difficulty:hard`, `critical-path`

---

## ⚠️ 주의사항 및 리스크

| 리스크 | 영향 | 완화 방안 |
|--------|------|----------|
| API Rate Limit | 동기화 실패 | 지수 백오프, 배치 처리 |
| Webhook 누락 | 데이터 불일치 | Polling Fallback |
| 토큰 만료 | 동기화 중단 | 자동 갱신 로직 |
| 외부 API 장애 | 서비스 영향 | Circuit Breaker 적용 |

---

## 📚 참고 문서
- Task Spec: `tasks/functional/EPIC1-BE-001_Calendar_Sync.md`
- REQ-IDs: REQ-FUNC-001, REQ-FUNC-003
- [Google Calendar API](https://developers.google.com/calendar/api)
- [Microsoft Graph Calendar API](https://docs.microsoft.com/en-us/graph/api/resources/calendar)
