package org.ultra.rcrs.mediaservice.controller;

import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.ultra.rcrs.mediaservice.dto.*;
import org.ultra.rcrs.mediaservice.service.AudioService;
import org.ultra.rcrs.mediaservice.temporal.workflow.ImageUploadWorkflow;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.ultra.rcrs.mediaservice.temporal.config.TemporalConfig.MEDIA_TASK_QUEUE;

@RestController
@RequiredArgsConstructor
@RequestMapping("/upload")
public class UploadController {

    private final AudioService audioService;

    private final WorkflowClient workflowClient;

    @Value("${cdn.images.thumbnails}")
    private int[] thumbnailSizes;

    @PostMapping(value = "/audio/pre-sign", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<S3PresignUrlResponse> getPreSignUrl(@RequestBody @Validated PreloadFileRequest request) {
        return ResponseEntity.ok(audioService.getPreSignUrl(request));
    }

    @PostMapping(value = "/audio/get-status", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<FileStatusResponse>> getAudioStatus(@RequestParam(value = "uids") List<String> uids) {
        if (uids == null) {
            uids = new ArrayList<>();
        }
        return ResponseEntity.ok(audioService.getAudioStatus(uids));
    }

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
