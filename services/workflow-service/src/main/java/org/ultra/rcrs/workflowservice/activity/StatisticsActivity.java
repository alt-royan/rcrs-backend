package org.ultra.rcrs.workflowservice.activity;

public interface StatisticsActivity {

    void recordEvent(String eventType, String entityId);

    String getStatistics(String entityType, String entityId);
}
