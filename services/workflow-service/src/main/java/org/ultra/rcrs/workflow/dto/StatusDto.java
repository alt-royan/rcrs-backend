package org.ultra.rcrs.workflow.dto;

import jakarta.validation.constraints.NotNull;
import org.ultra.rcrs.enums.LifecycleStatus;

public record StatusDto(@NotNull LifecycleStatus status) {
}

