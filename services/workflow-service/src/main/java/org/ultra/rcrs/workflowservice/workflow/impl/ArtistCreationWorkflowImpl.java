package org.ultra.rcrs.workflowservice.workflow.impl;

import org.ultra.rcrs.workflowservice.config.temporal.ActivityFactory;
import org.ultra.rcrs.workflowservice.config.temporal.BaseWorkflow;
import org.ultra.rcrs.workflowservice.dto.ArtistResponse;
import org.ultra.rcrs.workflowservice.dto.CreateArtistRequest;
import org.ultra.rcrs.workflowservice.workflow.ArtistCreationWorkflow;

public class ArtistCreationWorkflowImpl extends BaseWorkflow implements ArtistCreationWorkflow {

    public ArtistCreationWorkflowImpl(ActivityFactory activityFactory) {
        super(activityFactory);
    }

    @Override
    public ArtistResponse createArtist(CreateArtistRequest request) {
        return activityFactory.metadataActivity().createArtist(request);
    }
}
