package org.ultra.rcrs.workflow.handler;

import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import io.temporal.common.RetryOptions;
import io.temporal.common.SearchAttributeKey;
import io.temporal.common.SearchAttributes;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.ultra.rcrs.enums.EntityStatus;
import org.ultra.rcrs.workflow.dto.request.AlbumUploadRequest;
import org.ultra.rcrs.workflow.dto.request.ArtistUploadRequest;
import org.ultra.rcrs.workflow.dto.response.CreateResponse;
import org.ultra.rcrs.workflow.workflow.*;

import java.util.Map;
import java.util.UUID;

import static org.ultra.rcrs.workflow.config.TemporalConfig.WORKFLOW_TASK_QUEUE;

@Service
@RequiredArgsConstructor
public class WorkflowHandler {

    private final WorkflowClient workflowClient;

    @Value("${workflow.purge.cron}")
    private String purgeCronSchedule;

    public CreateResponse startRegisterArtistWorkflow(ArtistUploadRequest request,Jwt jwt) {

        ArtistRegistrationWorkflow workflow = workflowClient.newWorkflowStub(
                ArtistRegistrationWorkflow.class,
                getWorkflowOptions(jwt)
        );
        var future = WorkflowClient.execute(workflow::registerArtist, request);

        return future.join();
    }

    public void startArtistChangeAvailabilityStatusWorkflow(EntityStatus status, String id, Jwt jwt) {

        ArtistChangeAvailabilityStatusWorkflow workflow = workflowClient.newWorkflowStub(
                ArtistChangeAvailabilityStatusWorkflow.class,
                getWorkflowOptions(jwt)
        );
        var future = WorkflowClient.execute(workflow::changeAvailabilityStatus, status, id);
        future.join();
    }

    public void startAlbumChangeAvailabilityStatusWorkflow(EntityStatus status, String id, Jwt jwt) {

        AlbumChangeAvailabilityStatusWorkflow workflow = workflowClient.newWorkflowStub(
                AlbumChangeAvailabilityStatusWorkflow.class,
                getWorkflowOptions(jwt)
        );
        var future = WorkflowClient.execute(workflow::changeAvailabilityStatus, status, id);
        future.join();
    }

    public void startTrackChangeAvailabilityStatusWorkflow(EntityStatus status, String id, Jwt jwt) {

        TrackChangeAvailabilityStatusWorkflow workflow = workflowClient.newWorkflowStub(
                TrackChangeAvailabilityStatusWorkflow.class,
                getWorkflowOptions(jwt)
        );
        var future = WorkflowClient.execute(workflow::changeAvailabilityStatus, status, id);
        future.join();
    }

    public CreateResponse startAlbumUploadWorkflow(AlbumUploadRequest request, Jwt jwt) {

        AlbumUploadWorkflow workflow = workflowClient.newWorkflowStub(
                AlbumUploadWorkflow.class,
                getWorkflowOptions(jwt)
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
                        .setCronSchedule(purgeCronSchedule)
                        .setRetryOptions(
                                RetryOptions.newBuilder()
                                        .setMaximumAttempts(1)
                                        .build())
                        .build()
        );
        WorkflowClient.execute(workflow::purge);
    }

    private static @NonNull WorkflowOptions getWorkflowOptions(Jwt jwt) {
        return WorkflowOptions.newBuilder()
                .setTaskQueue(WORKFLOW_TASK_QUEUE)
                .setWorkflowId(UUID.randomUUID().toString())
                .setMemo(Map.of("userId", jwt.getSubject(), "username", jwt.getClaimAsString("preferred_username")))
                .setRetryOptions(
                        RetryOptions.newBuilder()
                                .setMaximumAttempts(1)
                                .build())
                .build();
    }

}
