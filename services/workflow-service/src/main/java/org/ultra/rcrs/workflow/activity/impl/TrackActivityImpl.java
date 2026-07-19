package org.ultra.rcrs.workflow.activity.impl;

import io.temporal.spring.boot.ActivityImpl;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.ultra.rcrs.workflow.activity.TrackActivity;
import org.ultra.rcrs.workflow.client.TrackClient;
import org.ultra.rcrs.workflow.dto.*;

@Component
@ActivityImpl
public class TrackActivityImpl implements TrackActivity {

    private final TrackClient trackClient;

    public TrackActivityImpl(TrackClient trackClient) {
        this.trackClient = trackClient;
    }

    @Override
    public ResponseEntity<CreateResponse> createTrack(TrackUploadRequest request) {
        return trackClient.createTrack(request);
    }

    @Override
    public ResponseEntity<Void> addArtistsToTrack(ArtistsToEntityRequest request, String trackId) {
        return trackClient.addArtistsToTrack(request, trackId);
    }

    @Override
    public ResponseEntity<Void> deleteArtistsFromTrack(ArtistsToEntityRequest request, String trackId) {
        return trackClient.deleteArtistsFromTrack(request, trackId);
    }

    @Override
    public ResponseEntity<Void> addOthersToTrack(OthersToTrackRequest request, String trackId) {
        return trackClient.addOthersToTrack(request, trackId);
    }

    @Override
    public ResponseEntity<Void> deleteOthersFromTrack(OthersToTrackRequest request, String trackId) {
        return trackClient.deleteOthersFromTrack(request, trackId);
    }

    @Override
    public ResponseEntity<Void> updateTrackStatus(StatusDto statusDto, String trackId) {
        return trackClient.updateTrackStatus(statusDto, trackId);
    }

    @Override
    public ResponseEntity<Void> hideTrack(String trackId) {
        return trackClient.hideTrack(trackId);
    }

    @Override
    public ResponseEntity<Void> activeTrack(String trackId) {
        return trackClient.activeTrack(trackId);
    }

    @Override
    public ResponseEntity<Void> markTrackDeleted(String trackId) {
        return trackClient.markTrackDeleted(trackId);
    }
}
