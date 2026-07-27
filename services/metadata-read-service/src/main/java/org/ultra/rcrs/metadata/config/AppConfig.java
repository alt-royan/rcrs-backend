package org.ultra.rcrs.metadata.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.ultra.rcrs.utils.ImageConfigBase;

import java.util.List;

@Configuration
@Import(ImageConfigBase.class)
public class AppConfig {

    @Bean
    OpenAPI metadataOpenAPI() {
        return new OpenAPI()
                .servers(List.of(
                        new Server().url("/api/catalog")
                ));
    }
}
