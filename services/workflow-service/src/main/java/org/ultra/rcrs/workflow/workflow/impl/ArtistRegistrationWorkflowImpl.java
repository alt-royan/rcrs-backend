package org.ultra.rcrs.workflow.workflow.impl;

import io.temporal.spring.boot.WorkflowImpl;
import org.ultra.rcrs.workflow.activity.ActivityFactory;
import org.ultra.rcrs.workflow.dto.CreateResponse;
import org.ultra.rcrs.workflow.dto.RegisterArtistRequest;
import org.ultra.rcrs.workflow.workflow.ArtistRegistrationWorkflow;

import static org.ultra.rcrs.workflow.config.TemporalConfig.WORKFLOW_TASK_QUEUE;

@WorkflowImpl(taskQueues = WORKFLOW_TASK_QUEUE)
public class ArtistRegistrationWorkflowImpl implements ArtistRegistrationWorkflow {

    private final ActivityFactory activityFactory;

    public ArtistRegistrationWorkflowImpl() {
        this.activityFactory = ActivityFactory.getInstance();
    }

    @Override
    public CreateResponse registerArtist(RegisterArtistRequest request) {
        return activityFactory.metadataActivity().createArtist(request);
    }
}
