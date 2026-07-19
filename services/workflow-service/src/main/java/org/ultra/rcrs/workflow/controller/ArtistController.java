package org.ultra.rcrs.workflow.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.ultra.rcrs.workflow.dto.ChangeAvailabilityStatusRequest;
import org.ultra.rcrs.workflow.dto.CreateResponse;
import org.ultra.rcrs.workflow.dto.RegisterArtistRequest;
import org.ultra.rcrs.workflow.handler.WorkflowHandler;

@RestController
@RequiredArgsConstructor
public class ArtistController {

    private final WorkflowHandler handler;

    @PostMapping("/artists")
    public ResponseEntity<CreateResponse> registerArtist(@Valid @RequestBody RegisterArtistRequest request) {
        return handler.startRegisterArtistWorkflow(request);
    }

    @PutMapping("/artists/{id}")
    public ResponseEntity<Void> changeArtistAvailability(@Valid @RequestBody ChangeAvailabilityStatusRequest request, @PathVariable("id") String id) {
        return handler.startArtistChangeAvailabilityStatusWorkflow(request.status(), id);
    }

}
