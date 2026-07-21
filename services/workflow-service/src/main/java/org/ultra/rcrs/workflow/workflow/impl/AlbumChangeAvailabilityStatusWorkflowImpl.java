package org.ultra.rcrs.workflow.workflow.impl;

import io.temporal.spring.boot.WorkflowImpl;
import org.springframework.stereotype.Component;
import org.ultra.rcrs.enums.EntityStatus;
import org.ultra.rcrs.workflow.workflow.AlbumChangeAvailabilityStatusWorkflow;
import org.ultra.rcrs.workflow.workflow.BaseWorkflow;

import static org.ultra.rcrs.workflow.config.TemporalConfig.WORKFLOW_TASK_QUEUE;

@Component
@WorkflowImpl(taskQueues = WORKFLOW_TASK_QUEUE)
public class AlbumChangeAvailabilityStatusWorkflowImpl extends BaseWorkflow implements AlbumChangeAvailabilityStatusWorkflow {

    @Override
    public void changeAvailabilityStatus(EntityStatus status, String id) {
        switch (status) {
            case ACTIVE -> activityFactory.albumActivity().activeAlbum(id);
            case HIDDEN -> activityFactory.albumActivity().hideAlbum(id);
            case DELETED -> activityFactory.albumActivity().markAlbumDeleted(id);
        }
        ;
    }
}
