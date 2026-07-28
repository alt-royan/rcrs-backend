package org.ultra.rcrs.mediaservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@Builder
@Schema(description = "Presigned S3 request details the client must use to upload an audio file directly to object storage.")
public class S3PresignUrlResponse {

    @Schema(description = "HTTP headers that must be included on the upload request")
    private List<Map.Entry<String, String>> headers;
    @Schema(description = "HTTP method to use for the upload request (e.g. PUT)")
    private String method;
    @Schema(description = "Uid identifying this pending upload; used later to poll status and reference the file")
    private String uid;
    @Schema(description = "Presigned S3 URL to send the upload request to")
    private String url;
}
