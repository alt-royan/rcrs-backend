# RCRS — Agent Instructions

## Project Overview
Maven multi-module Spring Boot 4 project (Java 21). Seven modules total: shared library, two infrastructure services, and four microservices.

## Modules
- `shared-lib` — shared DTOs, Kafka events, enums, exceptions, Base62 URL utils
- `discovery-server` — Netflix Eureka server, port **8761**
- `gateway-api` — Spring Cloud Gateway (WebFlux), port **8084**
- `services/catalog-write-service` — Spring WebMVC, JPA + Postgres, Kafka producer/consumer, port **8080**
- `services/catalog-read-service` — Spring WebFlux, MongoDB Reactive, Kafka consumer (CDC), Caffeine cache, port **8083**
- `services/media-service` — Spring WebMVC, JPA + Postgres, Kafka, S3/SQS via AWS Spring Cloud, port **8081**
- `services/search-service` — Spring WebMVC, Elasticsearch, Kafka, OpenFeign → catalog-read-service, port **8083**

## Build Commands
```bash
./mvnw clean install          # build all (use mvnw, not mvn)
./mvnw test                   # test all
./mvnw test -pl services/catalog-write-service    # test single module
```

## Architecture
- **CQRS pattern**: `catalog-write-service` writes to Postgres, publishes CDC events to `catalog-cdc-topic`, `catalog-read-service` consumes and upserts denormalized documents in MongoDB
- `search-service` calls `catalog-read-service` via OpenFeign (hardcoded URL `localhost:8083`)
- All services register with Eureka; gateway routes via `lb://` URIs
- `media-service` generates thumbnails at **64, 300, 640**px, transcodes audio via external `ffmpeg` process
- Jackson 3.x (`tools.jackson.databind`) is used throughout (Spring Boot 4)
- MapStruct is declared in root POM but **no mappers exist yet**

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

## Shared Library
Shared DTOs/enums/events live in `shared-lib/src/main/java/org/ultra/rcrs/`. When modifying shared classes, rebuild `shared-lib` first:
```bash
./mvnw install -pl shared-lib -DskipTests
```

## Testing
- Only 3 context-load smoke tests exist (gateway-api, media-service, search-service)
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
