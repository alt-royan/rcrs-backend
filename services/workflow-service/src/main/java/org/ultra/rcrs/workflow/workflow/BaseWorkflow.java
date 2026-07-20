package org.ultra.rcrs.workflow.workflow;


import org.ultra.rcrs.workflow.activity.ActivityFactory;

public abstract class BaseWorkflow {

    protected final ActivityFactory activityFactory;

    public BaseWorkflow() {
        this.activityFactory = ActivityFactory.getInstance();
    }
}
