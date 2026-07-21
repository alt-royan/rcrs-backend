# RCRS — Agent Instructions

## Project Overview
Maven multi-module Spring Boot 4.0.3 project (Java 21). Eight modules: shared library, two infrastructure services, and five microservices. Spotify-like music catalog system with CQRS, event-driven CDC, Temporal orchestration, and media processing.

## DO NOT build after completing tasks. The developer handles builds and verification.

## Modules
| Module | Port | Description |
|--------|------|-------------|
| `shared-lib` | — | DTOs, Protobuf events, enums, exceptions, Base62 utils, Kafka config |
| `discovery-server` | 8761 | Netflix Eureka server |
| `gateway-api` | 8084 | Spring Cloud Gateway (WebFlux) — routes: `/api/metadata/**`, `/api/upload/**`, `/api/search/**` |
| `services/metadata-write-service` | 8080 | WebMVC, JPA + Postgres (`rcrs_catalog`), Kafka producer — writes |
| `services/metadata-read-service` | 8081 | WebFlux, MongoDB Reactive, Kafka consumer (CDC), Caffeine cache — reads |
| `services/media-service` | 8082 | WebMVC, JPA + Postgres (`rcrs_upload`), S3, Temporal worker, ffmpeg — file uploads/transcoding |
| `services/search-service` | 8083 | WebMVC, Elasticsearch, Kafka consumer, OpenFeign → metadata-write |
| `services/workflow-service` | 8082 | Temporal orchestration, OpenFeign clients, Kafka, REST — coordinates upload flows |

**Port conflict**: media-service and workflow-service both use port **8082** — cannot run simultaneously on same host.

## Build Commands
```bash
mvn clean install          # build all
mvn test                   # test all
mvn test -pl services/metadata-write-service    # test single module
mvn install -pl shared-lib -DskipTests          # rebuild shared-lib after changes
```

## Architecture
- **CQRS pattern**: `metadata-write-service` writes to Postgres (schema `rcrs_catalog`), publishes **Protobuf** `DomainEvent` messages to both `catalog.cdc.topic` AND `search.index.topic`; `metadata-read-service` consumes `catalog.cdc.topic` and upserts denormalized documents in MongoDB
- `search-service` calls `metadata-write-service` via OpenFeign (hardcoded URL `localhost:8080`)
- All services register with Eureka; gateway routes via `lb://` URIs
- `media-service` generates thumbnails at **64, 300, 640**px, transcodes audio via external `ffmpeg` process
- Jackson 3.x (`tools.jackson.databind`) is used throughout (Spring Boot 4)
- MapStruct is declared in root POM but **no mappers exist yet**
- `workflow-service` orchestrates via Temporal (Spring Boot Starter 1.37.0) — 6 workflows, 7 activity interfaces, Feign clients, no database
- Entity IDs are UUIDs in Postgres, encoded as Base62 strings (`Url62`) for APIs and Kafka payloads
- CDC events use **Protobuf** serialization (`ProtobufEventProducer`, `DomainEventOuterClass`)

## Kafka Topics (defined in `shared-lib/.../kafka/Topics.java`)
```
catalog.cdc.topic                 # CQRS: write → read (Protobuf) — also search.index duplicate
search.index.topic                 # write → search (index entities)
media.tracnscoding.topic          # write → media (trigger transcoding) — typo is in code
catalog.update.status.topic       # media → write (transcoding results) + write self-consumer
```
No DLT topics are currently configured despite `DeadLetterPublishingRecoverer` being imported.

## Gateway Routes (defined in `GatewayConfig.java`)
```
/api/metadata/**  → lb://catalog-service         # BUG: should be metadata-write-service (no service registered as catalog-service)
/api/upload/**    → lb://media-service           # OK
/api/search/**    → lb://search-service          # OK
```

## Infrastructure (local/)
All infra is Docker-based in `local/`. Start required services before running apps.

| Service | Port | Directory | Required By |
|---------|------|-----------|-------------|
| PostgreSQL | 5432 | `local/postgres/` | metadata-write, media |
| MongoDB | 27017 | `local/mongodb/` | metadata-read |
| Elasticsearch | 9200 | `local/elk/` | search |
| Kafka + Zookeeper | 9092/2181 | `local/kafka/` | all services |
| Kafka UI | 9091 | `local/kafka/` | — |
| S3 Ninja | 9444 | `local/s3/` | metadata-write, media |
| Redis | 6379 | `local/redis/` | **unused** |
| Temporal Server | 7233 | `local/temporal/` | workflow-service |
| Temporal UI | 8888 | `local/temporal/` | — |
| Temporal Postgres | 5454 | `local/temporal/` | workflow-service |

## Testing
- 5 tests total: 4 context-load smoke tests + 1 unit test (Url62Test)
- `metadata-read-service` has **zero tests**
- No integration tests, no Testcontainers, no H2, no test profiles
- Tests require the full infrastructure stack running

## Known Bugs (see TODO.md for details)
- **B1**: `AlbumChangeAvailabilityStatusWorkflowImpl` calls `artistActivity()` instead of `albumActivity()` on all 3 branches
- **B2**: Workflow-service Feign URL for media-service points to `localhost:8081` instead of `:8082`
- **B3**: `SqsS3Listener` annotated `@KafkaListener` but expects SQS `Message` parameter type
- **B4**: Gateway routes `lb://catalog-service` — no service registers with that name

## Gotchas
- **Package namespace**: discovery-server uses `ru.ultra.rcrs.discovery` while all others use `org.ultra.rcrs`
- **workflow-service Feign URLs** (from `application.yml`): metadata-service→`localhost:8080`, media-service→`localhost:8081` **(BUG)**, search-service→`localhost:8083`
- **FFmpeg**: media-service invokes `ffmpeg` as external process via `ProcessBuilder`
- **No CI/CD, no linting/formatting config** exists
- **workflow-service task queue**: `WORKFLOW_TASK_QUEUE` constant — all workflows and activities share this single queue
- **Track duration** is hardcoded `null` at creation — never probed from audio metadata
- **web-ui/** is static HTML with no API integration
- **Topic name typo**: `media.tracnscoding.topic` ("tracnscoding") — typo is in `Topics.java` constant and actual topic name
