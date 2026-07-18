package org.ultra.rcrs.workflowservice.config.temporal;

public abstract class BaseWorkflow {

    protected final ActivityFactory activityFactory;

    protected BaseWorkflow(ActivityFactory activityFactory) {
        this.activityFactory = activityFactory;
    }
}
