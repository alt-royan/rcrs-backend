package org.ultra.rcrs.workflowservice.client;

public interface CleanupClient {

    void cleanupOrphaned(String entityType);

    void cleanupExpired(String entityType, int olderThanDays);
}
