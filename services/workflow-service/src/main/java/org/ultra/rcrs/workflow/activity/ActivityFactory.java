package org.ultra.rcrs.workflow.activity;

import io.temporal.activity.ActivityOptions;
import io.temporal.common.RetryOptions;
import io.temporal.workflow.Workflow;
import org.ultra.rcrs.workflow.config.TemporalProperties;
import org.ultra.rcrs.workflow.kafka.WorkflowEventProducer;

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

    public ArtistActivity artistActivity() {
        return Workflow.newActivityStub(ArtistActivity.class, buildOptions("artist"));
    }

    public AlbumActivity albumActivity() {
        return Workflow.newActivityStub(AlbumActivity.class, buildOptions("album"));
    }

    public TrackActivity trackActivity() {
        return Workflow.newActivityStub(TrackActivity.class, buildOptions("track"));
    }

    public AudioActivity audioActivity() {
        return Workflow.newActivityStub(AudioActivity.class, buildOptions("audio"));
    }

    public TranscodingActivity transcodingActivity() {
        return Workflow.newActivityStub(TranscodingActivity.class, buildOptions("transcoding"));
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
