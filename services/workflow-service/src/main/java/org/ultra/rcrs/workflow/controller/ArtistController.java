package org.ultra.rcrs.workflow.controller;

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

@RestController
@RequiredArgsConstructor
@RequestMapping("/artists")
public class ArtistController {

    private final WorkflowHandler handler;

    @PostMapping
    public ResponseEntity<CreateResponse> registerArtist(@Valid @RequestBody ArtistUploadRequest request, @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.status(HttpStatus.CREATED).body(handler.startRegisterArtistWorkflow(request, jwt));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> changeArtistAvailability(@Valid @RequestBody ChangeAvailabilityStatusRequest request, @PathVariable("id") String id, @AuthenticationPrincipal Jwt jwt) {
        handler.startArtistChangeAvailabilityStatusWorkflow(request.status(), id, jwt);
        return ResponseEntity.ok().build();
    }

}
