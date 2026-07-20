package org.ultra.rcrs.workflow.activity.impl;

import io.temporal.spring.boot.ActivityImpl;
import org.springframework.stereotype.Component;
import org.ultra.rcrs.workflow.activity.TrackActivity;
import org.ultra.rcrs.workflow.client.TrackClient;
import org.ultra.rcrs.workflow.client.model.ArtistsToEntityModel;
import org.ultra.rcrs.workflow.client.model.OthersToTrackModel;
import org.ultra.rcrs.workflow.client.model.TrackUploadModel;
import org.ultra.rcrs.workflow.dto.StatusDto;
import org.ultra.rcrs.workflow.dto.response.CreateResponse;

@Component
@ActivityImpl
public class TrackActivityImpl implements TrackActivity {

    private final TrackClient trackClient;

    public TrackActivityImpl(TrackClient trackClient) {
        this.trackClient = trackClient;
    }

    @Override
    public CreateResponse createTrack(TrackUploadModel request) {
        var res = trackClient.createTrack(request);
        if (!res.getStatusCode().is2xxSuccessful() || res.getBody() == null) {
            throw new RuntimeException("Unsupported behavior: response is not 2xx");
        }
        return res.getBody();
    }

    @Override
    public void addArtistsToTrack(ArtistsToEntityModel request, String trackId) {
        trackClient.addArtistsToTrack(request, trackId);
    }

    @Override
    public void deleteArtistsFromTrack(ArtistsToEntityModel request, String trackId) {
        trackClient.deleteArtistsFromTrack(request, trackId);
    }

    @Override
    public void addOthersToTrack(OthersToTrackModel request, String trackId) {
        trackClient.addOthersToTrack(request, trackId);
    }

    @Override
    public void deleteOthersFromTrack(OthersToTrackModel request, String trackId) {
        trackClient.deleteOthersFromTrack(request, trackId);
    }

    @Override
    public void updateTrackStatus(StatusDto statusDto, String trackId) {
        trackClient.updateTrackStatus(statusDto, trackId);
    }

    @Override
    public void hideTrack(String trackId) {
        trackClient.hideTrack(trackId);
    }

    @Override
    public void activeTrack(String trackId) {
        trackClient.activeTrack(trackId);
    }

    @Override
    public void markTrackDeleted(String trackId) {
        trackClient.markTrackDeleted(trackId);
    }
}
