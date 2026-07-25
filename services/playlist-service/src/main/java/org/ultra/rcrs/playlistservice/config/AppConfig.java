package org.ultra.rcrs.playlistservice.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.ultra.rcrs.utils.S3Utils;

import java.util.List;

@Configuration
public class AppConfig {

    @Bean
    OpenAPI playlistOpenAPI() {
        return new OpenAPI()
                .servers(List.of(
                        new Server().url("/playlists")
                ));
    }

    @Bean
    public S3Utils s3Utils(@Value("${cdn.images.endpoint}") String endpoint) {
        return new S3Utils(endpoint);
    }
}
