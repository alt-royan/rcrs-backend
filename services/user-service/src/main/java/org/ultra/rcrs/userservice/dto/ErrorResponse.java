package org.ultra.rcrs.userservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Standard error payload returned for failed requests")
public record ErrorResponse(
        @Schema(description = "HTTP status code of the error, e.g. 404", example = "404")
        int status,
        @Schema(description = "Human-readable message describing the cause of the error")
        String message
) {
}
