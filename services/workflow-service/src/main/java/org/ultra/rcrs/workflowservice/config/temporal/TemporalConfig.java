package org.ultra.rcrs.workflowservice.config.temporal;

import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowClientOptions;
import io.temporal.serviceclient.WorkflowServiceStubs;
import io.temporal.serviceclient.WorkflowServiceStubsOptions;
import io.temporal.worker.Worker;
import io.temporal.worker.WorkerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.ultra.rcrs.workflowservice.activity.impl.AudioActivityImpl;
import org.ultra.rcrs.workflowservice.activity.impl.CleanupActivityImpl;
import org.ultra.rcrs.workflowservice.activity.impl.MetadataActivityImpl;
import org.ultra.rcrs.workflowservice.activity.impl.NotificationActivityImpl;
import org.ultra.rcrs.workflowservice.activity.impl.SearchActivityImpl;
import org.ultra.rcrs.workflowservice.activity.impl.StatisticsActivityImpl;
import org.ultra.rcrs.workflowservice.activity.impl.TextActivityImpl;
import org.ultra.rcrs.workflowservice.workflow.impl.ArtistCreationWorkflowImpl;

@Configuration
public class TemporalConfig {

    private final TemporalProperties properties;
    private WorkerFactory workerFactory;

    public TemporalConfig(TemporalProperties properties) {
        this.properties = properties;
    }

    @Bean
    public WorkflowServiceStubs workflowServiceStubs() {
        return WorkflowServiceStubs.newServiceStubs(
                WorkflowServiceStubsOptions.newBuilder()
                        .setTarget(properties.serviceUrl())
                        .build()
        );
    }

    @Bean
    public WorkflowClient workflowClient(WorkflowServiceStubs stubs) {
        return WorkflowClient.newInstance(stubs,
                WorkflowClientOptions.newBuilder()
                        .setNamespace(properties.namespace())
                        .build()
        );
    }

    @Bean
    public ActivityFactory activityFactory() {
        return new ActivityFactory(properties);
    }

    @Bean
    public WorkerFactory workerFactory(
            WorkflowClient workflowClient,
            ActivityFactory activityFactory,
            MetadataActivityImpl metadataActivity,
            AudioActivityImpl audioActivity,
            SearchActivityImpl searchActivity,
            TextActivityImpl textActivity,
            NotificationActivityImpl notificationActivity,
            StatisticsActivityImpl statisticsActivity,
            CleanupActivityImpl cleanupActivity) {

        WorkerFactory factory = WorkerFactory.newInstance(workflowClient);
        Worker worker = factory.newWorker(properties.taskQueue());

        worker.register(
                new ArtistCreationWorkflowImpl(activityFactory)
        );

        worker.registerActivitiesImplementations(
                metadataActivity,
                audioActivity,
                searchActivity,
                textActivity,
                notificationActivity,
                statisticsActivity,
                cleanupActivity
        );

        this.workerFactory = factory;
        return factory;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void startWorkerFactory() {
        if (workerFactory != null) {
            workerFactory.start();
        }
    }

    @EventListener(org.springframework.context.event.ContextClosedEvent.class)
    public void shutdownWorkerFactory() {
        if (workerFactory != null) {
            workerFactory.shutdown();
        }
    }
}
