package org.ultra.rcrs.mediaservice.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@Data
@ConfigurationProperties(value = "download")
public class DownloadConfigurationProperties {

    private BucketProperties bucket;
    private Duration signatureDuration;
}
