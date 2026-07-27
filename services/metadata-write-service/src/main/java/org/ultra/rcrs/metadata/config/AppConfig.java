package org.ultra.rcrs.metadata.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.ultra.rcrs.utils.ImageUtils;

@Configuration
public class AppConfig {

    @Bean
    public ImageUtils s3Utils(@Value("${cdn.images.endpoint}") String endpoint) {
        return new ImageUtils(endpoint);
    }
}
