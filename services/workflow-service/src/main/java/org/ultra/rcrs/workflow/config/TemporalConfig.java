package org.ultra.rcrs.workflow.config;

import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowClientOptions;
import io.temporal.serviceclient.WorkflowServiceStubs;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.ultra.rcrs.workflow.activity.ActivityFactory;

@Configuration
@EnableConfigurationProperties(TemporalProperties.class)
public class TemporalConfig {
    public final static String WORKFLOW_TASK_QUEUE = "WORKFLOW_TASK_QUEUE";

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
