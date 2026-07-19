package org.ultra.rcrs.workflow.worker;

import io.temporal.client.WorkflowClient;
import io.temporal.worker.Worker;
import io.temporal.worker.WorkerFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.ultra.rcrs.workflow.activity.impl.AlbumActivityImpl;
import org.ultra.rcrs.workflow.activity.impl.ArtistActivityImpl;
import org.ultra.rcrs.workflow.activity.impl.TrackActivityImpl;
import org.ultra.rcrs.workflow.workflow.impl.ArtistChangeAvailabilityStatusWorkflowImpl;
import org.ultra.rcrs.workflow.workflow.impl.ArtistRegistrationWorkflowImpl;

import static org.ultra.rcrs.workflow.config.TemporalConfig.WORKFLOW_TASK_QUEUE;

@Component
@RequiredArgsConstructor
public class TemporalWorker implements CommandLineRunner {

    private final WorkflowClient workflowClient;

    private final ArtistActivityImpl artistActivityImpl;
    private final AlbumActivityImpl albumActivityImpl;
    private final TrackActivityImpl trackActivityImpl;

    @Override
    public void run(String... args) {
        WorkerFactory factory = WorkerFactory.newInstance(workflowClient);
        Worker worker = factory.newWorker(WORKFLOW_TASK_QUEUE);

        worker.registerWorkflowImplementationTypes(ArtistRegistrationWorkflowImpl.class);
        worker.registerWorkflowImplementationTypes(ArtistChangeAvailabilityStatusWorkflowImpl.class);

        worker.registerActivitiesImplementations(artistActivityImpl);
        worker.registerActivitiesImplementations(albumActivityImpl);
        worker.registerActivitiesImplementations(trackActivityImpl);

        factory.start();
    }
}