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
import org.ultra.rcrs.metadata.dto.request.ArtistCreateRequest;
import org.ultra.rcrs.metadata.dto.response.CreateResponse;
import org.ultra.rcrs.metadata.service.ArtistService;
import org.ultra.rcrs.utils.Url62;

@Tag(name = "Artist Write API", description = "Write-side (command) operations for creating and managing artists in the catalog. Persists changes in Postgres and publishes protobuf domain events to Kafka for downstream projections (metadata-read-service) and search indexing (search-service).")
@RestController
@RequiredArgsConstructor
@RequestMapping("/catalog/artists")
public class ArtistWriteController {

    private final ArtistService artistService;

    @Operation(summary = "Create a new artist", description = "Persists a new artist with availability status ACTIVE, then publishes an ARTIST_CREATED domain event to Kafka.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Artist created successfully"),
            @ApiResponse(responseCode = "400", description = "Request body is malformed or fails validation"),
            @ApiResponse(responseCode = "500", description = "Unexpected internal error")
    })
    @PostMapping
    public ResponseEntity<CreateResponse> createArtist(@RequestBody @Validated ArtistCreateRequest request) {
        var res = artistService.createArtist(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new CreateResponse(res));
    }

    @Operation(summary = "Hide an artist", description = "Marks the artist as HIDDEN (availability status) and publishes an ARTIST_HIDDEN domain event to Kafka.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Artist hidden successfully"),
            @ApiResponse(responseCode = "400", description = "The artist id could not be decoded"),
            @ApiResponse(responseCode = "500", description = "Unexpected internal error")
    })
    @PutMapping("/{artistId}/hide")
    public ResponseEntity<Void> hideArtist(@Parameter(description = "Base62-encoded (Url62) public identifier of the artist") @PathVariable("artistId") String artistId) {
        artistService.hideArtist(Url62.decode(artistId));
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Activate an artist", description = "Marks the artist as ACTIVE (availability status) and publishes an ARTIST_ACTIVATED domain event to Kafka.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Artist activated successfully"),
            @ApiResponse(responseCode = "400", description = "The artist id could not be decoded"),
            @ApiResponse(responseCode = "500", description = "Unexpected internal error")
    })
    @PutMapping("/{artistId}/active")
    public ResponseEntity<Void> activeArtist(@Parameter(description = "Base62-encoded (Url62) public identifier of the artist") @PathVariable("artistId") String artistId) {
        artistService.activeArtist(Url62.decode(artistId));
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Soft-delete an artist", description = "Marks the artist as DELETED (availability status). The record is not removed from the database immediately; the periodic purge (see /catalog/purge) later removes it permanently. Publishes an ARTIST_DELETED domain event to Kafka.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Artist marked as deleted successfully"),
            @ApiResponse(responseCode = "400", description = "The artist id could not be decoded"),
            @ApiResponse(responseCode = "500", description = "Unexpected internal error")
    })
    @DeleteMapping("/{artistId}")
    public ResponseEntity<Void> deleteArtist(@Parameter(description = "Base62-encoded (Url62) public identifier of the artist") @PathVariable("artistId") String artistId) {
        artistService.markArtistDelete(Url62.decode(artistId));
        return ResponseEntity.noContent().build();
    }
}
