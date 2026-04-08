package org.ultra.rcrs.mediaservice.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

@Getter
@Setter
@ConfigurationProperties("s3")
public class S3ConfigProperties {
    private String url;
    private String accessKey;
    private String secretKey;
    private String region;
    private Map<String, String> buckets;
}
