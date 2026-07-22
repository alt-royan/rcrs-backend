package org.ultra.rcrs.mediaservice.temporal.worker;

import io.temporal.client.WorkflowClient;
import io.temporal.worker.Worker;
import io.temporal.worker.WorkerFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.ultra.rcrs.mediaservice.config.AudioConfigurationProperties;
import org.ultra.rcrs.mediaservice.temporal.activity.impl.*;
import org.ultra.rcrs.mediaservice.temporal.workflow.AudioTranscodingWorkflow;
import org.ultra.rcrs.mediaservice.temporal.workflow.ImageUploadWorkflow;
import org.ultra.rcrs.mediaservice.temporal.workflow.impl.AudioTranscodingWorkflowImpl;
import org.ultra.rcrs.mediaservice.temporal.workflow.impl.ImageUploadWorkflowImpl;

import static org.ultra.rcrs.mediaservice.temporal.config.TemporalConfig.MEDIA_TASK_QUEUE;

@Component
@RequiredArgsConstructor
public class TemporalWorker implements CommandLineRunner {

    private final WorkflowClient workflowClient;
    private final DbActivityImpl dbActivity;
    private final ProbeAudioMetadataActivityImpl probeAudioMetadataActivity;
    private final S3ActivityImpl s3Activity;
    private final ThumbnailActivityImpl thumbnailActivity;
    private final TranscodeAudioActivityImpl transcodeAudioActivity;
    private final TranscodingStatusActivityImpl transcodingStatusActivity;
    private final ValidateActivityImpl validateActivity;

    private final AudioConfigurationProperties properties;

    @Override
    public void run(String... args) {
        WorkerFactory factory = WorkerFactory.newInstance(workflowClient);
        Worker worker = factory.newWorker(MEDIA_TASK_QUEUE);

        worker.registerWorkflowImplementationFactory(
                ImageUploadWorkflow.class,
                ImageUploadWorkflowImpl::new
        );
        worker.registerWorkflowImplementationFactory(
                AudioTranscodingWorkflow.class,
                () -> new AudioTranscodingWorkflowImpl(properties.getBitrates())
        );

        worker.registerActivitiesImplementations(dbActivity);
        worker.registerActivitiesImplementations(probeAudioMetadataActivity);
        worker.registerActivitiesImplementations(s3Activity);
        worker.registerActivitiesImplementations(thumbnailActivity);
        worker.registerActivitiesImplementations(transcodeAudioActivity);
        worker.registerActivitiesImplementations(transcodingStatusActivity);
        worker.registerActivitiesImplementations(validateActivity);

        factory.start();
    }
}
