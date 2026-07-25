package org.ultra.rcrs.mediaservice.dto;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class DownloadFileResponse {

    private UUID id;

    private UUID guid;

    private String fileName;

    private String contentType;

    private String url;

    private Instant expiresAt;
}
