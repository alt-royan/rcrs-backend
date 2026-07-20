package org.ultra.rcrs.workflow.workflow.impl;

import io.temporal.spring.boot.WorkflowImpl;
import lombok.extern.slf4j.Slf4j;
import org.ultra.rcrs.workflow.workflow.BaseWorkflow;
import org.ultra.rcrs.workflow.workflow.PurgeDeletedWorkflow;

import static org.ultra.rcrs.workflow.config.TemporalConfig.WORKFLOW_TASK_QUEUE;

@Slf4j
@WorkflowImpl(taskQueues = WORKFLOW_TASK_QUEUE)
public class PurgeDeletedWorkflowImpl extends BaseWorkflow implements PurgeDeletedWorkflow {

    @Override
    public void purge() {
        log.info("Starting monthly purge of soft-deleted entities");
        activityFactory.purgeActivity().purge();
        log.info("Monthly purge completed");
    }
}
