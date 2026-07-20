# RCRS — Agent Instructions

## Project Overview
Maven multi-module Spring Boot 4.0.3 project (Java 21). Eight modules: shared library, two infrastructure services, and five microservices.

## DO NOT build after completing tasks. The developer handles builds and verification.

## Modules
- `shared-lib` — shared DTOs, Kafka events (Protobuf), enums, exceptions, Base62 URL utils
- `discovery-server` — Netflix Eureka server, port **8761**
- `gateway-api` — Spring Cloud Gateway (WebFlux), port **8084**
- `services/metadata-write-service` — Spring WebMVC, JPA + Postgres, Kafka producer/consumer, port **8080**
- `services/metadata-read-service` — Spring WebFlux, MongoDB Reactive, Kafka consumer (CDC), Caffeine cache, port **8081**
- `services/media-service` — Spring WebMVC, JPA + Postgres, Kafka, S3/SQS via AWS Spring Cloud, port **8082**
- `services/search-service` — Spring WebMVC, Elasticsearch, Kafka, OpenFeign → metadata-write-service, port **8083**
- `services/workflow-service` — Temporal orchestration, OpenFeign clients, Kafka, REST, port **8082**

## Build Commands
```bash
mvn clean install          # build all
mvn test                   # test all
mvn test -pl services/metadata-write-service    # test single module
```

## Architecture
- **CQRS pattern**: `metadata-write-service` writes to Postgres (schema `rcrs_catalog`), publishes **Protobuf** `DomainEvent` messages to both `catalog.cdc.topic` AND `search.index.topic`; `metadata-read-service` consumes `catalog.cdc.topic` and upserts denormalized documents in MongoDB
- `search-service` calls `metadata-write-service` via OpenFeign (hardcoded URL `localhost:8080`)
- All services register with Eureka; gateway routes via `lb://` URIs
- `media-service` generates thumbnails at **64, 300, 640**px, transcodes audio via external `ffmpeg` process
- Jackson 3.x (`tools.jackson.databind`) is used throughout (Spring Boot 4)
- MapStruct is declared in root POM but **no mappers exist yet**
- `workflow-service` orchestrates via Temporal (Spring Boot Starter 1.37.0, managed by root POM) — has workflows, activities, Feign clients, but no database
- Entity IDs are UUIDs in Postgres, encoded as Base62 strings (`Url62`) for APIs and Kafka payloads
- CDC events use **Protobuf** serialization (`ProtobufEventProducer`, `DomainEventOuterClass`)

## Kafka Topics (defined in `shared-lib/.../kafka/Topics.java`)
```
catalog.cdc.topic                               # CQRS: write → read (Protobuf)
search.index.topic                               # write → search (index entities)
search-start-reindex-topic                       # search-service self-consumer
media-start-track-tracnscoding-topic             # write → media (trigger transcoding) [note: typo in topic name is intentional]
catalog-update-entity-status-topic               # media → write (transcoding results) + write self-consumer
catalog-dlt-topic, search-dlt-topic              # dead letter topics
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

## Shared Library
Shared DTOs/enums/events live in `shared-lib/src/main/java/org/ultra/rcrs/`. Protobuf definitions in `shared-lib/src/main/proto/`. When modifying shared classes, rebuild `shared-lib` first:
```bash
mvn install -pl shared-lib -DskipTests
```

## Testing
- 4 context-load smoke tests (gateway-api, media-service, search-service, workflow-service) + 1 unit test (metadata-write-service Url62Test)
- No integration tests, no Testcontainers, no H2, no test profiles
- Tests require the full infrastructure stack running
- `metadata-read-service` has **no tests at all**

## Gotchas
- **Port conflict**: media-service and workflow-service both use port **8082** — cannot run simultaneously on same host
- **Gateway routing mismatch**: gateway routes `lb://catalog-service` for `/api/metadata/**` but no service registers with that name (write=`metadata-write-service`, read=`metadata-read-service`)
- **docker-compose.yml is stale**: `services/docker-compose.yml` references non-existent `catalog-service/` and `upload-service/` directories
- **Package namespace**: discovery-server uses `ru.ultra.rcrs.discovery` while all others use `org.ultra.rcrs`
- **FFmpeg**: media-service invokes `ffmpeg` as external process via `ProcessBuilder`
- **No CI/CD config** exists in the repository
- **No linting/formatting config** exists (no checkstyle, spotless, google-java-format)
- **workflow-service task queue**: `WORKFLOW_TASK_QUEUE` constant — all workflows and activities share this single queue
- **workflow-service Feign clients**: ArtistClient, AlbumClient, TrackClient → metadata-write-service; AudioClient → media-service
- **Topic name typo**: `media-start-track-tracnscoding-topic` ("tracnscoding") — typo is in Topics.java and actual topic name
