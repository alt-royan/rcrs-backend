package org.ultra.rcrs.workflow.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.ultra.rcrs.workflow.client.model.ArtistsToEntityModel;
import org.ultra.rcrs.workflow.client.model.OthersToTrackModel;
import org.ultra.rcrs.workflow.client.model.TrackUploadModel;
import org.ultra.rcrs.workflow.dto.*;
import org.ultra.rcrs.workflow.dto.response.CreateResponse;

@FeignClient(name = "track-write-client", url = "${feign.metadata-service.url}")
public interface TrackClient {

    @PostMapping("/tracks")
    ResponseEntity<CreateResponse> createTrack(TrackUploadModel request);

    @PostMapping("/tracks/{trackId}/artists")
    ResponseEntity<Void> addArtistsToTrack(ArtistsToEntityModel request, @PathVariable String trackId);

    @DeleteMapping("/tracks/{trackId}/artists")
    ResponseEntity<Void> deleteArtistsFromTrack(ArtistsToEntityModel request, @PathVariable String trackId);

    @PostMapping("/tracks/{trackId}/others")
    ResponseEntity<Void> addOthersToTrack(OthersToTrackModel request, @PathVariable String trackId);

    @DeleteMapping("/tracks/{trackId}/others")
    ResponseEntity<Void> deleteOthersFromTrack(OthersToTrackModel request, @PathVariable String trackId);

    @PutMapping("/tracks/{trackId}/status")
    ResponseEntity<Void> updateTrackStatus(StatusDto statusDto, @PathVariable String trackId);

    @PutMapping("/tracks/{trackId}/hide")
    ResponseEntity<Void> hideTrack(@PathVariable String trackId);

    @PutMapping("/tracks/{trackId}/active")
    ResponseEntity<Void> activeTrack(@PathVariable String trackId);

    @DeleteMapping("/tracks/{trackId}")
    ResponseEntity<Void> markTrackDeleted(@PathVariable String trackId);
}
