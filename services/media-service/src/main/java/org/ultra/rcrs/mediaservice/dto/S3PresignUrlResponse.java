package org.ultra.rcrs.mediaservice.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@Builder
public class S3PresignUrlResponse {

    private List<Map.Entry<String, String>> headers;
    private String method;
    private String uid;
    private String url;
}
