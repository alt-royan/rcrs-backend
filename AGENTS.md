# RCRS — Agent Instructions

## Project Overview
Maven multi-module Spring Boot 4 project (Java 21). Three microservices + shared library.

## Modules
- `shared-lib` — shared DTOs, Kafka events, exceptions, utilities
- `services/catalog-service` — Spring WebFlux, R2DBC (reactive Postgres), Kafka consumer, port **8080**
- `services/media-service` — Spring Web (MVC), JPA, S3/SQS via AWS Spring Cloud, port **8081**
- `services/upload-service` — Spring WebMVC, Thymeleaf, OpenFeign client, port **8082**

## Build Commands
```bash
./mvnw clean install          # build all (use mvnw, not mvn)
./mvnw test                   # test all
./mvnw test -pl services/catalog-service    # test single module
./mvnw test -pl services/media-service -DskipTests=false
```

## Infrastructure (local/)
All infra is Docker-based. Start required services before running apps.

| Service | Port | Compose file |
|---------|------|-------------|
| PostgreSQL | 5432 | `local/postgres/docker-compose.yaml` |
| Kafka + Zookeeper | 9092/2181 | `local/kafka/docker-compose.yaml` |
| Kafka UI | 9091 | `local/kafka/docker-compose.yaml` |
| LocalStack (S3+SQS) | 4566 | `local/localstack/docker-compose.yaml` |
| S3 Ninja (local S3) | 9444 | `local/s3/docker-compose.yaml` |
| Cassandra | 9042 | `local/cassandra/docker-compose.yaml` (optional) |

## Key Architecture Notes
- `catalog-service` uses **reactive** stack (WebFlux + R2DBC); `media-service` and `upload-service` use **blocking** stack
- `upload-service` calls `media-service` and `catalog-service` via **OpenFeign** (hardcoded URLs in `application.properties`)
- All services use **Liquibase** for DB migrations (`db/changelog/db.changelog-master.yaml`)
- `catalog-service` sends events to Kafka topics; `media-service` consumes and produces
- media-service generates thumbnails at sizes **64, 300, 640**px
- `catalog-service` stores images via S3 Ninja endpoint (`http://localhost:9444`)
- `media-service` uses LocalStack for SQS/S3 (`http://localhost.localstack.cloud:4566`)

## Shared Library
Shared DTOs/enums/events live in `shared-lib/src/main/java/org/ultra/rcrs/`. When modifying shared classes, rebuild `shared-lib` first:
```bash
./mvnw install -pl shared-lib -DskipTests
```

## Testing Notes
- Tests for blocking services use `@SpringBootTest` with embedded or real infra
- catalog-service Kafka tests use `spring-boot-starter-kafka-test`
- media-service tests use `spring-boot-starter-webmvc-test`
- No embedded Cassandra or LocalStack in tests; tests may require real containers
