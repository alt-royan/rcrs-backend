package org.ultra.rcrs.mediaservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "A short-lived presigned URL that can be used to stream or download an audio asset.")
public record PresignedUrlResponse(
        @Schema(description = "The presigned URL, valid for a limited time, pointing directly at the requested audio asset")
        String url) {
}
