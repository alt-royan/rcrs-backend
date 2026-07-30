package org.ultra.rcrs.workflow.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.ultra.rcrs.security.CallerId;
import org.ultra.rcrs.workflow.dto.request.ChangeAvailabilityStatusRequest;
import org.ultra.rcrs.workflow.handler.WorkflowHandler;

@Tag(
        name = "Track Workflows",
        description = "Triggers the Temporal TrackChangeAvailabilityStatusWorkflow that orchestrates track lifecycle " +
                "transitions across metadata-write-service and search-service. The controller starts the workflow and " +
                "blocks on its result (WorkflowClient#execute(...).join()), so the HTTP response is only returned once " +
                "the orchestrated steps have completed, not merely accepted for later processing."
)
@RestController
@RequiredArgsConstructor
@RequestMapping("/workflow/tracks")
public class TrackController {

    private final WorkflowHandler handler;

    @Operation(
            summary = "Change a track's availability status",
            description = "Starts a Temporal workflow that transitions the track's availability status (e.g. hide, " +
                    "activate) and propagates the change to the downstream services that hold projections of the " +
                    "track (metadata-write-service, search-service). The call blocks until the orchestrated workflow " +
                    "finishes before returning a response."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "The workflow completed and the track's availability status was changed"),
            @ApiResponse(responseCode = "400", description = "The request body failed validation, or a step in the workflow reported a bad request"),
            @ApiResponse(responseCode = "401", description = "The caller is not authenticated"),
            @ApiResponse(responseCode = "403", description = "The caller is authenticated but not authorized to perform this action"),
            @ApiResponse(responseCode = "404", description = "The track could not be found by one of the orchestrated steps"),
            @ApiResponse(responseCode = "409", description = "A step in the workflow reported a conflicting state"),
            @ApiResponse(responseCode = "503", description = "A downstream service invoked by the workflow, or the Temporal service itself, is unavailable"),
            @ApiResponse(responseCode = "500", description = "The workflow failed for an unexpected reason")
    })
    @PutMapping("/{id}")
    public ResponseEntity<Void> changeTrackAvailability(
            @Valid @RequestBody ChangeAvailabilityStatusRequest request,
            @Parameter(description = "Short (Base62) identifier of the track whose availability status is being changed", required = true)
            @PathVariable("id") String id,
            @AuthenticationPrincipal Jwt jwt) {
        handler.startTrackChangeAvailabilityStatusWorkflow(request.status(), id, CallerId.require(jwt));
        return ResponseEntity.ok().build();
    }

}
