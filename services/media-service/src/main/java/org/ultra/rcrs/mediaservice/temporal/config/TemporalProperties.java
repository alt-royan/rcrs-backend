package org.ultra.rcrs.mediaservice.temporal.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
import java.util.Map;

@ConfigurationProperties(prefix = "spring.temporal")
public record TemporalProperties(
        String namespace,
        Map<String, ActivityConfig> activities
) {
    public record ActivityConfig(
            Duration startToCloseTimeout,
            RetryConfig retry
    ) {
    }

    public record RetryConfig(
            Duration initialInterval,
            double backoffCoefficient,
            Duration maximumInterval,
            int maximumAttempts
    ) {
    }
}
