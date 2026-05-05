package org.ultra.rcrs.gatewayapi;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfig {

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("catalog-route", r -> r
                        .path("/api/metadata/**")
                        .filters(f -> f.stripPrefix(1))
                        .uri("lb://catalog-service"))
                .route("upload-route", r -> r
                        .path("/api/upload/**")
                        .filters(f -> f.stripPrefix(1))
                        .uri("lb://media-service"))
                .route("search-route", r -> r
                        .path("/api/search/**")
                        .filters(f -> f.stripPrefix(1))
                        .uri("lb://search-service"))
                .build();
    }

}