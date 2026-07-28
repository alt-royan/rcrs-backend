package org.ultra.rcrs.metadata.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.ultra.rcrs.metadata.dto.StatusDto;
import org.ultra.rcrs.metadata.dto.request.ArtistsToEntityRequest;
import org.ultra.rcrs.metadata.dto.request.OthersToTrackRequest;
import org.ultra.rcrs.metadata.dto.request.TrackUploadRequest;
import org.ultra.rcrs.metadata.dto.response.CreateResponse;
import org.ultra.rcrs.metadata.service.TrackService;
import org.ultra.rcrs.utils.Url62;

@Tag(name = "Track Write API", description = "Write-side (command) operations for creating and managing tracks in the catalog. Persists changes in Postgres and publishes protobuf domain events to Kafka for downstream projections (metadata-read-service) and search indexing (search-service).")
@RestController
@RequiredArgsConstructor
@RequestMapping("/catalog/tracks")
public class TrackWriteController {

    private final TrackService trackService;

    @Operation(summary = "Create a new track", description = "Persists a new track under the given album with lifecycle status CREATED and availability status ACTIVE, then publishes a TRACK_CREATED domain event to Kafka.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Track created successfully"),
            @ApiResponse(responseCode = "400", description = "Request body is malformed or fails validation"),
            @ApiResponse(responseCode = "404", description = "The referenced album does not exist"),
            @ApiResponse(responseCode = "500", description = "Unexpected internal error")
    })
    @PostMapping
    public ResponseEntity<CreateResponse> createTrack(@RequestBody @Validated TrackUploadRequest request) {
        var res = trackService.createTrack(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new CreateResponse(res));
    }

    @Operation(summary = "Attach artists to a track", description = "Associates one or more artists (with a role) with the given track and publishes an ARTIST_ADDED_TO_TRACK domain event per artist to Kafka.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Artists successfully attached to the track"),
            @ApiResponse(responseCode = "400", description = "Request body is malformed, fails validation, or the track id could not be decoded"),
            @ApiResponse(responseCode = "404", description = "Track or one of the referenced artists does not exist"),
            @ApiResponse(responseCode = "500", description = "Unexpected internal error")
    })
    @PostMapping("/{trackId}/artists")
    public ResponseEntity<Void> addArtistsToTrack(@RequestBody @Validated ArtistsToEntityRequest request, @Parameter(description = "Base62-encoded (Url62) public identifier of the track") @PathVariable("trackId") String trackId) {
        trackService.addAllArtistToTrack(request.getArtists(), Url62.decode(trackId));
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Detach artists from a track", description = "Removes the association between one or more artists and the given track, publishing an ARTIST_DELETED_FROM_TRACK domain event per artist to Kafka.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Artists successfully detached from the track"),
            @ApiResponse(responseCode = "400", description = "Request body is malformed, fails validation, or the track id could not be decoded"),
            @ApiResponse(responseCode = "500", description = "Unexpected internal error")
    })
    @DeleteMapping("/{trackId}/artists")
    public ResponseEntity<Void> deleteArtistsFromAlbum(@RequestBody @Validated ArtistsToEntityRequest request, @Parameter(description = "Base62-encoded (Url62) public identifier of the track") @PathVariable("trackId") String trackId) {
        trackService.deleteAllArtistFromTrack(request.getArtists(), Url62.decode(trackId));
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Attach contributors to a track", description = "Associates one or more non-performing contributors (e.g. producers, writers) with the given track.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Contributors successfully attached to the track"),
            @ApiResponse(responseCode = "400", description = "Request body is malformed, fails validation, or the track id could not be decoded"),
            @ApiResponse(responseCode = "404", description = "Track does not exist"),
            @ApiResponse(responseCode = "500", description = "Unexpected internal error")
    })
    @PostMapping("/{trackId}/others")
    public ResponseEntity<Void> addOthersToTrack(@RequestBody @Validated OthersToTrackRequest request, @Parameter(description = "Base62-encoded (Url62) public identifier of the track") @PathVariable("trackId") String trackId) {
        trackService.addAllOthersToTrack(request.getOthers(), Url62.decode(trackId));
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Detach contributors from a track", description = "Removes the association between one or more non-performing contributors and the given track.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Contributors successfully detached from the track"),
            @ApiResponse(responseCode = "400", description = "Request body is malformed, fails validation, or the track id could not be decoded"),
            @ApiResponse(responseCode = "500", description = "Unexpected internal error")
    })
    @DeleteMapping("/{trackId}/others")
    public ResponseEntity<Void> deleteOthersFromTrack(@RequestBody @Validated OthersToTrackRequest request, @Parameter(description = "Base62-encoded (Url62) public identifier of the track") @PathVariable("trackId") String trackId) {
        trackService.deleteAllOthersFromTrack(request.getOthers(), Url62.decode(trackId));
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Update track lifecycle status", description = "Updates the track's lifecycle status (CREATED, TRANSCODING, FAILED, READY, PUBLISHED) and publishes a domain event reflecting the change to Kafka. Reaching READY on all tracks of an album may also advance the album's own lifecycle status.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lifecycle status updated successfully"),
            @ApiResponse(responseCode = "400", description = "Request body is malformed, fails validation, or the track id could not be decoded"),
            @ApiResponse(responseCode = "500", description = "Unexpected internal error")
    })
    @PutMapping("/{trackId}/status")
    public ResponseEntity<Void> updateTrackStatus(@RequestBody @Validated StatusDto statusDto, @Parameter(description = "Base62-encoded (Url62) public identifier of the track") @PathVariable("trackId") String trackId) {
        trackService.updateLifecycleStatus(statusDto.getStatus(), Url62.decode(trackId));
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Hide a track", description = "Marks the track as HIDDEN (availability status) and publishes a TRACK_HIDDEN domain event to Kafka.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Track hidden successfully"),
            @ApiResponse(responseCode = "400", description = "The track id could not be decoded"),
            @ApiResponse(responseCode = "500", description = "Unexpected internal error")
    })
    @PutMapping("/{trackId}/hide")
    public ResponseEntity<Void> hideTrack(@Parameter(description = "Base62-encoded (Url62) public identifier of the track") @PathVariable("trackId") String trackId) {
        trackService.hideTrack(Url62.decode(trackId));
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Activate a track", description = "Marks the track as ACTIVE (availability status) and publishes a TRACK_ACTIVATED domain event to Kafka.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Track activated successfully"),
            @ApiResponse(responseCode = "400", description = "The track id could not be decoded"),
            @ApiResponse(responseCode = "500", description = "Unexpected internal error")
    })
    @PutMapping("/{trackId}/active")
    public ResponseEntity<Void> activeTrack(@Parameter(description = "Base62-encoded (Url62) public identifier of the track") @PathVariable("trackId") String trackId) {
        trackService.activeTrack(Url62.decode(trackId));
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Soft-delete a track", description = "Marks the track as DELETED (availability status). The record is not removed from the database immediately; the periodic purge (see /catalog/purge) later removes it permanently. Publishes a TRACK_DELETED domain event to Kafka.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Track marked as deleted successfully"),
            @ApiResponse(responseCode = "400", description = "The track id could not be decoded"),
            @ApiResponse(responseCode = "500", description = "Unexpected internal error")
    })
    @DeleteMapping("/{trackId}")
    public ResponseEntity<Void> deleteTrack(@Parameter(description = "Base62-encoded (Url62) public identifier of the track") @PathVariable("trackId") String trackId) {
        trackService.markTrackDelete(Url62.decode(trackId));
        return ResponseEntity.noContent().build();
    }

}
