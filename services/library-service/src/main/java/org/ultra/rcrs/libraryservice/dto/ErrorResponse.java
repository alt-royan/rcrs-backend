package org.ultra.rcrs.libraryservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Standard error payload returned for any failed request.")
public record ErrorResponse(
        @Schema(description = "HTTP status code of the error response.", example = "404")
        int status,
        @Schema(description = "Human-readable message describing what went wrong.", example = "resource not found")
        String message) {
}
