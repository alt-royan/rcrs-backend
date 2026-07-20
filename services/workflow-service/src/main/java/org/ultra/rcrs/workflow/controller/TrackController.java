package org.ultra.rcrs.workflow.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.ultra.rcrs.workflow.dto.request.ChangeAvailabilityStatusRequest;
import org.ultra.rcrs.workflow.handler.WorkflowHandler;

@RestController
@RequiredArgsConstructor
@RequestMapping("/tracks")
public class TrackController {

    private final WorkflowHandler handler;

    @PutMapping("/{id}")
    public ResponseEntity<Void> changeTrackAvailability(@Valid @RequestBody ChangeAvailabilityStatusRequest request, @PathVariable("id") String id) {
        handler.startTrackChangeAvailabilityStatusWorkflow(request.status(), id);
        return ResponseEntity.ok().build();
    }

}
