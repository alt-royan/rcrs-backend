package org.ultra.rcrs.catalogservice.dto.request;

import lombok.Data;

import java.time.OffsetDateTime;

@Data
public class TrackUploadRequest {

    private String albumId;

    private String title;

    private Integer trackNumber;

    private Boolean explicit;
}
