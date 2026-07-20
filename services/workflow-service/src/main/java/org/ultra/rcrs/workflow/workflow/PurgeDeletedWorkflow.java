package org.ultra.rcrs.workflow.workflow;

import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

@WorkflowInterface
public interface PurgeDeletedWorkflow {

    @WorkflowMethod
    void purge();
}
