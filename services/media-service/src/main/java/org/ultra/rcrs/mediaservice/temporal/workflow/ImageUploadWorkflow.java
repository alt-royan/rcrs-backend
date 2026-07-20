package org.ultra.rcrs.mediaservice.temporal.workflow;

import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;
import org.ultra.rcrs.mediaservice.dto.ImageResponse;

import java.util.List;

@WorkflowInterface
public interface ImageUploadWorkflow {

    @WorkflowMethod
    ImageResponse uploadImage(String dataUrl, List<Integer> thumbnailSizes);
}
