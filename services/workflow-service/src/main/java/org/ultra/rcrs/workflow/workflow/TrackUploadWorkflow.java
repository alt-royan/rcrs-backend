package org.ultra.rcrs.workflow.workflow;

import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;
import org.ultra.rcrs.workflow.dto.request.TrackUploadRequest;
import org.ultra.rcrs.workflow.dto.response.CreateResponse;

@WorkflowInterface
public interface TrackUploadWorkflow {

    @WorkflowMethod
    CreateResponse uploadTrack(TrackUploadRequest request, String albumId);
}
