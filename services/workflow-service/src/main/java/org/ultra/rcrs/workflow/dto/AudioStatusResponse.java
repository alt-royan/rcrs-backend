package org.ultra.rcrs.workflow.dto;

import org.ultra.rcrs.enums.FileStatus;

public record AudioStatusResponse(
        String uid,
        FileStatus status,
        String reason
) {
}
