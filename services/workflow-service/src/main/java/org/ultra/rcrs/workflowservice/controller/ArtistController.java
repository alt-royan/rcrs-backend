package org.ultra.rcrs.workflowservice.controller;

import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.ultra.rcrs.workflowservice.config.temporal.TemporalProperties;
import org.ultra.rcrs.workflowservice.dto.ArtistResponse;
import org.ultra.rcrs.workflowservice.dto.CreateArtistRequest;
import org.ultra.rcrs.workflowservice.workflow.ArtistCreationWorkflow;

@RestController
@RequestMapping("/api/workflows")
public class ArtistController {

    private final WorkflowClient workflowClient;
    private final TemporalProperties temporalProperties;

    public ArtistController(WorkflowClient workflowClient, TemporalProperties temporalProperties) {
        this.workflowClient = workflowClient;
        this.temporalProperties = temporalProperties;
    }

    @PostMapping("/artists")
    public ResponseEntity<ArtistResponse> createArtist(@Valid @RequestBody CreateArtistRequest request) {
        ArtistCreationWorkflow workflow = workflowClient.newTypedWorkflowStub(
                ArtistCreationWorkflow.class,
                WorkflowOptions.newBuilder()
                        .setTaskQueue(temporalProperties.taskQueue())
                        .build()
        );

        ArtistResponse response = workflow.createArtist(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
