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
import org.ultra.rcrs.workflow.activity.impl.TranscodingActivityImpl;
import org.ultra.rcrs.workflow.converter.UploadRequestConverter;
import org.ultra.rcrs.workflow.kafka.WorkflowEventProducer;
import org.ultra.rcrs.workflow.workflow.*;
import org.ultra.rcrs.workflow.workflow.impl.*;

import static org.ultra.rcrs.workflow.config.TemporalConfig.WORKFLOW_TASK_QUEUE;

@Component
@RequiredArgsConstructor
public class TemporalWorker implements CommandLineRunner {

    private final WorkflowClient workflowClient;
    private final UploadRequestConverter converter;
    private final WorkflowEventProducer eventProducer;

    private final ArtistActivityImpl artistActivityImpl;
    private final AlbumActivityImpl albumActivityImpl;
    private final TrackActivityImpl trackActivityImpl;
    private final TranscodingActivityImpl transcodingActivityImpl;

    @Override
    public void run(String... args) {
        WorkerFactory factory = WorkerFactory.newInstance(workflowClient);
        Worker worker = factory.newWorker(WORKFLOW_TASK_QUEUE);

        worker.registerWorkflowImplementationFactory(
                ArtistRegistrationWorkflow.class,
                () -> new ArtistRegistrationWorkflowImpl(converter)
        );
        worker.registerWorkflowImplementationFactory(
                TrackUploadWorkflow.class,
                () -> new TrackUploadWorkflowImpl(converter)
        );
        worker.registerWorkflowImplementationFactory(
                AlbumUploadWorkflow.class,
                () -> new AlbumUploadWorkflowImpl(converter)
        );
        worker.registerWorkflowImplementationFactory(
                ArtistChangeAvailabilityStatusWorkflow.class,
                ArtistChangeAvailabilityStatusWorkflowImpl::new
        );
        worker.registerWorkflowImplementationFactory(
                AlbumChangeAvailabilityStatusWorkflow.class,
                AlbumChangeAvailabilityStatusWorkflowImpl::new
        );
        worker.registerWorkflowImplementationFactory(
                TrackChangeAvailabilityStatusWorkflow.class,
                TrackChangeAvailabilityStatusWorkflowImpl::new
        );

        worker.registerActivitiesImplementations(artistActivityImpl);
        worker.registerActivitiesImplementations(albumActivityImpl);
        worker.registerActivitiesImplementations(trackActivityImpl);
        worker.registerActivitiesImplementations(transcodingActivityImpl);

        factory.start();
    }
}