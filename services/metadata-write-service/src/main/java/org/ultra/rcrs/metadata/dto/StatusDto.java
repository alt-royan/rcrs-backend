package org.ultra.rcrs.metadata.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.ultra.rcrs.enums.LifecycleStatus;

@Data
public class StatusDto {

    @NotNull
    private LifecycleStatus status;
}
