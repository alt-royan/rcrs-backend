package org.ultra.rcrs.workflowservice.activity.impl;

import org.springframework.stereotype.Component;
import org.ultra.rcrs.workflowservice.activity.CleanupActivity;
import org.ultra.rcrs.workflowservice.client.CleanupClient;

@Component
public class CleanupActivityImpl implements CleanupActivity {

    private final CleanupClient cleanupClient;

    public CleanupActivityImpl(CleanupClient cleanupClient) {
        this.cleanupClient = cleanupClient;
    }

    @Override
    public void cleanupOrphaned(String entityType) {
        cleanupClient.cleanupOrphaned(entityType);
    }

    @Override
    public void cleanupExpired(String entityType, int olderThanDays) {
        cleanupClient.cleanupExpired(entityType, olderThanDays);
    }
}
