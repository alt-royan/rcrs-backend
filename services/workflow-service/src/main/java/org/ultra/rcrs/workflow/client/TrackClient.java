package org.ultra.rcrs.workflow.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.ultra.rcrs.workflow.dto.*;

@FeignClient(name = "track-write-client", url = "${feign.metadata-service.url}")
public interface TrackClient {

    @PostMapping("/tracks")
    ResponseEntity<CreateResponse> createTrack(TrackUploadRequest request);

    @PostMapping("/tracks/{trackId}/artists")
    ResponseEntity<Void> addArtistsToTrack(ArtistsToEntityRequest request, String trackId);

    @DeleteMapping("/tracks/{trackId}/artists")
    ResponseEntity<Void> deleteArtistsFromTrack(ArtistsToEntityRequest request, String trackId);

    @PostMapping("/tracks/{trackId}/others")
    ResponseEntity<Void> addOthersToTrack(OthersToTrackRequest request, String trackId);

    @DeleteMapping("/tracks/{trackId}/others")
    ResponseEntity<Void> deleteOthersFromTrack(OthersToTrackRequest request, String trackId);

    @PutMapping("/tracks/{trackId}/status")
    ResponseEntity<Void> updateTrackStatus(StatusDto statusDto, String trackId);

    @PutMapping("/tracks/{trackId}/hide")
    ResponseEntity<Void> hideTrack(String trackId);

    @PutMapping("/tracks/{trackId}/active")
    ResponseEntity<Void> activeTrack(String trackId);

    @DeleteMapping("/tracks/{trackId}")
    ResponseEntity<Void> markTrackDeleted(String trackId);
}
