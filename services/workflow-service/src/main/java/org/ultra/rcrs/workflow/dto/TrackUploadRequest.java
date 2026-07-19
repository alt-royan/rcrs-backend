package org.ultra.rcrs.workflow.dto;

public record TrackUploadRequest(
        String albumId,
        String title,
        Integer trackNumber,
        Boolean explicit
) {
}
