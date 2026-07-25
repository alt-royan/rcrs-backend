# TODO

## From Codebase

- [ ] **Refine CORS configuration** — `gateway-api/src/main/java/org/ultra/rcrs/gatewayapi/SecurityConfig.java:33`
  - `//TODO:доработать` — placeholder CORS config, needs to be properly configured.

- [ ] **Use s3Utils in StreamingService** — `services/media-service/src/main/java/org/ultra/rcrs/mediaservice/service/StreamingService.java:29`
  - `//TODO: use s3Utils` — `streamAudio(UUID trackId)` returns empty URL list instead of generating real S3 presigned URLs.

- [ ] **Use s3Utils in StreamingService** — `services/media-service/src/main/java/org/ultra/rcrs/mediaservice/service/StreamingService.java:39`
  - `//TODO: use s3Utils` — `streamAudio(UUID audioId)` returns empty URL list instead of generating real S3 presigned URLs.

## Additional

- [ ] Fix track and album status flow
- [ ] Fix tests and test all functions
- [ ] Group audios by main
- [ ] Add user on front and fix user registration flow
