package org.ultra.rcrs.catalogservice.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.ultra.rcrs.enums.LifecycleStatus;

@Data
public class StatusDto {

    @NotNull
    private LifecycleStatus status;
}
