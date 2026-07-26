# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What this is

RCRS is a Spring Boot microservices backend (Java 21, Spring Boot 4.0.3, Spring Cloud 2025.1.0) for a music catalog/streaming platform: artists, albums, tracks, audio transcoding, search, and user identity. It's a Maven multi-module reactor build.

## Build & test commands

Run all commands from the repo root (`pom.xml` is the reactor parent).

```
./mvnw clean install -DskipTests      # build all modules, skip tests
./mvnw clean verify                   # build + run full test suite (what CI runs)
./mvnw -pl services/media-service -am test          # test a single module (and its dependencies)
./mvnw -pl services/media-service test -Dtest=HashTest   # run a single test class
```

Integration tests use Testcontainers (Postgres, MongoDB, Elasticsearch, embedded Kafka) and require Docker to be running — this is why CI runs `docker version` before `mvn clean verify`. Tests live under each module's `src/test/java`; most are Spring Boot `@SpringBootTest` integration tests named `*IntegrationTest` that spin up real containers, plus a smaller number of plain unit tests for services/utils.

To run several test classes at once, comma-separate them (`-Dtest=FooTest,BarTest`); add `-Dsurefire.failIfNoSpecifiedTests=false` when the pattern won't match in every reactor module that `-am` pulls in. Note the surefire summary is suppressed by `-q` — read `target/surefire-reports/*.txt` instead.

### Testing conventions

- Each service with integration tests has an abstract `integration/BaseIntegrationTest` that owns the `@Container` declarations, `@ActiveProfiles("test")`, `@EmbeddedKafka`, `@DirtiesContext`, and helper methods for seeding fixtures and publishing protobuf events. Add new integration tests by extending it rather than re-declaring containers.
- **`src/test/resources/application-test.yaml` does not inherit from a test-specific default** — anything not set there falls through to the module's `src/main/resources/application.yaml`, which holds the hardcoded `192.168.1.3` local-dev hosts. When a test asserts on a value derived from config (e.g. `cdn.images.endpoint`, which `S3Utils` uses to build image URLs), pin that property in `application-test.yaml`, or the assertion silently tests the dev config.
- Tests disable auth with `spring.security.enabled: false`; each service's real `SecurityConfig` is `@ConditionalOnProperty` on that flag, and search-service adds a permissive test-only `SecurityConfig` under `src/test/java`. Eureka is disabled the same way.
- metadata-read-service is the only WebFlux module, so its tests use `WebTestClient`; every other service is Spring MVC and uses `MockMvc`.
- On Windows, don't bulk-edit Java sources via PowerShell 5.1 `Set-Content -Encoding utf8` — it prepends a BOM and `javac` fails with `illegal character: '﻿'`. Use `[System.IO.File]::WriteAllText` with `UTF8Encoding($false)`.

### Local environment

```
local/start.bat
```
does: create the `rcrs` docker network → `mvnw.cmd clean install -DskipTests` → build then start `local/docker-compose.yml`. That compose file builds and runs all 9 services. `local/infra/docker-compose.yml` and the `elk/`, `keycloak/`, `s3/`, `temporal/` subfolders under `local/infra/` hold the supporting infrastructure (Postgres, MongoDB, Kafka, Elasticsearch/ELK, Keycloak, LocalStack S3, Temporal) that the app services expect to reach over the network at `192.168.1.3` (see the hardcoded hosts in `local/docker-compose.yml`) — adjust that IP for your own machine when running locally.

## Architecture

### Modules

- `shared-lib` — common code shared by every service: domain enums (`AlbumType`, `ArtistRole`, `EntityStatus`, `LifecycleStatus`, `FileStatus`, `Order`, `EntityType`), shared exceptions, Kafka topic names (`Topics`) and base Kafka config, a generic `ProtobufEventProducer`, and utilities (`Base62`/`Url62` short-ID encoding, `S3Utils`, `UuidConverter`, `BigIntegerPairing`). It also owns `src/main/proto/events/**` — all protobuf event schemas (album/artist/track lifecycle events plus common types), compiled and consumed by every service that produces or reads domain events.
- `discovery-server` — Eureka service registry. Every other service registers with it and depends on it being healthy before starting.
- `gateway-api` — Spring Cloud Gateway (WebFlux, reactive). Single public entry point; routes by path prefix to backend services via Eureka (`lb://`), validates JWTs (OAuth2 resource server against Keycloak), and aggregates Swagger UIs. Route map (see `gateway-api/src/main/resources/application.yaml`):
  - `/me/**` → user-service (prefix stripped)
  - `/workflow/**` → workflow-service (prefix stripped)
  - `/media/**` → media-service (prefix stripped)
  - `/api/search/**` → search-service (2 segments stripped)
  - `/api/catalog/**` → metadata-read-service (2 segments stripped)
  - `/playlists/**` → playlist-service (no prefix stripped — the service maps `/playlists` itself)
  - metadata-write-service is **not** exposed through the gateway — it's write-path-internal only.
- `services/metadata-write-service` — command side of catalog CQRS. Owns artists/albums/tracks as the source of truth in Postgres (Liquibase-managed, schema `rcrs_catalog`). Validates and persists writes, then emits protobuf domain events (create/update/delete/lifecycle-change/relationship-change) to Kafka (`Topics.CATALOG_CDC_TOPIC`).
- `services/metadata-read-service` — query side of catalog CQRS. Consumes the CDC events from metadata-write-service and projects them into MongoDB, then serves fast reads through separate `admin` and `publ` (public) controllers/services. Do not expect it to accept writes.
- `services/search-service` — indexes artists/albums/tracks into Elasticsearch (listens on `Topics.SEARCH_INDEX_TOPIC`) and serves search queries via separate admin/public controllers.
- `services/media-service` — handles audio/image assets: upload, S3 storage (via LocalStack in local dev), image thumbnailing, and audio transcoding/loudness-normalization orchestrated as **Temporal workflows** (`temporal/workflow`, `temporal/activity`, `temporal/worker`). Publishes transcoding-status events (`Topics.MEDIA_TRANSCODING_TOPIC`).
- `services/workflow-service` — cross-service saga/orchestration layer, also built on Temporal. Talks to metadata-write, media, and search services via Feign clients (`FEIGN_*_URL` env vars) to coordinate multi-step processes (e.g. publishing a track end-to-end) and runs a scheduled purge job (`WORKFLOW_PURGE_CRON`).
- `services/user-service` — identity/profile service backed by Postgres (schema `rcrs_user`); consumes identity events from Keycloak (`Topics.IDENTITY_EVENTS_TOPIC`) to keep local user records in sync.
- `services/playlist-service` — user playlists backed by Postgres (schema `rcrs_playlist`, own Liquibase changelog tables `playlist_databasechangelog*`). Serves reads/writes directly over REST (`PlaylistController`, owner taken from the JWT subject) **and** accepts the same four commands asynchronously as protobuf `DomainEvent`s on `Topics.PLAYLIST_COMMANDS_TOPIC` (`PLAYLIST_CREATED`, `TRACKS_ADDED_TO_PLAYLIST`, `TRACKS_REMOVED_FROM_PLAYLIST`, `PLAYLIST_DELETED`). Nothing in the repo produces to that topic yet — the consumer side exists for other services to drive playlists later, so keep the two paths in sync when changing playlist behavior.
- `web-ui` — a standalone static HTML/JS debug tool (`index.html`) plus a Python seeding script (`seed.py`) and JSON/audio seed fixtures for exercising the catalog API; not part of the Spring build.

### Cross-cutting patterns

- **Event-driven CQRS via Kafka + Protobuf**: writes happen in metadata-write-service, are published as protobuf `DomainEvent`s (schemas in `shared-lib/src/main/proto/events`), and are fanned out to metadata-read-service (Mongo projection) and search-service (Elasticsearch indexing). When changing write-side behavior, check whether a corresponding event/consumer needs updating in metadata-read-service and search-service.
- **Auth**: every service (including the gateway) independently validates JWTs issued by Keycloak (`spring.security.oauth2.resourceserver.jwt.issuer-uri`/`jwk-set-uri`), and can be disabled per-service via the `spring.security.enabled` property (used in tests). Realm roles are read from a custom `rcrs-roles` JWT claim and mapped to `ROLE_*` Spring authorities — see each service's `SecurityConfig`.
- **Gateway prefix ↔ controller mapping**: the gateway's `StripPrefix` filter and each service's `@RequestMapping` are two halves of one contract — a service maps the path it receives *after* stripping, not the public URL. Most services keep their own prefix (metadata-read maps `/albums`, `/artists`, `/tracks` and `/admin/*`; playlist-service maps `/playlists` with nothing stripped). The exception is **search-service**, whose `SearchController` has a bare `@RequestMapping` and therefore serves the **root** path: public `/api/search?q=…&type=…` has 2 segments stripped and arrives as `/`. Tests calling `/search` there will 404 on everything, including param-validation cases, because routing fails before binding. metadata-write-service's `PurgeController` likewise maps root. When changing a route prefix, change both sides.
- **Service discovery**: services register with `discovery-server` (Eureka) and reference each other by logical name (`lb://service-name` in the gateway, `EUREKA_CLIENT_SERVICEURL_DEFAULTZONE` env var elsewhere) rather than hardcoded hosts.
- **Short IDs**: `Base62`/`Url62` in shared-lib encode UUIDs into short, URL-friendly identifiers used in public-facing catalog URLs.
- **Admin vs public split**: metadata-read-service and search-service both separate their controller/service layers into `admin` and `publ` packages — admin endpoints expose more/unfiltered data, public endpoints serve the storefront.
- **Docker builds**: each service's `Dockerfile` is a multi-stage Maven build run from the repo root as build context (so it can `COPY` `shared-lib` and its own module), producing a slim `eclipse-temurin:21-jre-alpine` image running as a non-root user.
- **Adding a service module** touches four places beyond the module itself: `<modules>` in the root `pom.xml`, a per-service build step in `.github/workflows/build.yaml` (pushes `ghcr.io/<owner>/rcrs-<name>:latest` on every push to `main`), a service entry in `local/docker-compose.yml`, and — if it should be publicly reachable — a route plus a `springdoc.swagger-ui.urls` entry in `gateway-api/src/main/resources/application.yaml`.