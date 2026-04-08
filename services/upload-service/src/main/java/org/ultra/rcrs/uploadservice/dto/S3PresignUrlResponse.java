package org.ultra.rcrs.uploadservice.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class S3PresignUrlResponse {

    private Map<String, List<String>> headers;
    private String method;
    private String uid;
    private String url;
}
