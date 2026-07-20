package org.ultra.rcrs.workflow.workflow.impl;

import io.temporal.spring.boot.WorkflowImpl;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.ultra.rcrs.enums.EntityStatus;
import org.ultra.rcrs.workflow.workflow.BaseWorkflow;
import org.ultra.rcrs.workflow.workflow.TrackChangeAvailabilityStatusWorkflow;

import static org.ultra.rcrs.workflow.config.TemporalConfig.WORKFLOW_TASK_QUEUE;

@Component
@WorkflowImpl(taskQueues = WORKFLOW_TASK_QUEUE)
public class TrackChangeAvailabilityStatusWorkflowImpl extends BaseWorkflow implements TrackChangeAvailabilityStatusWorkflow {

    @Override
    public void changeAvailabilityStatus(EntityStatus status, String id) {
         switch (status) {
            case ACTIVE -> activityFactory.artistActivity().activeArtist(id);
            case HIDDEN -> activityFactory.artistActivity().hideArtist(id);
            case DELETED -> activityFactory.artistActivity().markArtistDeleted(id);
        };
    }
}
