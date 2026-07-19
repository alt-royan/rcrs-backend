package org.ultra.rcrs.workflow.activity;

import io.temporal.activity.ActivityOptions;
import io.temporal.common.RetryOptions;
import io.temporal.workflow.Workflow;
import org.ultra.rcrs.workflow.config.TemporalProperties;

public class ActivityFactory {

    private static ActivityFactory instance;

    private final TemporalProperties properties;

    private ActivityFactory(TemporalProperties properties) {
        this.properties = properties;
    }

    public static synchronized ActivityFactory getInstance(TemporalProperties properties) {
        if (instance == null) {
            instance = new ActivityFactory(properties);
        }
        return instance;
    }

    public static ActivityFactory getInstance() {
        if (instance == null) {
            throw new IllegalStateException("ActivityFactory not initialized. Call getInstance(TemporalProperties) first.");
        }
        return instance;
    }

    public MetadataActivity metadataActivity() {
        return Workflow.newActivityStub(MetadataActivity.class, buildOptions("metadata"));
    }

    public AudioActivity audioActivity() {
        return Workflow.newActivityStub(AudioActivity.class, buildOptions("audio"));
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
