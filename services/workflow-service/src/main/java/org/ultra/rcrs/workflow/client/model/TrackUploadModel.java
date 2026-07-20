package org.ultra.rcrs.workflow.client.model;

public record TrackUploadModel(
        String albumId,
        String title,
        Integer trackNumber,
        Boolean explicit
) {
}
