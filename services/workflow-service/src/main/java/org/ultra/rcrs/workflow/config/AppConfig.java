package org.ultra.rcrs.workflow.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class AppConfig {

    @Bean
    OpenAPI metadataOpenAPI() {
        return new OpenAPI()
                .servers(List.of(
                        new Server().url("/workflow")
                ));
    }
}
