# RCRS — Agent Instructions

## Project Overview
Maven multi-module Spring Boot 4 project (Java 21). Eight modules total: shared library, two infrastructure services, and five microservices.

## Modules
- `shared-lib` — shared DTOs, Kafka events, enums, exceptions, Base62 URL utils
- `discovery-server` — Netflix Eureka server, port **8761**
- `gateway-api` — Spring Cloud Gateway (WebFlux), port **8084**
- `services/catalog-write-service` — Spring WebMVC, JPA + Postgres, Kafka producer/consumer, port **8080**
- `services/catalog-read-service` — Spring WebFlux, MongoDB Reactive, Kafka consumer (CDC), Caffeine cache, port **8083**
- `services/media-service` — Spring WebMVC, JPA + Postgres, Kafka, S3/SQS via AWS Spring Cloud, port **8081**
- `services/search-service` — Spring WebMVC, Elasticsearch, Kafka, OpenFeign → catalog-read-service, port **8083**
- `workflow-service` — Temporal orchestration, REST, port **8082**

## Build Commands
```bash
mvn clean install          # build all
mvn test                   # test all
mvn test -pl services/catalog-write-service    # test single module
```

## Architecture
- **CQRS pattern**: `catalog-write-service` writes to Postgres, publishes CDC events to `catalog-cdc-topic`, `catalog-read-service` consumes and upserts denormalized documents in MongoDB
- `search-service` calls `catalog-read-service` via OpenFeign (hardcoded URL `localhost:8083`)
- All services register with Eureka; gateway routes via `lb://` URIs
- `media-service` generates thumbnails at **64, 300, 640**px, transcodes audio via external `ffmpeg` process
- Jackson 3.x (`tools.jackson.databind`) is used throughout (Spring Boot 4)
- MapStruct is declared in root POM but **no mappers exist yet**
- `workflow-service` orchestrates via Temporal (standalone SDK 1.25.x, no Spring Boot starter) — contains zero business logic, no database, no Kafka

## Kafka Topics (defined in `shared-lib/.../kafka/Topics.java`)
```
catalog-cdc-topic                              # CQRS: write → read
search-index-topic                             # write → search (index entities)
search-start-reindex-topic                     # search-service self-consumer
media-start-track-tracnscoding-topic           # write → media (trigger transcoding) [note: typo in topic name]
catalog-update-entity-status-topic             # media → write (transcoding results) + write self-consumer
catalog-dlt-topic, search-dlt-topic            # dead letter topics
```

## Infrastructure (local/)
All infra is Docker-based in `local/`. Start required services before running apps.

| Service | Port | Directory | Required By |
|---------|------|-----------|-------------|
| PostgreSQL | 5432 | `local/postgres/` | catalog-write, media |
| MongoDB | 27017 | `local/mongodb/` | catalog-read |
| Elasticsearch | 9200 | `local/elk/` | search |
| Kafka + Zookeeper | 9092/2181 | `local/kafka/` | all services |
| Kafka UI | 9091 | `local/kafka/` | — |
| LocalStack (S3+SQS) | 4566 | `local/localstack/` | media |
| S3 Ninja (local S3) | 9444 | `local/s3/` | catalog-write (image URLs) |
| Cassandra | 9042 | `local/cassandra/` | **unused** |
| Redis | 6379 | `local/redis/` | **unused** |
| Temporal Server | 7233 | `local/temporal/` | workflow-service |
| Temporal UI | 8085 | `local/temporal/` | — |
| Temporal Postgres | 5433 | `local/temporal/` | workflow-service |

## Shared Library
Shared DTOs/enums/events live in `shared-lib/src/main/java/org/ultra/rcrs/`. When modifying shared classes, rebuild `shared-lib` first:
```bash
mvn install -pl shared-lib -DskipTests
```

## Testing
- Only 4 context-load smoke tests exist (gateway-api, media-service, search-service, workflow-service)
- No unit/integration tests, no Testcontainers, no H2, no test profiles
- Tests require the full infrastructure stack running
- `catalog-write-service` and `catalog-read-service` have **no tests at all**

## Gotchas
- **Port conflict**: search-service and catalog-read-service both use port **8083** — cannot run simultaneously on same host
- **Gateway routing mismatch**: gateway routes `lb://catalog-service` but no service registers with that name (write=`catalog-write-service`, read=`catalog-read-service`)
- **docker-compose.yml is stale**: `services/docker-compose.yml` references non-existent `catalog-service/` and `upload-service/` directories
- **Package namespace**: discovery-server uses `ru.ultra.rcrs.discovery` while all others use `org.ultra.rcrs`
- **FFmpeg**: media-service invokes `ffmpeg` as external process via `ProcessBuilder`
- **No CI/CD config** exists in the repository
- **workflow-service Temporal dependency**: version `1.25.1` is in workflow-service POM, not managed by root POM's `dependencyManagement`
- **workflow-service task queue**: `metadata-task-queue` — all workflows and activities share this single queue
- **workflow-service external URLs**: service base URLs in `application.yml` default to `localhost` ports that may not match actual service ports
