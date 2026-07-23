package org.ultra.rcrs.mediaservice.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@Data
@ConfigurationProperties(value = "upload")
public class UploadConfigurationProperties {

    private BucketProperties bucket;
    private Duration signatureDuration;
    private Duration uploadDuration;

}
