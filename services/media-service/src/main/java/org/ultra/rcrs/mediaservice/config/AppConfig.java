package org.ultra.rcrs.mediaservice.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.ultra.rcrs.utils.ImageConfigBase;

import java.util.List;

@Configuration
@EnableConfigurationProperties(MediaConfigurationProperties.class)
@Import(ImageConfigBase.class)
public class AppConfig {

    @Bean
    OpenAPI metadataOpenAPI() {
        return new OpenAPI()
                .servers(List.of(
                        new Server().url("/media")
                ));
    }
}
