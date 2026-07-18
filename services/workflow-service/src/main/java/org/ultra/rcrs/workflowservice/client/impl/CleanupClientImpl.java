package org.ultra.rcrs.workflowservice.client.impl;

import org.springframework.stereotype.Component;
import org.ultra.rcrs.workflowservice.client.CleanupClient;

@Component
public class CleanupClientImpl implements CleanupClient {

    @Override
    public void cleanupOrphaned(String entityType) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void cleanupExpired(String entityType, int olderThanDays) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
