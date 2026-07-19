package org.ultra.rcrs.metadata.dto.request;

import lombok.Data;

@Data
public class TrackUploadRequest {

    private String albumId;

    private String title;

    private Integer trackNumber;

    private Boolean explicit;
}
