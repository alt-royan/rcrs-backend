package org.ultra.rcrs.mediaservice.temporal.workflow;

import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

@WorkflowInterface
public interface AudioTranscodingWorkflow {

    @WorkflowMethod
    void transcode(String uid, String trackId);
}
