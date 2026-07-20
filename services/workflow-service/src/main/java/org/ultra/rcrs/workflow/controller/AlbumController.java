package org.ultra.rcrs.workflow.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.ultra.rcrs.workflow.dto.request.AlbumUploadRequest;
import org.ultra.rcrs.workflow.dto.request.ChangeAvailabilityStatusRequest;
import org.ultra.rcrs.workflow.dto.response.CreateResponse;
import org.ultra.rcrs.workflow.handler.WorkflowHandler;

@RestController
@RequiredArgsConstructor
@RequestMapping("/albums")
public class AlbumController {

    private final WorkflowHandler handler;

    @PostMapping
    public ResponseEntity<CreateResponse> uploadAlbum(@Valid @RequestBody AlbumUploadRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(handler.startAlbumUploadWorkflow(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> changeAlbumAvailability(@Valid @RequestBody ChangeAvailabilityStatusRequest request, @PathVariable("id") String id) {
        handler.startAlbumChangeAvailabilityStatusWorkflow(request.status(), id);
        return ResponseEntity.ok().build();
    }

}
