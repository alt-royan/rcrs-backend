package org.ultra.rcrs.mediaservice.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@Data
@ConfigurationProperties(value = "image")
public class ImageConfigurationProperties {

    private BucketProperties bucket;
    private Thumbnails thumbnails;

    @Data
    public static class Thumbnails {
        private List<Integer> sizes;
    }

}
