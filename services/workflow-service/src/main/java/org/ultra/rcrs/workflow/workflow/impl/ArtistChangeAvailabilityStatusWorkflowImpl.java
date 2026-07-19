package org.ultra.rcrs.workflow.workflow.impl;

import io.temporal.spring.boot.WorkflowImpl;
import org.springframework.http.ResponseEntity;
import org.ultra.rcrs.enums.EntityStatus;
import org.ultra.rcrs.workflow.activity.ActivityFactory;
import org.ultra.rcrs.workflow.workflow.ArtistChangeAvailabilityStatusWorkflow;

import static org.ultra.rcrs.workflow.config.TemporalConfig.WORKFLOW_TASK_QUEUE;

@WorkflowImpl(taskQueues = WORKFLOW_TASK_QUEUE)
public class ArtistChangeAvailabilityStatusWorkflowImpl implements ArtistChangeAvailabilityStatusWorkflow {

    private final ActivityFactory activityFactory;

    public ArtistChangeAvailabilityStatusWorkflowImpl() {
        this.activityFactory = ActivityFactory.getInstance();
    }

    @Override
    public ResponseEntity<Void> changeAvailabilityStatus(EntityStatus status, String id) {
        return switch (status) {
            case ACTIVE -> activityFactory.artistActivity().activeArtist(id);
            case HIDDEN -> activityFactory.artistActivity().hideArtist(id);
            case DELETED -> activityFactory.artistActivity().markArtistDeleted(id);
        };
    }
}
