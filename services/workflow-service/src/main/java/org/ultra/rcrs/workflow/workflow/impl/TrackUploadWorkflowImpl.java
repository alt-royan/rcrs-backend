package org.ultra.rcrs.workflow.workflow.impl;

import io.temporal.spring.boot.WorkflowImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.ultra.rcrs.workflow.client.model.ArtistsToEntityModel;
import org.ultra.rcrs.workflow.client.model.OthersToTrackModel;
import org.ultra.rcrs.workflow.converter.UploadRequestConverter;
import org.ultra.rcrs.workflow.dto.ArtistDto;
import org.ultra.rcrs.workflow.dto.OtherArtistDto;
import org.ultra.rcrs.workflow.dto.request.TrackUploadRequest;
import org.ultra.rcrs.workflow.dto.response.CreateResponse;
import org.ultra.rcrs.workflow.workflow.BaseWorkflow;
import org.ultra.rcrs.workflow.workflow.TrackUploadWorkflow;

import java.util.ArrayList;
import java.util.List;

import static org.ultra.rcrs.workflow.config.TemporalConfig.WORKFLOW_TASK_QUEUE;

@Component
@WorkflowImpl(taskQueues = WORKFLOW_TASK_QUEUE)
@RequiredArgsConstructor
public class TrackUploadWorkflowImpl extends BaseWorkflow implements TrackUploadWorkflow {

    private final UploadRequestConverter converter;

    @Override
    public CreateResponse uploadTrack(TrackUploadRequest request, String albumId) {
        List<ArtistDto> artists = request.artists() == null ? new ArrayList<>() : request.artists();
        List<OtherArtistDto> others = request.others() == null ? new ArrayList<>() : request.others();

        var trackModel = converter.toTrackCreateModel(request, albumId);
        var trackRes = activityFactory.trackActivity().createTrack(trackModel);

        String trackId = trackRes.id();
        if (!artists.isEmpty()) {
            activityFactory.trackActivity().addArtistsToTrack(new ArtistsToEntityModel(artists), trackId);
        }

        if (!others.isEmpty()) {
            activityFactory.trackActivity().addOthersToTrack(new OthersToTrackModel(others), trackId);
        }

        activityFactory.transcodingActivity().trackTranscoding(request.uid(), trackId);

        return new CreateResponse(trackId);
    }
}
