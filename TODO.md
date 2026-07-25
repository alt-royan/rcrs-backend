# TODO

## From Codebase

- [ ] **Refine CORS configuration** — `gateway-api/src/main/java/org/ultra/rcrs/gatewayapi/SecurityConfig.java:33`
  - `//TODO:доработать` — placeholder CORS config, needs to be properly configured.

- [ ] **Use s3Utils in StreamingService** — `services/media-service/src/main/java/org/ultra/rcrs/mediaservice/service/StreamingService.java:29`
  - `//TODO: use s3Utils` — `streamAudio(UUID trackId)` returns empty URL list instead of generating real S3 presigned URLs.

- [ ] **Use s3Utils in StreamingService** — `services/media-service/src/main/java/org/ultra/rcrs/mediaservice/service/StreamingService.java:39`
  - `//TODO: use s3Utils` — `streamAudio(UUID audioId)` returns empty URL list instead of generating real S3 presigned URLs.

## Additional

- [ ] Fix tests and test all functions
- [ ] Tests in read service
- [ ] Tests in playlist service
- [ ] Tests in search service
