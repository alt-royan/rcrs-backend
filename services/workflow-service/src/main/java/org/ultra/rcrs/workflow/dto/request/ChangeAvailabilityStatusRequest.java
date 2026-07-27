package org.ultra.rcrs.workflow.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import org.ultra.rcrs.enums.EntityStatus;

@Schema(description = "Request body for changing the availability (lifecycle) status of an artist, album, or track " +
        "via an orchestrated Temporal workflow.")
public record ChangeAvailabilityStatusRequest(
        @NotNull
        @Schema(description = "The target availability status to transition the entity to (e.g. hidden or active).", requiredMode = Schema.RequiredMode.REQUIRED)
        EntityStatus status
) {
}
