package org.ultra.rcrs.mediaservice.temporal.config;

import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowClientOptions;
import io.temporal.serviceclient.WorkflowServiceStubs;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.ultra.rcrs.mediaservice.temporal.activity.ActivityFactory;

@Configuration
@EnableConfigurationProperties(TemporalProperties.class)
public class TemporalConfig {
    public static final String MEDIA_TASK_QUEUE = "MEDIA_TASK_QUEUE";

    private final TemporalProperties properties;

    public TemporalConfig(TemporalProperties properties) {
        this.properties = properties;
        ActivityFactory.getInstance(properties);
    }

    @Bean
    public WorkflowClient workflowClient() {
        WorkflowServiceStubs service = WorkflowServiceStubs.newLocalServiceStubs();
        return WorkflowClient.newInstance(service, WorkflowClientOptions.newBuilder()
                .setNamespace(properties.namespace())
                .build());
    }
}
