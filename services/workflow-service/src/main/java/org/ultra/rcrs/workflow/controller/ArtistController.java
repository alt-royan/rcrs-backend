package org.ultra.rcrs.workflow.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.ultra.rcrs.workflow.dto.request.ArtistUploadRequest;
import org.ultra.rcrs.workflow.dto.request.ChangeAvailabilityStatusRequest;
import org.ultra.rcrs.workflow.dto.response.CreateResponse;
import org.ultra.rcrs.workflow.handler.WorkflowHandler;

@Tag(
        name = "Artist Workflows",
        description = "Triggers Temporal workflows (ArtistRegistrationWorkflow, ArtistChangeAvailabilityStatusWorkflow) " +
                "that orchestrate artist creation and lifecycle transitions across metadata-write-service and " +
                "search-service. Endpoints start the workflow via WorkflowClient and block on its result " +
                "(CompletableFuture#join), so the response reflects the finished orchestration rather than a fire-and-forget acknowledgement."
)
@RestController
@RequiredArgsConstructor
@RequestMapping("/workflow/artists")
public class ArtistController {

    private final WorkflowHandler handler;

    @Operation(
            summary = "Register a new artist",
            description = "Starts a Temporal workflow that creates the artist in metadata-write-service and indexes it " +
                    "in search-service. The call blocks until the orchestrated workflow completes and returns the " +
                    "identifier of the newly created artist."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "The workflow completed and the artist was created"),
            @ApiResponse(responseCode = "400", description = "The request body failed validation, or a step in the workflow reported a bad request"),
            @ApiResponse(responseCode = "401", description = "The caller is not authenticated"),
            @ApiResponse(responseCode = "403", description = "The caller is authenticated but not authorized to perform this action"),
            @ApiResponse(responseCode = "409", description = "A step in the workflow reported a conflicting state (e.g. duplicate artist)"),
            @ApiResponse(responseCode = "503", description = "A downstream service invoked by the workflow, or the Temporal service itself, is unavailable"),
            @ApiResponse(responseCode = "500", description = "The workflow failed for an unexpected reason")
    })
    @PostMapping
    public ResponseEntity<CreateResponse> registerArtist(@Valid @RequestBody ArtistUploadRequest request, @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.status(HttpStatus.CREATED).body(handler.startRegisterArtistWorkflow(request, jwt));
    }

    @Operation(
            summary = "Change an artist's availability status",
            description = "Starts a Temporal workflow that transitions the artist's availability status (e.g. hide, " +
                    "activate) and propagates the change to the downstream services that hold projections of the " +
                    "artist (metadata-write-service, search-service). The call blocks until the orchestrated workflow " +
                    "finishes before returning a response."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "The workflow completed and the artist's availability status was changed"),
            @ApiResponse(responseCode = "400", description = "The request body failed validation, or a step in the workflow reported a bad request"),
            @ApiResponse(responseCode = "401", description = "The caller is not authenticated"),
            @ApiResponse(responseCode = "403", description = "The caller is authenticated but not authorized to perform this action"),
            @ApiResponse(responseCode = "404", description = "The artist could not be found by one of the orchestrated steps"),
            @ApiResponse(responseCode = "409", description = "A step in the workflow reported a conflicting state"),
            @ApiResponse(responseCode = "503", description = "A downstream service invoked by the workflow, or the Temporal service itself, is unavailable"),
            @ApiResponse(responseCode = "500", description = "The workflow failed for an unexpected reason")
    })
    @PutMapping("/{id}")
    public ResponseEntity<Void> changeArtistAvailability(
            @Valid @RequestBody ChangeAvailabilityStatusRequest request,
            @Parameter(description = "Short (Base62) identifier of the artist whose availability status is being changed", required = true)
            @PathVariable("id") String id,
            @AuthenticationPrincipal Jwt jwt) {
        handler.startArtistChangeAvailabilityStatusWorkflow(request.status(), id, jwt);
        return ResponseEntity.ok().build();
    }

}
