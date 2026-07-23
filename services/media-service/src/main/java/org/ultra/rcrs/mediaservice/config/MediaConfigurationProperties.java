package org.ultra.rcrs.mediaservice.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

@Data
@ConfigurationProperties(value = "media")
public class MediaConfigurationProperties {

    @NestedConfigurationProperty
    private AudioConfigurationProperties audio;

    @NestedConfigurationProperty
    private ImageConfigurationProperties image;

    @NestedConfigurationProperty
    private UploadConfigurationProperties upload;

    private SqsProperties sqs;

    @Data
    public static class SqsProperties {
        private String queue;
    }
}
