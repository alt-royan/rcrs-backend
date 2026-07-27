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
import org.ultra.rcrs.workflow.dto.request.AlbumUploadRequest;
import org.ultra.rcrs.workflow.dto.request.ChangeAvailabilityStatusRequest;
import org.ultra.rcrs.workflow.dto.response.CreateResponse;
import org.ultra.rcrs.workflow.handler.WorkflowHandler;

@Tag(
        name = "Album Workflows",
        description = "Triggers Temporal workflows (AlbumUploadWorkflow, AlbumChangeAvailabilityStatusWorkflow) that " +
                "orchestrate album creation (including its tracks and artist relationships) and lifecycle transitions " +
                "across metadata-write-service, media-service, and search-service. Endpoints start the workflow via " +
                "WorkflowClient and block on its result (CompletableFuture#join), so the response reflects the finished " +
                "orchestration rather than a fire-and-forget acknowledgement."
)
@RestController
@RequiredArgsConstructor
@RequestMapping("/workflow/albums")
public class AlbumController {

    private final WorkflowHandler handler;

    @Operation(
            summary = "Upload a new album",
            description = "Starts a Temporal workflow that creates the album (and its tracks and artist relationships) " +
                    "in metadata-write-service, coordinates any associated media processing, and indexes the album in " +
                    "search-service. The call blocks until the orchestrated workflow completes and returns the " +
                    "identifier of the newly created album."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "The workflow completed and the album was created"),
            @ApiResponse(responseCode = "400", description = "The request body failed validation, or a step in the workflow reported a bad request"),
            @ApiResponse(responseCode = "401", description = "The caller is not authenticated"),
            @ApiResponse(responseCode = "403", description = "The caller is authenticated but not authorized to perform this action"),
            @ApiResponse(responseCode = "409", description = "A step in the workflow reported a conflicting state (e.g. duplicate album)"),
            @ApiResponse(responseCode = "503", description = "A downstream service invoked by the workflow, or the Temporal service itself, is unavailable"),
            @ApiResponse(responseCode = "500", description = "The workflow failed for an unexpected reason")
    })
    @PostMapping
    public ResponseEntity<CreateResponse> uploadAlbum(@Valid @RequestBody AlbumUploadRequest request, @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.status(HttpStatus.CREATED).body(handler.startAlbumUploadWorkflow(request, jwt));
    }

    @Operation(
            summary = "Change an album's availability status",
            description = "Starts a Temporal workflow that transitions the album's availability status (e.g. hide, " +
                    "activate) and propagates the change to the downstream services that hold projections of the " +
                    "album (metadata-write-service, search-service). The call blocks until the orchestrated workflow " +
                    "finishes before returning a response."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "The workflow completed and the album's availability status was changed"),
            @ApiResponse(responseCode = "400", description = "The request body failed validation, or a step in the workflow reported a bad request"),
            @ApiResponse(responseCode = "401", description = "The caller is not authenticated"),
            @ApiResponse(responseCode = "403", description = "The caller is authenticated but not authorized to perform this action"),
            @ApiResponse(responseCode = "404", description = "The album could not be found by one of the orchestrated steps"),
            @ApiResponse(responseCode = "409", description = "A step in the workflow reported a conflicting state"),
            @ApiResponse(responseCode = "503", description = "A downstream service invoked by the workflow, or the Temporal service itself, is unavailable"),
            @ApiResponse(responseCode = "500", description = "The workflow failed for an unexpected reason")
    })
    @PutMapping("/{id}")
    public ResponseEntity<Void> changeAlbumAvailability(
            @Valid @RequestBody ChangeAvailabilityStatusRequest request,
            @Parameter(description = "Short (Base62) identifier of the album whose availability status is being changed", required = true)
            @PathVariable("id") String id,
            @AuthenticationPrincipal Jwt jwt) {
        handler.startAlbumChangeAvailabilityStatusWorkflow(request.status(), id, jwt);
        return ResponseEntity.ok().build();
    }

}
