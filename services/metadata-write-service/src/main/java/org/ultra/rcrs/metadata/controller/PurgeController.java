package org.ultra.rcrs.metadata.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.ultra.rcrs.metadata.service.PurgeService;

@Tag(name = "Catalog Maintenance API", description = "Administrative/maintenance operations on the catalog write store. Not intended for public or storefront use.")
@RestController
@RequiredArgsConstructor
@RequestMapping("/catalog")
public class PurgeController {

    private final PurgeService purgeService;

    @Operation(
            summary = "Purge soft-deleted catalog records",
            description = "Permanently removes all tracks, albums, and artists whose availability status is DELETED (i.e. previously soft-deleted), along with their artist/track and artist/album associations. For each permanently removed entity, publishes a corresponding *_TRUE_DELETED domain event to Kafka so downstream projections (metadata-read-service) and search indexing (search-service) can drop the record entirely. Intended to be invoked periodically as a maintenance/cleanup job rather than as part of normal user-facing traffic."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Purge completed successfully (entities with DELETED status, if any, were permanently removed)"),
            @ApiResponse(responseCode = "500", description = "Unexpected internal error during purge")
    })
    @PostMapping("/purge")
    public ResponseEntity<Void> purge() {
        purgeService.purge();
        return ResponseEntity.ok().build();
    }
}
