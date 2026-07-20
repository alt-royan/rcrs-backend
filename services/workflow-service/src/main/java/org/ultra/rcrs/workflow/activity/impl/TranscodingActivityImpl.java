package org.ultra.rcrs.workflow.activity.impl;

import io.temporal.spring.boot.ActivityImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.ultra.rcrs.workflow.activity.TranscodingActivity;
import org.ultra.rcrs.workflow.kafka.WorkflowEventProducer;

@Component
@ActivityImpl
@RequiredArgsConstructor
public class TranscodingActivityImpl implements TranscodingActivity {

    private final WorkflowEventProducer eventProducer;

    @Override
    public void trackTranscoding(String uid, String trackId) {
        eventProducer.trackTranscoding(uid, trackId);
    }
}
