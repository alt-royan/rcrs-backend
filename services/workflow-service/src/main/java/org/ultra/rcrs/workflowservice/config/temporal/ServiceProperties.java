package org.ultra.rcrs.workflowservice.config.temporal;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "services")
public record ServiceProperties(
    ServiceConfig metadata,
    ServiceConfig audio,
    ServiceConfig search,
    ServiceConfig text,
    ServiceConfig notification,
    ServiceConfig statistics,
    ServiceConfig cleanup
) {
    public record ServiceConfig(String url) {}
}
