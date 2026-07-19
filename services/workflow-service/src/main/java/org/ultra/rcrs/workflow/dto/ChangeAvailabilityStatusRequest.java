package org.ultra.rcrs.workflow.dto;

import jakarta.validation.constraints.NotNull;
import org.ultra.rcrs.enums.EntityStatus;

public record ChangeAvailabilityStatusRequest(
        @NotNull
        EntityStatus status
) {
}
