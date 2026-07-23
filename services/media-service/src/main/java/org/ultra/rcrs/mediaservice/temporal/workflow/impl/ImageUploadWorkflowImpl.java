package org.ultra.rcrs.mediaservice.temporal.workflow.impl;

import io.temporal.spring.boot.WorkflowImpl;
import lombok.extern.slf4j.Slf4j;
import org.ultra.rcrs.mediaservice.dto.ImageResponse;
import org.ultra.rcrs.mediaservice.temporal.activity.ActivityFactory;
import org.ultra.rcrs.mediaservice.temporal.activity.model.ValidatedImage;
import org.ultra.rcrs.mediaservice.temporal.workflow.ImageUploadWorkflow;

import java.util.List;

import static org.ultra.rcrs.mediaservice.temporal.config.TemporalConfig.MEDIA_TASK_QUEUE;

@Slf4j
@WorkflowImpl(taskQueues = MEDIA_TASK_QUEUE)
public class ImageUploadWorkflowImpl implements ImageUploadWorkflow {

    @Override
    public ImageResponse uploadImage(String dataUrl, List<Integer> thumbnailSizes) {
        ActivityFactory activities = ActivityFactory.getInstance();

        log.info("Starting image upload workflow");

        ValidatedImage validated = activities.validateActivity().validateImage(dataUrl);
        String key = validated.key();
        byte[] imageData = validated.imageData();
        String contentType = validated.contentType();

        String uri = activities.s3Activity().putImage(key, imageData, contentType);

        for (int size : thumbnailSizes) {
            String thumbnailKey = key + String.format("/%sx%s", size, size);
            byte[] thumbnail = activities.thumbnailActivity().createThumbnail(imageData, validated.format(), size);
            activities.s3Activity().putImage(thumbnailKey, thumbnail, contentType);
        }

        log.info("Image upload workflow completed, uri={}", uri);
        return new ImageResponse(uri);
    }
}
