package org.ultra.rcrs.mediaservice.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(value = "image")
public class ImageConfigurationProperties {

    private BucketProperties bucket;

}
