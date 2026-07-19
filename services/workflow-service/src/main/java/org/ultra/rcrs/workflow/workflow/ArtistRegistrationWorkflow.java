package org.ultra.rcrs.workflow.workflow;

import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;
import org.ultra.rcrs.workflow.dto.CreateResponse;
import org.ultra.rcrs.workflow.dto.RegisterArtistRequest;

@WorkflowInterface
public interface ArtistRegistrationWorkflow {

    @WorkflowMethod
    CreateResponse registerArtist(RegisterArtistRequest request);
}
