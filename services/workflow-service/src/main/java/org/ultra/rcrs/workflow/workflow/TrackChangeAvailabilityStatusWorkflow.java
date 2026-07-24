package org.ultra.rcrs.workflow.workflow;

import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;
import org.ultra.rcrs.enums.EntityStatus;

@WorkflowInterface
public interface TrackChangeAvailabilityStatusWorkflow {

    @WorkflowMethod
    void changeAvailabilityStatus(EntityStatus status, String id);
}
