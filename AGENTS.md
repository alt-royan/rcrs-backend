# RCRS Backend

## Build & Run

- **Java 21**, Spring Boot 4.0.3, multi-module Maven
- Build all: `./mvnw clean install -DskipTests`
- Build single module: `./mvnw clean install -pl services/media-service -am -DskipTests`
- Run tests: `./mvnw test` (or `-pl <module>` for focused testing)
- Local infra: `local/start.sh` creates Docker network, starts infra + services

## Architecture

- **Services**: discovery (Eureka), gateway (Spring Cloud Gateway), media, metadata-write (Postgres + MongoDB CQRS), metadata-read (MongoDB), search (Elasticsearch), user (Postgres), workflow (Temporal)
- **Communication**: Kafka (protobuf domain events), SQS (S3 upload events), Eureka discovery, Feign clients
- **Orchestration**: Temporal.io for media processing workflows (media-service, workflow-service)
- **Infra**: LocalStack (S3+SQS), Kafka, Postgres, MongoDB, Elasticsearch, Temporal, Keycloak, Redis

## Key Configs

- S3/SQS LocalStack init: `local/infra/s3/init.sh` + `queue-policy.json` + `notification.json`
- Application configs: `services/*/src/main/resources/application*.yaml`
- Media upload pipeline: `s3:ObjectCreated:Put` → SQS → `SqsS3Listener` → DB status update
- Queue name: `rcrs-upload-event-queue`, Buckets: `images`, `rcrs-audio`, `rcrs-upload`
- Region: `eu-west-1` (LocalStack), AWS endpoint: `http://localhost.localstack.cloud:4566`

## Noteworthy

- MapStruct + Lombok — annotation processor must be configured in compiler plugin (parent POM already does this)
- shared-lib provides protobuf event schemas (`src/main/proto/`), Kafka producer base, shared enums/exceptions/utils
- Liquibase for schema migration (media-service, user-service)
- MongoDB replica set required (MongoDB 7, `rs0`)

## Verify

- Check service health: `curl http://localhost:<port>/actuator/health`
- Check LocalStack: `curl http://localhost:4566/_localstack/health`
