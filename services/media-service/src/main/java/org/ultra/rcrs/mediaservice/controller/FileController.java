package org.ultra.rcrs.mediaservice.controller;

import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.ultra.rcrs.mediaservice.dto.ImageResponse;
import org.ultra.rcrs.mediaservice.dto.ImageUploadRequest;
import org.ultra.rcrs.mediaservice.temporal.workflow.ImageUploadWorkflow;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.ultra.rcrs.mediaservice.temporal.config.TemporalConfig.MEDIA_TASK_QUEUE;

@RestController
@RequiredArgsConstructor
@RequestMapping("/upload/files")
public class FileController {

    private final WorkflowClient workflowClient;

    @Value("${cdn.images.thumbnails}")
    private int[] thumbnailSizes;

    @PostMapping(value = "/image")
    public ResponseEntity<ImageResponse> uploadImage(@RequestBody ImageUploadRequest request) {
        ImageUploadWorkflow workflow = workflowClient.newWorkflowStub(
                ImageUploadWorkflow.class,
                WorkflowOptions.newBuilder()
                        .setTaskQueue(MEDIA_TASK_QUEUE)
                        .setWorkflowId(UUID.randomUUID().toString())
                        .build()
        );
        List<Integer> sizes = Arrays.stream(thumbnailSizes).boxed().toList();
        var future = WorkflowClient.execute(workflow::uploadImage, request.getImage(), sizes);
        return ResponseEntity.ok(future.join());
    }
}
