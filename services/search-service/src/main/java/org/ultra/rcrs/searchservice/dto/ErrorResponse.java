package org.ultra.rcrs.searchservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "A standard error payload returned when a search request fails.")
public record ErrorResponse(
        @Schema(description = "The HTTP status code of the error, e.g. 400 for a bad request or 500 for an internal server error.") int status,
        @Schema(description = "A human-readable description of what went wrong.") String message) {
}
