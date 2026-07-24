package org.ultra.rcrs.userservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.ultra.rcrs.utils.S3Utils;

@Configuration
public class AppConfig {

    @Bean
    public S3Utils s3Utils(@Value("${cdn.images.endpoint:}") String endpoint) {
        return new S3Utils(endpoint);
    }
}
