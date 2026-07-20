package org.ultra.rcrs.workflow.handler;

import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import io.temporal.common.RetryOptions;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.ultra.rcrs.enums.EntityStatus;
import org.ultra.rcrs.workflow.dto.request.AlbumUploadRequest;
import org.ultra.rcrs.workflow.dto.request.ArtistUploadRequest;
import org.ultra.rcrs.workflow.dto.response.CreateResponse;
import org.ultra.rcrs.workflow.workflow.*;

import java.util.UUID;

import static org.ultra.rcrs.workflow.config.TemporalConfig.WORKFLOW_TASK_QUEUE;

@Service
@RequiredArgsConstructor
public class WorkflowHandler {

    private final WorkflowClient workflowClient;

    public CreateResponse startRegisterArtistWorkflow(ArtistUploadRequest request) {

        ArtistRegistrationWorkflow workflow = workflowClient.newWorkflowStub(
                ArtistRegistrationWorkflow.class,
                WorkflowOptions.newBuilder()
                        .setTaskQueue(WORKFLOW_TASK_QUEUE)
                        .setWorkflowId(UUID.randomUUID().toString())
                        .setRetryOptions(
                                RetryOptions.newBuilder()
                                        .setMaximumAttempts(1)
                                        .build())
                        .build()
        );
        var future = WorkflowClient.execute(workflow::registerArtist, request);

        return future.join();
    }

    public void startArtistChangeAvailabilityStatusWorkflow(EntityStatus status, String id) {

        ArtistChangeAvailabilityStatusWorkflow workflow = workflowClient.newWorkflowStub(
                ArtistChangeAvailabilityStatusWorkflow.class,
                WorkflowOptions.newBuilder()
                        .setTaskQueue(WORKFLOW_TASK_QUEUE)
                        .setWorkflowId(UUID.randomUUID().toString())
                        .setRetryOptions(
                                RetryOptions.newBuilder()
                                        .setMaximumAttempts(1)
                                        .build())
                        .build()
        );
        var future = WorkflowClient.execute(workflow::changeAvailabilityStatus, status, id);
        future.join();
    }

    public void startAlbumChangeAvailabilityStatusWorkflow(EntityStatus status, String id) {

        AlbumChangeAvailabilityStatusWorkflow workflow = workflowClient.newWorkflowStub(
                AlbumChangeAvailabilityStatusWorkflow.class,
                WorkflowOptions.newBuilder()
                        .setTaskQueue(WORKFLOW_TASK_QUEUE)
                        .setWorkflowId(UUID.randomUUID().toString())
                        .setRetryOptions(
                                RetryOptions.newBuilder()
                                        .setMaximumAttempts(1)
                                        .build())
                        .build()
        );
        var future = WorkflowClient.execute(workflow::changeAvailabilityStatus, status, id);
        future.join();
    }

    public void startTrackChangeAvailabilityStatusWorkflow(EntityStatus status, String id) {

        TrackChangeAvailabilityStatusWorkflow workflow = workflowClient.newWorkflowStub(
                TrackChangeAvailabilityStatusWorkflow.class,
                WorkflowOptions.newBuilder()
                        .setTaskQueue(WORKFLOW_TASK_QUEUE)
                        .setWorkflowId(UUID.randomUUID().toString())
                        .setRetryOptions(
                                RetryOptions.newBuilder()
                                        .setMaximumAttempts(1)
                                        .build())
                        .build()
        );
        var future = WorkflowClient.execute(workflow::changeAvailabilityStatus, status, id);
        future.join();
    }

    public CreateResponse startAlbumUploadWorkflow(AlbumUploadRequest request) {

        AlbumUploadWorkflow workflow = workflowClient.newWorkflowStub(
                AlbumUploadWorkflow.class,
                WorkflowOptions.newBuilder()
                        .setTaskQueue(WORKFLOW_TASK_QUEUE)
                        .setWorkflowId(UUID.randomUUID().toString())
                        .setRetryOptions(
                                RetryOptions.newBuilder()
                                        .setMaximumAttempts(1)
                                        .build())
                        .build()
        );
        var future = WorkflowClient.execute(workflow::uploadAlbum, request);
        return future.join();
    }

    public void startPurgeDeletedWorkflow() {
        PurgeDeletedWorkflow workflow = workflowClient.newWorkflowStub(
                PurgeDeletedWorkflow.class,
                WorkflowOptions.newBuilder()
                        .setTaskQueue(WORKFLOW_TASK_QUEUE)
                        .setWorkflowId("purge-deleted-monthly")
                        .setCronSchedule("0 0 4 1 * *")
                        .setRetryOptions(
                                RetryOptions.newBuilder()
                                        .setMaximumAttempts(1)
                                        .build())
                        .build()
        );
        WorkflowClient.execute(workflow::purge);
    }

}
