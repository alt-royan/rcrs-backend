package org.ultra.rcrs.mediaservice.temporal.worker;

import io.temporal.client.WorkflowClient;
import io.temporal.worker.Worker;
import io.temporal.worker.WorkerFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
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
    private final ValidateImageActivityImpl validateImageActivityImpl;
    private final SaveOriginalImageActivityImpl saveOriginalImageActivityImpl;
    private final SaveThumbnailActivityImpl saveThumbnailActivityImpl;
    private final ValidateAudioActivityImpl validateAudioActivityImpl;
    private final ProbeAudioMetadataActivityImpl probeAudioMetadataActivityImpl;
    private final SaveOriginalAudioToS3ActivityImpl saveOriginalAudioToS3ActivityImpl;
    private final SaveAudioRecordActivityImpl saveAudioRecordActivityImpl;
    private final NormalizeAudioActivityImpl normalizeAudioActivityImpl;
    private final TranscodingStatusActivityImpl transcodingStatusActivityImpl;

    @Value("${cdn.audios.bitrates}")
    private final String[] bitrates;

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
                () -> new AudioTranscodingWorkflowImpl(bitrates)
        );

        worker.registerActivitiesImplementations(validateImageActivityImpl);
        worker.registerActivitiesImplementations(saveOriginalImageActivityImpl);
        worker.registerActivitiesImplementations(saveThumbnailActivityImpl);
        worker.registerActivitiesImplementations(validateAudioActivityImpl);
        worker.registerActivitiesImplementations(probeAudioMetadataActivityImpl);
        worker.registerActivitiesImplementations(saveOriginalAudioToS3ActivityImpl);
        worker.registerActivitiesImplementations(saveAudioRecordActivityImpl);
        worker.registerActivitiesImplementations(normalizeAudioActivityImpl);
        worker.registerActivitiesImplementations(transcodingStatusActivityImpl);

        factory.start();
    }
}
