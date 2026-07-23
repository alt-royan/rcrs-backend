package org.ultra.rcrs.mediaservice.temporal.workflow;

import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;
import org.ultra.rcrs.mediaservice.dto.TranscodingWorkflowInput;

@WorkflowInterface
public interface AudioTranscodingWorkflow {

    @WorkflowMethod
    void transcode(TranscodingWorkflowInput input);
}
