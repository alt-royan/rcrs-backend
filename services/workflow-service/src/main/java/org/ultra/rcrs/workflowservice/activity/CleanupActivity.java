package org.ultra.rcrs.workflowservice.activity;

public interface CleanupActivity {

    void cleanupOrphaned(String entityType);

    void cleanupExpired(String entityType, int olderThanDays);
}
