package org.ultra.rcrs.mediaservice.temporal.activity;

import io.temporal.activity.ActivityOptions;
import io.temporal.common.RetryOptions;
import io.temporal.workflow.Workflow;
import org.ultra.rcrs.mediaservice.temporal.config.TemporalProperties;

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

    public ValidateActivity validateActivity() {
        return Workflow.newActivityStub(ValidateActivity.class, buildOptions("validate"));
    }

    public ThumbnailActivity thumbnailActivity() {
        return Workflow.newActivityStub(ThumbnailActivity.class, buildOptions("thumbnail"));
    }

    public ProbeAudioMetadataActivity probeAudioMetadataActivity() {
        return Workflow.newActivityStub(ProbeAudioMetadataActivity.class, buildOptions("probeAudioMetadata"));
    }

    public S3Activity s3Activity() {
        return Workflow.newActivityStub(S3Activity.class, buildOptions("s3Activity"));
    }

    public DbActivity dbActivity() {
        return Workflow.newActivityStub(DbActivity.class, buildOptions("dbActivity"));
    }

    public TranscodeAudioActivity transcodeAudioActivity() {
        return Workflow.newActivityStub(TranscodeAudioActivity.class, buildOptions("transcodeAudioActivity"));
    }

    public TranscodingStatusActivity transcodingStatusActivity() {
        return Workflow.newActivityStub(TranscodingStatusActivity.class, buildOptions("transcodingStatus"));
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
