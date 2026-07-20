package org.ultra.rcrs.workflow.workflow.impl;

import io.temporal.spring.boot.WorkflowImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.ultra.rcrs.workflow.converter.UploadRequestConverter;
import org.ultra.rcrs.workflow.dto.request.ArtistUploadRequest;
import org.ultra.rcrs.workflow.dto.response.CreateResponse;
import org.ultra.rcrs.workflow.workflow.ArtistRegistrationWorkflow;
import org.ultra.rcrs.workflow.workflow.BaseWorkflow;

import static org.ultra.rcrs.workflow.config.TemporalConfig.WORKFLOW_TASK_QUEUE;

@Component
@WorkflowImpl(taskQueues = WORKFLOW_TASK_QUEUE)
@RequiredArgsConstructor
public class ArtistRegistrationWorkflowImpl extends BaseWorkflow implements ArtistRegistrationWorkflow {

    private final UploadRequestConverter converter;

    @Override
    public CreateResponse registerArtist(ArtistUploadRequest request) {
        return activityFactory.artistActivity().createArtist(converter.toArtistCreateModel(request));
    }
}
