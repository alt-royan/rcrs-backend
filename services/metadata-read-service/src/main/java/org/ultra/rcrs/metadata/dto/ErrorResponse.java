package org.ultra.rcrs.metadata.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Standard error payload returned for failed requests.")
public record ErrorResponse(
        @Schema(description = "HTTP status code of the error response.") int status,
        @Schema(description = "Human-readable description of what went wrong.") String message) {
}
