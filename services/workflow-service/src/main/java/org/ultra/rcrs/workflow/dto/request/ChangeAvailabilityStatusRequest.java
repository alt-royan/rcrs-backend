package org.ultra.rcrs.workflow.dto.request;

import jakarta.validation.constraints.NotNull;
import org.ultra.rcrs.enums.EntityStatus;

public record ChangeAvailabilityStatusRequest(
        @NotNull
        EntityStatus status
) {
}
