package org.ultra.rcrs.workflow.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.ultra.rcrs.workflow.dto.request.ChangeAvailabilityStatusRequest;
import org.ultra.rcrs.workflow.handler.WorkflowHandler;

@RestController
@RequiredArgsConstructor
@RequestMapping("/workflow/tracks")
public class TrackController {

    private final WorkflowHandler handler;

    @PutMapping("/{id}")
    public ResponseEntity<Void> changeTrackAvailability(@Valid @RequestBody ChangeAvailabilityStatusRequest request, @PathVariable("id") String id, @AuthenticationPrincipal Jwt jwt) {
        handler.startTrackChangeAvailabilityStatusWorkflow(request.status(), id, jwt);
        return ResponseEntity.ok().build();
    }

}
