package org.ultra.rcrs.metadata.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.ultra.rcrs.enums.LifecycleStatus;

@Schema(description = "Payload used to update the lifecycle status of an album or track.")
@Data
public class StatusDto {

    @Schema(description = "New lifecycle status to apply. Valid values: CREATED, TRANSCODING, FAILED, READY, PUBLISHED. Required.", example = "PUBLISHED")
    @NotNull
    private LifecycleStatus status;
}
