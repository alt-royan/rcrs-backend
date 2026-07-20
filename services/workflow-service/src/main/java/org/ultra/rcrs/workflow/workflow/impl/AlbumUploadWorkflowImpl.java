package org.ultra.rcrs.workflow.workflow.impl;

import io.temporal.api.enums.v1.ParentClosePolicy;
import io.temporal.common.RetryOptions;
import io.temporal.spring.boot.WorkflowImpl;
import io.temporal.workflow.Async;
import io.temporal.workflow.ChildWorkflowOptions;
import io.temporal.workflow.Promise;
import io.temporal.workflow.Workflow;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.ultra.rcrs.workflow.client.model.ArtistsToEntityModel;
import org.ultra.rcrs.workflow.converter.UploadRequestConverter;
import org.ultra.rcrs.workflow.dto.ArtistDto;
import org.ultra.rcrs.workflow.dto.request.AlbumUploadRequest;
import org.ultra.rcrs.workflow.dto.request.TrackUploadRequest;
import org.ultra.rcrs.workflow.dto.response.CreateResponse;
import org.ultra.rcrs.workflow.workflow.AlbumUploadWorkflow;
import org.ultra.rcrs.workflow.workflow.BaseWorkflow;
import org.ultra.rcrs.workflow.workflow.TrackUploadWorkflow;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.ultra.rcrs.workflow.config.TemporalConfig.WORKFLOW_TASK_QUEUE;

@Component
@WorkflowImpl(taskQueues = WORKFLOW_TASK_QUEUE)
@RequiredArgsConstructor
public class AlbumUploadWorkflowImpl extends BaseWorkflow implements AlbumUploadWorkflow {

    private final UploadRequestConverter converter;

    //можно добавить saga
    @Override
    public CreateResponse uploadAlbum(AlbumUploadRequest request) {
        List<TrackUploadRequest> tracks = request.tracks() == null ? new ArrayList<>() : request.tracks();
        List<ArtistDto> artists = request.artists() == null ? new ArrayList<>() : request.artists();
        //проверить  что все аудио дошли до s3

        var albumModel = converter.toAlbumCreateModel(request);
        var albumRes = activityFactory.albumActivity().createAlbum(albumModel);

        String albumId = albumRes.id();
        if (!artists.isEmpty()) {
            activityFactory.albumActivity().addArtistsToAlbum(new ArtistsToEntityModel(artists), albumId);
        }

        List<Promise<Void>> starts = new ArrayList<>();
        for (TrackUploadRequest track : tracks) {
            TrackUploadWorkflow child = Workflow.newChildWorkflowStub(
                    TrackUploadWorkflow.class,
                    ChildWorkflowOptions.newBuilder()
                            .setTaskQueue(WORKFLOW_TASK_QUEUE)
                            .setWorkflowId(UUID.randomUUID().toString())
                            .setParentClosePolicy(ParentClosePolicy.PARENT_CLOSE_POLICY_ABANDON)
                            .setRetryOptions(
                                    RetryOptions.newBuilder()
                                            .setMaximumAttempts(1)
                                            .build())
                            .build());

            Promise<Void> promise = Async.procedure(child::uploadTrack, track, albumId);
            starts.add(promise);
        }

        Workflow.await(() ->
                starts.stream().allMatch(Promise::isCompleted)
        );

        return new CreateResponse(albumId);
    }
}
