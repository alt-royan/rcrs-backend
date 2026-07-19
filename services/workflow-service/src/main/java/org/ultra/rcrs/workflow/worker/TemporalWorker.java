package org.ultra.rcrs.workflow.worker;

import io.temporal.client.WorkflowClient;
import io.temporal.worker.Worker;
import io.temporal.worker.WorkerFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.ultra.rcrs.workflow.activity.ActivityFactory;
import org.ultra.rcrs.workflow.activity.AudioActivity;
import org.ultra.rcrs.workflow.activity.MetadataActivity;
import org.ultra.rcrs.workflow.activity.impl.MetadataActivityImpl;
import org.ultra.rcrs.workflow.config.TemporalProperties;
import org.ultra.rcrs.workflow.workflow.ArtistRegistrationWorkflow;
import org.ultra.rcrs.workflow.workflow.impl.ArtistRegistrationWorkflowImpl;

import static org.ultra.rcrs.workflow.config.TemporalConfig.WORKFLOW_TASK_QUEUE;

@Component
@RequiredArgsConstructor
public class TemporalWorker implements CommandLineRunner {

    private final WorkflowClient workflowClient;

    private final MetadataActivityImpl metadataActivityImpl;

    @Override
    public void run(String... args) {
        WorkerFactory factory = WorkerFactory.newInstance(workflowClient);
        Worker worker = factory.newWorker(WORKFLOW_TASK_QUEUE);

        worker.registerWorkflowImplementationTypes(ArtistRegistrationWorkflowImpl.class);

        worker.registerActivitiesImplementations(metadataActivityImpl);

        factory.start();
    }
}