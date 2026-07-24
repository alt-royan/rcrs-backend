package org.ultra.rcrs.workflow.workflow;

import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;
import org.ultra.rcrs.workflow.dto.request.ArtistUploadRequest;
import org.ultra.rcrs.workflow.dto.response.CreateResponse;

@WorkflowInterface
public interface ArtistRegistrationWorkflow {

    @WorkflowMethod
    CreateResponse registerArtist(ArtistUploadRequest request);
}
