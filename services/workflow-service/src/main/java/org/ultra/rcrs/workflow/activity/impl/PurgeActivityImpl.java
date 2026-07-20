package org.ultra.rcrs.workflow.activity.impl;

import io.temporal.spring.boot.ActivityImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.ultra.rcrs.workflow.activity.PurgeActivity;
import org.ultra.rcrs.workflow.client.MetadataClient;

@Component
@ActivityImpl
@RequiredArgsConstructor
public class PurgeActivityImpl implements PurgeActivity {

    private final MetadataClient metadataClient;

    @Override
    public void purge() {
        var res = metadataClient.purge();
        if (!res.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("Purge request failed with status: " + res.getStatusCode());
        }
    }
}
