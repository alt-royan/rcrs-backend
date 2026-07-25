package org.ultra.rcrs.mediaservice.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.ultra.rcrs.utils.S3Utils;

import java.util.List;

@Configuration
@EnableConfigurationProperties(MediaConfigurationProperties.class)
public class AppConfig {

    @Bean
    OpenAPI metadataOpenAPI() {
        return new OpenAPI()
                .servers(List.of(
                        new Server().url("/media")
                ));
    }

    @Bean
    public S3Utils s3Utils(@Value("${media.audio.bucket.public-url}") String endpoint) {
        return new S3Utils(endpoint);
    }
}
