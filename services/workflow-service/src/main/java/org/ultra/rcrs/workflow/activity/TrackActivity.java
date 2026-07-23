package org.ultra.rcrs.workflow.activity;

import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;
import org.ultra.rcrs.workflow.client.model.ArtistsToEntityModel;
import org.ultra.rcrs.workflow.client.model.OthersToTrackModel;
import org.ultra.rcrs.workflow.client.model.TrackUploadModel;
import org.ultra.rcrs.workflow.dto.StatusDto;
import org.ultra.rcrs.workflow.dto.response.CreateResponse;

@ActivityInterface
public interface TrackActivity {

    @ActivityMethod
    CreateResponse createTrack(TrackUploadModel request);

    @ActivityMethod
    void addArtistsToTrack(ArtistsToEntityModel request, String trackId);

    @ActivityMethod
    void deleteArtistsFromTrack(ArtistsToEntityModel request, String trackId);

    @ActivityMethod
    void addOthersToTrack(OthersToTrackModel request, String trackId);

    @ActivityMethod
    void deleteOthersFromTrack(OthersToTrackModel request, String trackId);

    @ActivityMethod
    void updateTrackStatus(StatusDto statusDto, String trackId);

    @ActivityMethod
    void hideTrack(String trackId);

    @ActivityMethod
    void activeTrack(String trackId);

    @ActivityMethod
    void markTrackDeleted(String trackId);
}
