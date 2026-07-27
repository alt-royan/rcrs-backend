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
import org.ultra.rcrs.metadata.dto.request.AlbumUploadRequest;
import org.ultra.rcrs.metadata.dto.request.ArtistsToEntityRequest;
import org.ultra.rcrs.metadata.dto.response.CreateResponse;
import org.ultra.rcrs.metadata.service.AlbumService;
import org.ultra.rcrs.utils.Url62;

@Tag(name = "Album Write API", description = "Write-side (command) operations for creating and managing albums in the catalog. Persists changes in Postgres and publishes protobuf domain events to Kafka for downstream projections (metadata-read-service) and search indexing (search-service).")
@RestController
@RequiredArgsConstructor
@RequestMapping("/catalog/albums")
public class AlbumWriteController {

    private final AlbumService albumService;

    @Operation(summary = "Create a new album", description = "Persists a new album with lifecycle status CREATED and availability status ACTIVE, then publishes an ALBUM_CREATED domain event to Kafka.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Album created successfully"),
            @ApiResponse(responseCode = "400", description = "Request body is malformed or fails validation"),
            @ApiResponse(responseCode = "500", description = "Unexpected internal error")
    })
    @PostMapping
    public ResponseEntity<CreateResponse> createAlbum(@RequestBody @Validated AlbumUploadRequest request) {
        var res = albumService.createAlbum(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new CreateResponse(res));
    }

    @Operation(summary = "Attach artists to an album", description = "Associates one or more artists (with a role) with the given album and publishes an ARTIST_ADDED_TO_ALBUM domain event per artist to Kafka.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Artists successfully attached to the album"),
            @ApiResponse(responseCode = "400", description = "Request body is malformed, fails validation, or the album id could not be decoded"),
            @ApiResponse(responseCode = "404", description = "Album or one of the referenced artists does not exist"),
            @ApiResponse(responseCode = "500", description = "Unexpected internal error")
    })
    @PostMapping("/{albumId}/artists")
    public ResponseEntity<Void> addArtistsToAlbum(@RequestBody @Validated ArtistsToEntityRequest request, @Parameter(description = "Base62-encoded (Url62) public identifier of the album") @PathVariable("albumId") String albumId) {
        albumService.addAllArtistToAlbum(request.getArtists(), Url62.decode(albumId));
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Detach artists from an album", description = "Removes the association between one or more artists and the given album, publishing an ARTIST_DELETED_FROM_ALBUM domain event per artist to Kafka.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Artists successfully detached from the album"),
            @ApiResponse(responseCode = "400", description = "Request body is malformed, fails validation, or the album id could not be decoded"),
            @ApiResponse(responseCode = "500", description = "Unexpected internal error")
    })
    @DeleteMapping("/{albumId}/artists")
    public ResponseEntity<Void> deleteArtistsFromAlbum(@RequestBody @Validated ArtistsToEntityRequest request, @Parameter(description = "Base62-encoded (Url62) public identifier of the album") @PathVariable("albumId") String albumId) {
        albumService.deleteAllArtistFromAlbum(request.getArtists(), Url62.decode(albumId));
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Update album lifecycle status", description = "Updates the album's lifecycle status (CREATED, TRANSCODING, FAILED, READY, PUBLISHED) and publishes an ALBUM_LIFECYCLE_STATUS_UPDATED domain event to Kafka.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lifecycle status updated successfully"),
            @ApiResponse(responseCode = "400", description = "Request body is malformed, fails validation, or the album id could not be decoded"),
            @ApiResponse(responseCode = "500", description = "Unexpected internal error")
    })
    @PutMapping("/{albumId}/status")
    public ResponseEntity<Void> updateAlbumStatus(@RequestBody @Validated StatusDto statusDto, @Parameter(description = "Base62-encoded (Url62) public identifier of the album") @PathVariable("albumId") String albumId) {
        albumService.updateLifecycleStatus(statusDto.getStatus(), Url62.decode(albumId));
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Hide an album", description = "Marks the album and all of its tracks as HIDDEN (availability status), publishing an ALBUM_HIDDEN domain event (and a corresponding hidden event per track) to Kafka.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Album hidden successfully"),
            @ApiResponse(responseCode = "400", description = "The album id could not be decoded"),
            @ApiResponse(responseCode = "500", description = "Unexpected internal error")
    })
    @PutMapping("/{albumId}/hide")
    public ResponseEntity<Void> hideAlbum(@Parameter(description = "Base62-encoded (Url62) public identifier of the album") @PathVariable("albumId") String albumId) {
        albumService.hideAlbum(Url62.decode(albumId));
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Activate an album", description = "Marks the album and all of its tracks as ACTIVE (availability status), publishing an ALBUM_ACTIVATED domain event (and a corresponding activated event per track) to Kafka.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Album activated successfully"),
            @ApiResponse(responseCode = "400", description = "The album id could not be decoded"),
            @ApiResponse(responseCode = "500", description = "Unexpected internal error")
    })
    @PutMapping("/{albumId}/active")
    public ResponseEntity<Void> activeAlbum(@Parameter(description = "Base62-encoded (Url62) public identifier of the album") @PathVariable("albumId") String albumId) {
        albumService.activeAlbum(Url62.decode(albumId));
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Soft-delete an album", description = "Marks the album and all of its tracks as DELETED (availability status). Records are not removed from the database immediately; the periodic purge (see /catalog/purge) later removes them permanently. Publishes an ALBUM_DELETED domain event (and a corresponding deleted event per track) to Kafka.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Album marked as deleted successfully"),
            @ApiResponse(responseCode = "400", description = "The album id could not be decoded"),
            @ApiResponse(responseCode = "500", description = "Unexpected internal error")
    })
    @DeleteMapping("/{albumId}")
    public ResponseEntity<Void> deleteAlbum(@Parameter(description = "Base62-encoded (Url62) public identifier of the album") @PathVariable("albumId") String albumId) {
        albumService.markAlbumDelete(Url62.decode(albumId));
        return ResponseEntity.noContent().build();
    }
}
