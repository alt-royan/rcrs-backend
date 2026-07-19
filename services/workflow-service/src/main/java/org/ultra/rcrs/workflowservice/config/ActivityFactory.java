package org.ultra.rcrs.workflowservice.config;

import io.temporal.activity.ActivityOptions;
import io.temporal.common.RetryOptions;
import io.temporal.workflow.Workflow;
import org.ultra.rcrs.workflowservice.activity.AudioActivity;
import org.ultra.rcrs.workflowservice.activity.MetadataActivity;
import org.ultra.rcrs.workflowservice.activity.SearchActivity;

public class ActivityFactory {

    private final TemporalProperties properties;

    public ActivityFactory(TemporalProperties properties) {
        this.properties = properties;
    }

    public MetadataActivity metadataActivity() {
        return Workflow.newActivityStub(MetadataActivity.class, buildOptions("metadata"));
    }

    public AudioActivity audioActivity() {
        return Workflow.newActivityStub(AudioActivity.class, buildOptions("audio"));
    }

    public SearchActivity searchActivity() {
        return Workflow.newActivityStub(SearchActivity.class, buildOptions("search"));
    }

    private ActivityOptions buildOptions(String serviceName) {
        TemporalProperties.ActivityConfig config = properties.activities().get(serviceName);
        return ActivityOptions.newBuilder()
                .setStartToCloseTimeout(config.startToCloseTimeout())
                .setRetryOptions(buildRetryOptions(config.retry()))
                .build();
    }

    private RetryOptions buildRetryOptions(TemporalProperties.RetryConfig config) {
        return RetryOptions.newBuilder()
                .setInitialInterval(config.initialInterval())
                .setBackoffCoefficient(config.backoffCoefficient())
                .setMaximumInterval(config.maximumInterval())
                .setMaximumAttempts(config.maximumAttempts())
                .build();
    }
}
