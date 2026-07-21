# RCRS — TODO

## Architecture

```
                         ┌───────────────┐
                         │  Discovery    │
                         │  (Eureka)     │
                         │  port 8761    │
                         └───────┬───────┘
                                 │
                    ┌────────────┼────────────┐
                    │            │            │
              ┌─────┴─────┐  ┌──┴──────┐  ┌──┴──────┐
              │  Gateway  │  │  Web UI │  │  Infra  │
              │  port 8084│  │  (HTML) │  │  Docker │
              └─────┬─────┘  └─────────┘  └──┬──────┘
                    │                         │
        ┌───────────┼───────────┐             │
        │           │           │             │
  ┌─────┴─────┐ ┌───┴─────┐ ┌──┴──────┐      │
  │ /api/     │ │ /api/   │ │ /api/   │      │
  │ metadata/*│ │ upload/*│ │ search/*│      │
  └─────┬─────┘ └───┬─────┘ └──┬──────┘      │
        │           │          │              │
  ┌─────┴───────────┴──────────┴──────┐       │
  │   Internal Service Communication │       │
  └────────────────────────────────────┘       │
        │                                      │
  ┌─────┴──────────────────────────────────────┘
  │
  │  ┌─────────────────────────────────────────────────┐
  │  │              WRITE SIDE (CQRS)                  │
  │  │                                                 │
  │  │  metadata-write-service (port 8080)             │
  │  │    ├── Postgres (rcrs_catalog schema)           │
  │  │    ├── Kafka Producer → catalog.cdc.topic       │
  │  │    │               → search.index.topic         │
  │  │    ├── Kafka Consumer: catalog.update.status.topic│
  │  │    │   (CatalogUpdateStatusListener)            │
  │  │    ├── REST: /albums, /tracks, /artists, /purge │
  │  │    └── S3 (images via S3Utils)                  │
  │  │                                                 │
  │  │  media-service (port 8082)                      │
  │  │    ├── Postgres (rcrs_upload schema)            │
  │  │    ├── S3 (uploads, images, audio)              │
  │  │    ├── Temporal Worker (upload/transcode)       │
  │  │    ├── Kafka Consumer: media.tracnscoding.topic │
  │  │    ├── Kafka Producer: catalog.update.status.topic│
  │  │    │   (TrackUpdateLifecycleStatusEvent,        │
  │  │    │    TrackTranscodingCompletedEvent)          │
  │  │    ├── SQS Listener (S3 events)                 │
  │  │    ├── REST: /upload/files, /upload/audio       │
  │  │    └── ffmpeg external process                  │
  │  │                                                 │
  │  │  search-service (port 8083)                     │
  │  │    ├── Elasticsearch                            │
  │  │    ├── Kafka Consumer: search.index.topic       │
  │  │    ├── OpenFeign → metadata-write-service       │
  │  │    └── REST: /search, /admin/search             │
  │  │                                                 │
  │  └─────────────────────────────────────────────────┘
  │
  │  ┌─────────────────────────────────────────────────┐
  │  │              READ SIDE (CQRS)                   │
  │  │                                                 │
  │  │  metadata-read-service (port 8081)              │
  │  │    ├── MongoDB (reactive)                       │
  │  │    ├── Kafka Consumer: catalog.cdc.topic        │
  │  │    ├── Caffeine Cache                           │
  │  │    ├── REST (admin): /albums, /tracks, /artists │
  │  │    └── REST (public): same, filtered by status  │
  │  │                                                 │
  │  └─────────────────────────────────────────────────┘
  │
  │  ┌─────────────────────────────────────────────────┐
  │  │              ORCHESTRATION                      │
  │  │                                                 │
  │  │  workflow-service (port 8082)                   │
  │  │    ├── Temporal Workflows & Activities          │
  │  │    ├── OpenFeign → metadata-write, media, search│
  │  │    ├── Kafka Producer                          │
  │  │    └── REST: /albums, /tracks, /artists         │
  │  │                                                 │
  │  └─────────────────────────────────────────────────┘
  │
  │  ┌─────────────────────────────────────────────────┐
  │  │              SHARED LIBRARY                     │
  │  │                                                 │
  │  │  shared-lib                                     │
  │  │    ├── Enums (AlbumType, ArtistRole, EntityStatus,│
  │  │    │         LifecycleStatus, FileStatus, Order) │
  │  │    ├── Exceptions (BadRequest, NotFound, etc.)   │
  │  │    ├── Kafka (Topics, ProtobufEventProducer,     │
  │  │    │         KafkaBaseConfig)                    │
  │  │    ├── Proto definitions (31 .proto files)       │
  │  │    ├── Pipeline (Handler, Pipeline)              │
  │  │    └── Utils (Base62, Url62, S3Utils,            │
  │  │               UuidConverter, BigIntegerPairing)  │
  │  │                                                 │
  │  └─────────────────────────────────────────────────┘
```

---

## Bugs

### CRITICAL

#### B1. AlbumChangeAvailabilityStatusWorkflowImpl calls wrong activity ✅
**File:** `services/workflow-service/src/main/java/org/ultra/rcrs/workflow/workflow/impl/AlbumChangeAvailabilityStatusWorkflowImpl.java`
```java
case ACTIVE -> activityFactory.artistActivity().activeArtist(id);   // BUG: calls artist instead of album
case HIDDEN -> activityFactory.artistActivity().hideArtist(id);     // BUG: calls artist instead of album
case DELETED -> activityFactory.artistActivity().markArtistDeleted(id); // BUG: calls artist instead of album
```
All three cases use `artistActivity()` instead of `albumActivity()`. This means changing an album's availability status actually changes the artist's status.

#### B2. Workflow-service Feign URL for media-service points to wrong port ✅
**File:** `services/workflow-service/src/main/resources/application.yml`
```yaml
feign:
  media-service:
    url: http://localhost:8081  # BUG: media-service runs on port 8082
```
Correct URL should be `http://localhost:8082`.

#### B3. SqsS3Listener annotated as Kafka listener but expects SQS Message ✅
**File:** `services/media-service/src/main/java/org/ultra/rcrs/mediaservice/listener/SqsS3Listener.java`
```java
@KafkaListener(topics = Topics.MEDIA_TRANSCODING_TOPIC, ...)  // Kafka annotation
public void handleEventObjectPut(Message message) {            // SQS Message type
```
The listener is annotated with `@KafkaListener` (expects Kafka ConsumerRecord of bytes) but the method parameter is `Message` from the SQS SDK. This will fail at runtime with deserialization errors. Additionally, it listens to the transcoding topic but parses S3 event notifications — the wrong abstraction entirely.

#### B4. Gateway routes to non-existent service name
**File:** `gateway-api/src/main/java/org/ultra/rcrs/gatewayapi/GatewayConfig.java`
```java
.uri("lb://catalog-service")  // No service registers with this name
```
Services register as `metadata-write-service` and `metadata-read-service`. The route should target `metadata-write-service`. The `/api/metadata/**` path is also ambiguous — there are two metadata services (write + read) and no rule to distinguish them.

### HIGH

#### B5. Track duration always null ✅
**File:** `services/metadata-write-service/src/main/java/org/ultra/rcrs/metadata/service/TrackService.java`
Track duration is now set after audio transcoding completes. `MediaEventProducer` sends `TrackTranscodingCompletedEvent` (with `duration_ms`) to `catalog.update.status.topic`. `CatalogUpdateStatusListener` in metadata-write-service consumes it and calls `TrackService.handleTranscodingCompleted()` which updates `lifecycle_status` and `duration_ms` in a single query.

#### B6. Docker compose references non-existent services
**File:** `services/docker-compose.yml` (from AGENTS.md — needs confirmation)
References `catalog-service/` and `upload-service/` directories that don't exist.

#### B7. No Dead Letter Topic configuration ✅
Fixed: single `global.dlq` topic. `DeadLetterPublishingRecoverer` configured in `KafkaBaseConfig`, retries 3 times (1s interval, backoff 2) then sends to DLQ. Applied to both `byteArrayContainerFactory` and `stringContainerFactory`.

### MEDIUM

#### B8. Topic names in AGENTS.md don't match code ✅
Fixed: AGENTS.md topics section updated to match `Topics.java` constants and current event flow.

#### B9. Port conflict: media-service and workflow-service both use port 8082 ✅
Fixed: workflow-service now runs on port 8090.

#### B10. Package naming inconsistency ✅
Fixed: `discovery-server` now uses `org.ultra.rcrs.discovery`.

#### B11. Media-service Temporal config references non-existent MEDIA_TASK_QUEUE config ✅
`MEDIA_TASK_QUEUE` is a Java constant in `TemporalConfig.java`, not YAML config. Used consistently across all workflows, worker, listener, and controller.

#### B12. web-ui is static HTML with no backend connection ✅
Intentional — static HTML, no API integration planned for now.

---

## Missing Features (Spotify-like)

### Domain Features

| Priority | Feature | Description | Affected Modules |
|----------|---------|-------------|-----------------|
| P0 | **User Management** | Registration, login, profiles, roles (admin/user) | All |
| P0 | **Authentication/Authorization** | JWT/OAuth2, Spring Security, endpoint protection | gateway, all services |
| P0 | **Playlists** | CRUD for playlists, add/remove tracks, public/private, collaborative | metadata-write, metadata-read, shared-lib |
| P1 | **Favorites/Likes** | Like tracks, albums, artists; library view | metadata-read |
| P1 | **Streaming Endpoint** | Audio streaming with range requests, adaptive bitrate | media-service |
| P1 | **Follow Artists** | Subscribe to artists, notifications on new releases | metadata-read, workflow |
| P2 | **Album/Track Release Workflow** | Draft → Review → Publish lifecycle, release date scheduling | workflow-service |
| P2 | **Radio / Auto-play** | Generate continuous playback based on seed tracks/artists | search-service |
| P2 | **Lyrics** | Timed lyrics, synced lyrics display, edit | metadata-write, metadata-read |
| P2 | **Comments/Reviews** | User comments on tracks/albums | new service or metadata-write |
| P3 | **Sharing** | Share links (track/album/playlist), embed codes | metadata-read |
| P3 | **Queue Management** | Server-side play queue for users | new service |
| P3 | **Download/Offline** | Download tracks for offline playback, DRM | media-service |
| P3 | **Charts/Trending** | Top tracks, albums, artists by region/timeframe | search-service |
| P3 | **Recommendations** | ML-based recommendations, similar artists | search-service + new |
| P3 | **Genre/Tag System** | Hierarchical genres, mood/activity tags | metadata-write, search |

### Infrastructure Features

| Priority | Feature | Description |
|----------|---------|-------------|
| P0 | **CI/CD** | GitHub Actions / GitLab CI for build, test, deploy |
| P0 | **Linting/Formatting** | Spotless/Checkstyle/PMD for consistent code style |
| P1 | **API Versioning** | URL or header-based API versioning (`/v1/albums`) |
| P1 | **Rate Limiting** | Per-user/IP rate limiting in gateway |
| P1 | **OpenAPI/Swagger UI** | Springdoc configured for all services |
| P2 | **Health Indicators** | Custom health checks (Kafka, DB, S3, Elasticsearch) |
| P2 | **Distributed Tracing** | Micrometer + Zipkin/Jaeger for request tracing |
| P2 | **Audit Logging** | Track who did what and when |
| P3 | **Graceful Shutdown** | Proper shutdown hooks, draining connections |
| P3 | **Feature Flags** | Toggle features without deployment |

### Data/Storage Features

| Priority | Feature | Description |
|----------|---------|-------------|
| P1 | **Soft Delete + Hard Purge** | Soft-delete flow is partially implemented; hard purge via monthly cron exists but needs testing |
| P1 | **Track Audio Features** | Store BPM, key, energy, danceability |
| P2 | **Album versions** | Deluxe, explicit, clean versions |
| P2 | **Multi-artist track roles** | "Featuring", "&", producer, writer roles beyond MAIN/FEATURED |

---

## Test Improvement Suggestions

### Current State
- metadata-read-service has **zero tests**
- No integration tests, no Testcontainers, no H2
- Tests require full infrastructure stack

### Suggested Test Strategy

#### Phase 1: Unit Tests (no infra needed)

| Module | What to Test | Priority | Done |
|--------|-------------|----------|---|
| shared-lib | `Base62` encode/decode roundtrip, edge cases (UUID max, min, zero) | P0 | ✅ |
| shared-lib | `Url62` encode/decode, exception paths | P0 | ✅ |
| shared-lib | `UuidConverter` toBigInteger / toUuid roundtrip | P0 | ✅ |
| shared-lib | `BigIntegerPairing` | P1 | ✅ |
| metadata-write | Service layer with mocked repositories: `AlbumService.createAlbum`, `TrackService.createTrack` | P0 |  ✅ |
| metadata-write | Status transitions: valid and invalid | P1 | ✅  |
| media-service | `Hash.sha1Base64` | P1 | ✅  |
| media-service | `AudioService.getPreSignUrl` with mocked S3Presigner | P1 | ✅ |
| search-service | DTO mapping, `SearchCollection` pagination logic | P1 |  ✅ |

#### Phase 2: Integration Tests (Testcontainers)

| Test | What to Verify | Priority | Infra Needed |Done |
|------|---------------|----------|-------------|----------|
| metadata-write | JPA repository CRUD + cascading | P0 | Postgres |
| metadata-write | Kafka event emission after DB write | P0 | Postgres + Kafka |
| metadata-read | MongoDB document upsert from CDC event | P0 | MongoDB + Kafka |
| metadata-read | REST endpoints return correct data | P0 | MongoDB |
| media-service | S3 presigned URL generation flow | P1 | S3 (MinIO) |
| media-service | Audio upload → status tracking | P1 | Postgres + S3 |
| search-service | Elasticsearch document indexing + search | P0 | Elasticsearch |
| workflow-service | Temporal workflow execution end-to-end | P1 | Temporal Server |
| workflow-service | Saga compensation on failure | P1 | Temporal + all services |

#### Phase 3: Contract / API Tests

| Test | What to Verify |
|------|---------------|
| Gateway routing | All `lb://` routes resolve to correct services |
| OpenFeign clients | workflow-service → metadata-write, media-service API contracts |
| Protobuf serialization | DomainEvent roundtrip: Java object → Proto → bytes → Proto → Java |
| CDC pipeline | Write (Postgres) → Kafka → Read (MongoDB) consistency |

#### Phase 4: E2E / Smoke Tests

| Test | What to Verify |
|------|---------------|
| Full upload flow | Artist create → Album create with tracks → Audio upload → Transcoding → Status update |
| Search flow | Create entity → Verify in search results |
| Status cascade | Hide artist → verify albums & tracks also hidden |
| Purge flow | Mark deleted → Monthly cron purges permanently |

### Recommended Tooling

```
Testcontainers   → Postgres, MongoDB, Kafka, Elasticsearch, Temporal (all via local/ infra)
Mockito          → Mock services, repositories, Feign clients, S3 presigner
REST Assured     → Controller integration tests
Spring Cloud Contract → Verify Feign client compatibility
ArchUnit         → Enforce package structure, layered architecture rules
```

### Specific Test Files to Create

```
shared-lib/src/test/java/org/ultra/rcrs/utils/Base62Test.java
shared-lib/src/test/java/org/ultra/rcrs/utils/UuidConverterTest.java
metadata-write-service/src/test/java/org/ultra/rcrs/metadata/service/AlbumServiceTest.java
metadata-write-service/src/test/java/org/ultra/rcrs/metadata/service/TrackServiceTest.java
metadata-write-service/src/test/java/org/ultra/rcrs/metadata/kafka/CatalogEventProducerTest.java
metadata-write-service/src/test/java/org/ultra/rcrs/metadata/kafka/CatalogUpdateStatusListenerTest.java
metadata-read-service/src/test/java/org/ultra/rcrs/metadata/service/write/AlbumWriteServiceTest.java
metadata-read-service/src/test/java/org/ultra/rcrs/metadata/kafka/listener/CdcEventListenerTest.java
media-service/src/test/java/org/ultra/rcrs/mediaservice/service/AudioServiceTest.java
search-service/src/test/java/org/ultra/rcrs/searchservice/service/PublicSearchServiceTest.java
workflow-service/src/test/java/org/ultra/rcrs/workflow/workflow/impl/AlbumUploadWorkflowImplTest.java
workflow-service/src/test/java/org/ultra/rcrs/workflow/workflow/impl/SagaCompensationTest.java
```

---

## Development Roadmap (20 Tasks)

### Phase 1: Critical Fixes

#### 5. Add Gateway-Level Authentication and Rate Limiting
**Category:** Security | **Priority:** Critical | **Difficulty:** Medium

**Problem:** Gateway has no auth or rate limiting. Internal services are exposed to unauthenticated requests.

**Goal:** Authenticate all requests at gateway, add rate limiting via Redis.

**Implementation Steps:**
1. Add `spring-cloud-starter-security` + `oauth2-resource-server` to gateway
2. Configure JWT bearer token validation
3. Add `RequestRateLimiter` filter using Redis (already deployed)
4. Propagate user context via headers

**Affected Files:** `gateway-api/pom.xml`, new `GatewaySecurityConfig.java`, `GatewayConfig.java`

---

### Phase 2: Product Improvements

#### 6. Implement Playlist CRUD Service
**Category:** Feature | **Priority:** High | **Difficulty:** Hard

**Problem:** Zero playlist functionality in a music streaming service.

**Goal:** Full playlist CRUD with tracks.

**Implementation Steps:**
1. Create `Playlist` and `PlaylistTrack` entities in metadata-write-service
2. Create `PlaylistService` and `PlaylistWriteController`
3. Create Protobuf events for CDC
4. Create `PlaylistPublicDocument` in metadata-read-service

**Affected Files:** New entities, service, controller, proto files, read-model

---

#### 7. Implement Audio Streaming Endpoint
**Category:** Feature | **Priority:** High | **Difficulty:** Hard

**Problem:** No way to play audio tracks. Media-service stores transcoded audio but no streaming endpoint.

**Goal:** HTTP range-request streaming for audio playback.

**Implementation Steps:**
1. Add `GET /audio/stream/{trackId}` with S3 streaming
2. Support HTTP `Range` headers for seeking
3. Return proper `Content-Type`, `Accept-Ranges`, `Content-Range` headers

**Affected Files:** `services/media-service/.../AudioController.java`, `services/media-service/.../AudioService.java`

---

#### 8. Implement Favorites/Likes System
**Category:** Feature | **Priority:** High | **Difficulty:** Medium

**Problem:** Users cannot like tracks, albums, or artists.

**Goal:** Like/unlike entities and view library.

**Implementation Steps:**
1. Create `UserFavorite` entity in user-service
2. Create REST endpoints: `POST/DELETE /users/favorites`, `GET /users/favorites`
3. Publish like/unlike events for read-side updates

**Affected Files:** `services/user-service/.../model/UserFavorite.java`, new controller and service

---

#### 9. Implement Recently Played and Listening History
**Category:** Feature | **Priority:** Medium | **Difficulty:** Medium

**Problem:** No recently played tracks, no playback resume support.

**Goal:** Track listening history with position.

**Implementation Steps:**
1. Create `ListeningHistory` entity
2. Endpoints: `POST /users/history`, `GET /users/history/recently-played`, `PUT /users/history/{id}/position`
3. Add TTL cleanup (90 days)

**Affected Files:** New controller and service in user-service

---

#### 10. Add User Profile and Settings Service
**Category:** Feature | **Priority:** Medium | **Difficulty:** Medium

**Problem:** User entity has only basic fields. No profile/avatar/settings REST API.

**Goal:** Comprehensive user profile and settings.

**Implementation Steps:**
1. Extend `User` with `displayName`, `avatarS3Key`, `bio`, `country`, `locale`
2. Add `UserSettings` entity
3. Add REST endpoints for profile and settings CRUD

**Affected Files:** `services/user-service/.../model/User.java`, new `UserSettings.java`, new controller

---

#### 11. Implement Audio Transcoding Progress
**Category:** UX | **Priority:** Medium | **Difficulty:** Medium

**Problem:** No way to track transcoding progress. Files just appear as completed.

**Goal:** Real-time upload/transcoding status.

**Implementation Steps:**
1. Add `GET /upload/audio/{uid}/status` endpoint
2. Publish intermediate status events via Kafka
3. Add SSE/WebSocket streaming for real-time updates

**Affected Files:** `services/media-service/.../AudioController.java`, `services/media-service/.../AudioService.java`

---

### Phase 3: Scaling and Production Readiness

#### 12. Implement CI/CD Pipeline
**Category:** DevOps | **Priority:** High | **Difficulty:** Medium

**Problem:** Zero CI/CD. Manual builds. Docker Compose has commented services.

**Goal:** Automated build, test, and deploy pipeline.

**Implementation Steps:**
1. Create `.github/workflows/ci.yml` (build + unit tests + Docker images)
2. Create `.github/workflows/deploy.yml` (tag-based deploy)
3. Create missing Dockerfiles
4. Fix `docker-compose.yml` — uncomment services, fix port mappings

**Affected Files:** `.github/workflows/*.yml`, Dockerfiles for all services, `local/docker-compose.yml`

---

#### 13. Add Distributed Tracing
**Category:** Performance | **Priority:** Medium | **Difficulty:** Medium

**Problem:** No request tracing across 10 microservices. Debugging requires manual log correlation.

**Goal:** End-to-end tracing with Zipkin.

**Implementation Steps:**
1. Add `micrometer-tracing-bridge-brave` + `zipkin-reporter-brave` to all services
2. Add Zipkin container to Docker Compose
3. Configure tracing sampling in each service

**Affected Files:** Root `pom.xml`, all service POMs and `application.yml`, `local/infra/docker-compose.yml`

---

#### 14. Implement API Versioning
**Category:** Architecture | **Priority:** Medium | **Difficulty:** Easy

**Problem:** No API versioning. Breaking changes break all clients.

**Goal:** URL-based versioning (`/api/v1/...`).

**Implementation Steps:**
1. Add version prefix to all controllers
2. Update gateway routes
3. Update Feign client URLs

**Affected Files:** All controllers, gateway routes, Feign configs

---

#### 15. Add Global Exception Handling
**Category:** Backend | **Priority:** High | **Difficulty:** Easy

**Problem:** Inconsistent error formats across services.

**Goal:** Standardized `ErrorResponse` across all APIs.

**Implementation Steps:**
1. Create shared `ErrorResponse` DTO in shared-lib
2. Create shared `@ControllerAdvice` base
3. Each service extends/imports shared handler

**Affected Files:** `shared-lib/.../exceptions/ErrorResponse.java`, `shared-lib/.../exceptions/GlobalExceptionHandler.java`, all controllers

---

#### 16. Standardize Pagination
**Category:** Backend | **Priority:** Medium | **Difficulty:** Easy

**Problem:** Inconsistent pagination: `Page`, `SearchCollection`, raw lists.

**Goal:** Unified `PageResponse<T>` across all endpoints.

**Implementation Steps:**
1. Create shared `PageResponse` in shared-lib
2. Add `PageRequest` validation helper
3. Update all list endpoints

**Affected Files:** `shared-lib/.../dto/PageResponse.java`, all list-returning controllers

---

#### 17. Cache Search Results with Caffeine
**Category:** Performance | **Priority:** Medium | **Difficulty:** Easy

**Problem:** Elasticsearch queried for every request with no caching.

**Goal:** Cache popular searches with 5-minute TTL.

**Implementation Steps:**
1. Add `spring-boot-starter-cache` to search-service
2. Configure Caffeine cache: max 1000 entries, 5-min expiry
3. Annotate search methods with `@Cacheable`
4. Evict cache on entity reindex

**Affected Files:** `services/search-service/pom.xml`, new `SearchServiceConfig.java`, `PublicSearchService.java`, `AdminSearchService.java`

---

#### 18. Implement CDN Strategy for Media
**Category:** Performance | **Priority:** High | **Difficulty:** Hard

**Problem:** All media served directly from S3 — slow for global users, expensive.

**Goal:** CDN-based media delivery with caching.

**Implementation Steps:**
1. Configure CloudFront distributions for audio and image buckets
2. Add cache headers (`Cache-Control`, `ETag`)
3. Implement cache invalidation on asset changes

**Affected Files:** `services/media-service/.../AudioService.java`, CDN config

---

### Phase 4: Advanced Spotify-Level Features

#### 19. Integrate Kafka Schema Registry
**Category:** Architecture | **Priority:** Medium | **Difficulty:** Hard

**Problem:** No schema registry. Schema evolution breaks consumers.

**Goal:** Confluent Schema Registry for Protobuf event governance.

**Implementation Steps:**
1. Add Schema Registry container
2. Add `kafka-protobuf-serializer` dependency
3. Update producer/consumer factories
4. Replace manual parsing with registry deserialization

**Affected Files:** `local/infra/docker-compose.yml`, `shared-lib/pom.xml`, `KafkaBaseConfig.java`, `ProtobufEventProducer.java`, all listeners

---

#### 20. Add Elasticsearch ILM and Health Management
**Category:** DevOps | **Priority:** Medium | **Difficulty:** Medium

**Problem:** 4GB ES heap for dev, no index lifecycle, fragile init script.

**Goal:** Optimized ES with proper index lifecycle.

**Implementation Steps:**
1. Reduce ES heap to 1GB for dev
2. Create ILM policy (30GB/30d rollover, 90d delete)
3. Convert `init-indices.sh` to Spring Boot `ApplicationRunner`
4. Add custom health indicator for ES
5. Add reindex trigger endpoint

**Affected Files:** `local/infra/docker-compose.yml`, `services/search-service/.../config/IndexInitializer.java`, `services/search-service/.../controller/SearchController.java`, `local/elk/init-indices.sh`

---

## Roadmap

| Phase | Tasks | Focus |
|-------|-------|-------|
| **Phase 1: Critical Fixes** | 1–5 | Bug fixes and security — deployable baseline |
| **Phase 2: Product Improvements** | 6–11 | Core music features — playlists, streaming, likes, profiles |
| **Phase 3: Scaling & Production** | 12–18 | CI/CD, tracing, caching, CDN — production readiness |
| **Phase 4: Advanced Features** | 19–20 | Schema registry, ES management — enterprise features |
```
