package org.ultra.rcrs.libraryservice.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Outcome of a batch of reported playbacks.")
public record ListenIngestResponse(
        @Schema(description = "How many playbacks were recorded and counted towards the caller's totals.")
        int accepted,
        @Schema(description = "How many were rejected as replays of a clientPlayId already recorded. " +
                "These are not counted again.")
        int duplicates) {
}
