package org.ultra.rcrs.workflow.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.ultra.rcrs.workflow.dto.CreateResponse;
import org.ultra.rcrs.workflow.dto.RegisterArtistRequest;
import org.ultra.rcrs.workflow.handler.WorkflowHandler;

import java.net.URI;

@RestController
@RequiredArgsConstructor
public class ArtistController {

    private final WorkflowHandler handler;

    @PostMapping("/artists")
    public ResponseEntity<CreateResponse> registerArtist(@Valid @RequestBody RegisterArtistRequest request) {
        CreateResponse response = handler.startRegisterArtistWorkflow(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .location(URI.create("/artists/" + response.id()))
                .body(response);
    }

}
