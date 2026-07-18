package org.ultra.rcrs.workflowservice.config.temporal;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
import java.util.Map;

@ConfigurationProperties(prefix = "temporal")
public record TemporalProperties(
    String serviceUrl,
    String namespace,
    String taskQueue,
    Map<String, ActivityConfig> activities
) {
    public record ActivityConfig(
        Duration startToCloseTimeout,
        RetryConfig retry
    ) {}

    public record RetryConfig(
        Duration initialInterval,
        double backoffCoefficient,
        Duration maximumInterval,
        int maximumAttempts
    ) {}
}
