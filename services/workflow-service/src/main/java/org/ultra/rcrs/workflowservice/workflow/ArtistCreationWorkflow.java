package org.ultra.rcrs.workflowservice.workflow;

import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;
import org.ultra.rcrs.workflowservice.dto.ArtistResponse;
import org.ultra.rcrs.workflowservice.dto.CreateArtistRequest;

@WorkflowInterface
public interface ArtistCreationWorkflow {

    @WorkflowMethod
    ArtistResponse createArtist(CreateArtistRequest request);
}
