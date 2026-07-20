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

    public ValidateImageActivity validateImageActivity() {
        return Workflow.newActivityStub(ValidateImageActivity.class, buildOptions("validateImage"));
    }

    public SaveOriginalImageActivity saveOriginalImageActivity() {
        return Workflow.newActivityStub(SaveOriginalImageActivity.class, buildOptions("saveOriginalImage"));
    }

    public SaveThumbnailActivity saveThumbnailActivity() {
        return Workflow.newActivityStub(SaveThumbnailActivity.class, buildOptions("saveThumbnail"));
    }

    public ValidateAudioActivity validateAudioActivity() {
        return Workflow.newActivityStub(ValidateAudioActivity.class, buildOptions("validateAudio"));
    }

    public ProbeAudioMetadataActivity probeAudioMetadataActivity() {
        return Workflow.newActivityStub(ProbeAudioMetadataActivity.class, buildOptions("probeAudioMetadata"));
    }

    public SaveOriginalAudioToS3Activity saveOriginalAudioToS3Activity() {
        return Workflow.newActivityStub(SaveOriginalAudioToS3Activity.class, buildOptions("saveOriginalAudioToS3"));
    }

    public SaveAudioRecordActivity saveAudioRecordActivity() {
        return Workflow.newActivityStub(SaveAudioRecordActivity.class, buildOptions("saveAudioRecord"));
    }

    public NormalizeAudioActivity normalizeAudioActivity() {
        return Workflow.newActivityStub(NormalizeAudioActivity.class, buildOptions("normalizeAudio"));
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
