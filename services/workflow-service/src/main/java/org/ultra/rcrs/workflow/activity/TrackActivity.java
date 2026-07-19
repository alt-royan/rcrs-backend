package org.ultra.rcrs.workflow.activity;

import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;
import org.springframework.http.ResponseEntity;
import org.ultra.rcrs.workflow.dto.*;

@ActivityInterface
public interface TrackActivity {

    @ActivityMethod
    ResponseEntity<CreateResponse> createTrack(TrackUploadRequest request);

    @ActivityMethod
    ResponseEntity<Void> addArtistsToTrack(ArtistsToEntityRequest request, String trackId);

    @ActivityMethod
    ResponseEntity<Void> deleteArtistsFromTrack(ArtistsToEntityRequest request, String trackId);

    @ActivityMethod
    ResponseEntity<Void> addOthersToTrack(OthersToTrackRequest request, String trackId);

    @ActivityMethod
    ResponseEntity<Void> deleteOthersFromTrack(OthersToTrackRequest request, String trackId);

    @ActivityMethod
    ResponseEntity<Void> updateTrackStatus(StatusDto statusDto, String trackId);

    @ActivityMethod
    ResponseEntity<Void> hideTrack(String trackId);

    @ActivityMethod
    ResponseEntity<Void> activeTrack(String trackId);

    @ActivityMethod
    ResponseEntity<Void> markTrackDeleted(String trackId);
}
