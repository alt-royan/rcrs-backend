package org.ultra.rcrs.metadata.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.ultra.rcrs.utils.ImageUtils;

import java.util.List;

@Configuration
public class AppConfig {

    @Bean
    OpenAPI metadataOpenAPI() {
        return new OpenAPI()
                .servers(List.of(
                        new Server().url("/api/catalog")
                ));
    }

    @Bean
    public ImageUtils s3Utils(@Value("${cdn.images.endpoint}") String endpoint) {
        return new ImageUtils(endpoint);
    }
}
