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
  │  │    ├── REST: /albums, /tracks, /artists, /purge │
  │  │    └── S3 (images via S3Utils)                  │
  │  │                                                 │
  │  │  media-service (port 8082)                      │
  │  │    ├── Postgres (rcrs_upload schema)            │
  │  │    ├── S3 (uploads, images, audio)              │
  │  │    ├── Temporal Worker (upload/transcode)       │
  │  │    ├── Kafka Consumer: media.tracnscoding.topic │
  │  │    ├── Kafka Producer: catalog.update.status.topic│
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
  │  │    ├── Proto definitions (30 .proto files)       │
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

#### B5. Track duration always null
**File:** `services/metadata-write-service/src/main/java/org/ultra/rcrs/metadata/service/TrackService.java`
```java
.durationMs(null)  // Hardcoded null, never probed
```
Track duration is never set. The `TrackCreatedEvent` proto also doesn't carry duration. Should be set after audio transcoding probes the file.

#### B6. Docker compose references non-existent services
**File:** `services/docker-compose.yml` (from AGENTS.md — needs confirmation)
References `catalog-service/` and `upload-service/` directories that don't exist.

#### B7. No Dead Letter Topic configuration
**File:** `shared-lib/src/main/java/org/ultra/rcrs/kafka/config/KafkaBaseConfig.java`
`DeadLetterPublishingRecoverer` is imported but never configured as a bean. `FixedBackOff` is also imported but unused. Failed Kafka messages will be silently dropped after the default retry behavior.

### MEDIUM

#### B8. Topic names in AGENTS.md don't match code
The `Topics.java` constants differ from what AGENTS.md documents:
| Code (Topics.java) | AGENTS.md |
|---|---|
| `media.tracnscoding.topic` | `media-start-track-tracnscoding-topic` |
| `catalog.update.status.topic` | `catalog-update-entity-status-topic` |
| (not defined) | `search-start-reindex-topic` |
| (not defined) | `catalog-dlt-topic`, `search-dlt-topic` |

#### B9. Port conflict: media-service and workflow-service both use port 8082
Cannot run both services simultaneously on the same host without changing one.

#### B10. Package naming inconsistency
`discovery-server` uses `ru.ultra.rcrs.discovery` while all other modules use `org.ultra.rcrs`. This causes issues with component scanning and shared-lib usage (discovery-server doesn't depend on shared-lib but if it did, package mismatch would cause issues).

#### B11. Media-service Temporal config references non-existent MEDIA_TASK_QUEUE config
**File:** `services/media-service/src/main/java/org/ultra/rcrs/mediaservice/temporal/config/TemporalConfig.java` needs checking — ensure the task queue constants match between workflow-service and media-service Temporal workers.

#### B12. web-ui is static HTML with no backend connection
The `web-ui/` directory contains static HTML files. The `upload/index.html` is a simple upload page. These have no API integration, auth, or dynamic behavior.

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
- 5 tests total: 4 context-load smoke tests + 1 unit test (Url62Test)
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
metadata-read-service/src/test/java/org/ultra/rcrs/metadata/service/write/AlbumWriteServiceTest.java
metadata-read-service/src/test/java/org/ultra/rcrs/metadata/kafka/listener/CdcEventListenerTest.java
media-service/src/test/java/org/ultra/rcrs/mediaservice/service/AudioServiceTest.java
search-service/src/test/java/org/ultra/rcrs/searchservice/service/PublicSearchServiceTest.java
workflow-service/src/test/java/org/ultra/rcrs/workflow/workflow/impl/AlbumUploadWorkflowImplTest.java
workflow-service/src/test/java/org/ultra/rcrs/workflow/workflow/impl/SagaCompensationTest.java
```
