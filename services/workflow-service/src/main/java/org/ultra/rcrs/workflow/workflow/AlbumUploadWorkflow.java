package org.ultra.rcrs.workflow.workflow;

import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;
import org.springframework.http.ResponseEntity;
import org.ultra.rcrs.workflow.client.model.AlbumUploadModel;
import org.ultra.rcrs.workflow.dto.request.AlbumUploadRequest;
import org.ultra.rcrs.workflow.dto.response.CreateResponse;

@WorkflowInterface
public interface AlbumUploadWorkflow {

    @WorkflowMethod
    CreateResponse uploadAlbum(AlbumUploadRequest request);
}
