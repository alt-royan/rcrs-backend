# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What this is

RCRS is a Spring Boot microservices backend (Java 21, Spring Boot 4.0.3, Spring Cloud 2025.1.0) for a music catalog/streaming platform: artists, albums, tracks, audio transcoding, search, user identity, playlists and per-user library. It's a Maven multi-module reactor build.

## Build & test commands

Run all commands from the repo root (`pom.xml` is the reactor parent).

```
./mvnw clean install -DskipTests      # build all modules, skip tests
./mvnw clean verify                   # build + run full test suite (what CI runs)
./mvnw -pl services/media-service -am test          # test a single module (and its dependencies)
./mvnw -pl services/media-service test -Dtest=HashTest   # run a single test class
```

To run several test classes at once, comma-separate them (`-Dtest=FooTest,BarTest`); add `-Dsurefire.failIfNoSpecifiedTests=false` when the pattern won't match in every reactor module that `-am` pulls in. Note the surefire summary is suppressed by `-q` — read `target/surefire-reports/*.txt` instead.

### Testing conventions

- **Most test suites have been deleted.** What remains: metadata-write-service (unit + `*IntegrationTest`), `shared-lib` utils unit tests, and `HashTest` in media-service. search-service, user-service, playlist-service, metadata-read-service and library-service have **no tests** — several still have an orphaned `src/test/resources/application-test.yaml` with no test classes using it. Don't assume a change is covered.
- Integration tests use Testcontainers (Postgres, MongoDB, embedded Kafka) and require Docker to be running — this is why CI runs `docker version` before `mvn clean verify`.
- metadata-write-service's `integration/BaseIntegrationTest` owns the `@Container` declarations, `@ActiveProfiles("test")`, `@EmbeddedKafka`, `@DirtiesContext`, and helpers for seeding fixtures and publishing protobuf events. Add integration tests by extending it rather than re-declaring containers.
- **`src/test/resources/application-test.yaml` does not inherit from a test-specific default** — anything not set there falls through to the module's `src/main/resources/application.yaml`, which holds the hardcoded `192.168.1.3` local-dev hosts. When a test asserts on a value derived from config (e.g. `cdn.images.endpoint`, which `S3Utils` uses to build image URLs), pin that property in `application-test.yaml`, or the assertion silently tests the dev config.
- Tests disable auth with `spring.security.enabled: false`; every service's real `SecurityConfig` is `@ConditionalOnProperty` on that flag. Eureka is disabled the same way. Note that with security off, `@AuthenticationPrincipal Jwt` arrives null — see `CallerId` below.
- metadata-read-service is the only WebFlux module (`WebTestClient`); every other service is Spring MVC (`MockMvc`).
- On Windows, don't bulk-edit Java sources via PowerShell 5.1 `Set-Content -Encoding utf8` — it prepends a BOM and `javac` fails with `illegal character: '﻿'`. Use `[System.IO.File]::WriteAllText` with `UTF8Encoding($false)`.

### Local environment

```
local/start.bat
```
does: create the `rcrs` docker network → `mvnw.cmd clean install -DskipTests` → build then start `local/docker-compose.yml`. That compose file builds and runs all 10 services with `SPRING_PROFILES_ACTIVE: dev`. `local/infra/docker-compose.yml` and the `elk/`, `keycloak/`, `s3/`, `temporal/` subfolders under `local/infra/` hold the supporting infrastructure (Postgres, MongoDB, Kafka, Elasticsearch/ELK, Keycloak, LocalStack S3, Temporal) that the app services expect to reach over the network at `192.168.1.3` (see the hardcoded hosts in `local/docker-compose.yml`) — adjust that IP for your own machine when running locally.

**Every module carries two configs**: `application.yaml` (bare-metal defaults — `localhost` infra, hardcoded `192.168.1.3` Keycloak) and `application-dev.yaml` (everything through `${ENV_VAR}` placeholders, used by docker-compose). workflow-service uses `.yml` instead of `.yaml`. A new property generally has to be added to both, or it will be missing in containers.

## Architecture

### Modules

- `shared-lib` — common code shared by every service: domain enums (`AlbumType`, `ArtistRole`, `EntityStatus`, `LifecycleStatus`, `FileStatus`, `Order`, `EntityType`), shared exceptions, Kafka topic names (`Topics`) and base Kafka config, a generic `ProtobufEventProducer`, `security/CallerId`, and utilities (`Base62`/`Url62` short-ID encoding, `S3Utils`, `UuidConverter`, `BigIntegerPairing`). It also owns `src/main/proto/events/**` — all protobuf event schemas (album/artist/track lifecycle events plus common types), compiled and consumed by every service that produces or reads domain events. It depends on spring-security/oauth2-resource-server, so `CallerId` is available everywhere.
- `discovery-server` — Eureka service registry. Every other service registers with it and depends on it being healthy before starting.
- `gateway-api` — Spring Cloud Gateway (WebFlux, reactive). Single public entry point; routes by path prefix to backend services via Eureka (`lb://`), validates JWTs, applies global CORS (allowed origin pinned to `https://192.168.1.3:3000`), and aggregates Swagger UIs. Route map (see `gateway-api/src/main/resources/application.yaml`):
  - `/me/library/**` → library-service — **must stay declared first**; routes match in declaration order and `/me/**` would otherwise swallow it
  - `/me/**` → user-service
  - `/workflow/**` → workflow-service
  - `/media/**` → media-service
  - `/api/search/**` → search-service
  - `/api/catalog/**` → metadata-read-service
  - `/playlists/**` → playlist-service
  - metadata-write-service is **not** exposed through the gateway — it's write-path-internal only.
- `services/metadata-write-service` — command side of catalog CQRS. Owns artists/albums/tracks as the source of truth in Postgres (Liquibase-managed, schema `rcrs_catalog`). Validates and persists writes, then emits protobuf domain events to Kafka (`Topics.CATALOG_CDC_TOPIC`). Maps `/catalog/**`.
- `services/metadata-read-service` — query side of catalog CQRS. Consumes the CDC events and projects them into MongoDB, then serves reads through separate `admin` (`/api/admin/catalog/**`) and `publ` (`/api/catalog/**`) controllers. Do not expect it to accept writes.
- `services/search-service` — indexes artists/albums/tracks into Elasticsearch (listens on `Topics.SEARCH_INDEX_TOPIC`). `SearchController` has a bare `@RestController` and maps the full paths on the methods: `/api/search` and `/api/admin/search`.
- `services/media-service` — audio/image assets: upload, S3 storage (LocalStack locally), image thumbnailing, and audio transcoding/loudness-normalization orchestrated as **Temporal workflows** (`temporal/workflow`, `temporal/activity`, `temporal/worker`). Publishes `Topics.MEDIA_TRANSCODING_TOPIC`. Maps `/media/**`.
- `services/workflow-service` — cross-service saga/orchestration layer, also Temporal-based. Talks to metadata-write, media and search via Feign clients (`FEIGN_*_URL` env vars) to coordinate multi-step processes (e.g. publishing a track end-to-end) and runs a scheduled purge job (`WORKFLOW_PURGE_CRON`). Maps `/workflow/**`.
- `services/user-service` — identity/profile service backed by Postgres (schema `rcrs_user`); consumes Keycloak identity events (`Topics.IDENTITY_EVENTS_TOPIC`) to keep local user records in sync. The avatar is a column on `User` (there is no separate `UserAvatar` entity/table any more). Maps `/me` and `/me/avatar`.
- `services/playlist-service` — user playlists backed by Postgres (schema `rcrs_playlist`, own Liquibase changelog tables `playlist_databasechangelog*`). REST only (`PlaylistController` at `/playlists`, owner taken from the JWT subject). The Kafka command-consumer path and its `events/playlist/*.proto` schemas have been removed; `Topics.PLAYLIST_COMMANDS_TOPIC` still exists but nothing produces or consumes it.
- `services/library-service` — per-user library: likes, downloads, listen history and search history. **Dual-store**: Postgres (schema `rcrs_library`, changelog tables `library_databasechangelog*`, plain-SQL `db/changelog/init.sql`) for likes / downloaded tracks & collections / search history, MongoDB (`rcrs_library`) for listen events, play counts and recently-played. Consumes `Topics.IDENTITY_EVENTS_TOPIC` and purges a user's whole library on Keycloak `DELETE_ACCOUNT`. Maps `/me/library/**`.

### Cross-cutting patterns

- **Event-driven CQRS via Kafka + Protobuf**: writes happen in metadata-write-service, are published as protobuf `DomainEvent`s (schemas in `shared-lib/src/main/proto/events`), and are fanned out to metadata-read-service (Mongo projection) and search-service (Elasticsearch indexing). When changing write-side behavior, check whether a corresponding event/consumer needs updating in both.
- **Gateway does no prefix stripping.** Every service maps its own full public path (`/me/library/likes`, `/api/catalog/albums`, `/media/upload`, …), so the URL a client calls is the URL the controller declares. This also applies to OpenAPI: services under a prefix pin `springdoc.api-docs.path` / `swagger-ui.path` beneath it (e.g. library-service serves `/me/library/v3/api-docs`) and permit those paths in their `SecurityConfig`. Changing a public prefix means changing the gateway route, the `@RequestMapping`, the springdoc paths, and the security permit-list.
- **Auth**: every service (including the gateway) independently validates JWTs issued by Keycloak (`spring.security.oauth2.resourceserver.jwt.issuer-uri`/`jwk-set-uri`), toggled per-service by `spring.security.enabled`. Realm roles come from a custom `resource_access.rcrs.roles` claim, filtered to those starting with `ROLE_`, and the principal name is `preferred_username`. The gateway additionally permits all swagger/actuator paths, OPTIONS, GET+POST on `/api/catalog/**` and GET on `/api/search/**` anonymously; everything else needs a token.
- **`CallerId` (shared-lib)** is the required way to read the caller in per-user services: `CallerId.of(jwt)` / `CallerId.require(jwt)` throw `AuthenticationCredentialsNotFoundException` (→ 401 via the service's `GlobalExceptionHandler`) instead of NPE-ing into a 500 when the request arrived without a token. Never dereference `@AuthenticationPrincipal Jwt` directly.
- **Service discovery**: services register with `discovery-server` (Eureka) and reference each other by logical name (`lb://service-name` in the gateway, `EUREKA_CLIENT_SERVICEURL_DEFAULTZONE` env var elsewhere) rather than hardcoded hosts.
- **Short IDs**: `Base62`/`Url62` in shared-lib encode UUIDs into short, URL-friendly identifiers used in public-facing catalog URLs — library-service and playlist-service store these strings, not UUIDs.
- **Admin vs public split**: metadata-read-service and search-service separate their controller/service layers into `admin` and `publ` packages — admin endpoints expose more/unfiltered data, public endpoints serve the storefront.
- **Docker builds**: each service's `Dockerfile` is a multi-stage Maven build run from the repo root as build context (so it can `COPY` `shared-lib` and its own module), producing a slim `eclipse-temurin:21-jre-alpine` image running as a non-root user.
- **Adding a service module** touches four places beyond the module itself: `<modules>` in the root `pom.xml`, a per-service build step in `.github/workflows/build.yaml` (pushes `ghcr.io/<owner>/rcrs-<name>:latest` on every push to `main`), a service entry in `local/docker-compose.yml`, and — if it should be publicly reachable — a route plus a `springdoc.swagger-ui.urls` entry in `gateway-api/src/main/resources/application.yaml`.
