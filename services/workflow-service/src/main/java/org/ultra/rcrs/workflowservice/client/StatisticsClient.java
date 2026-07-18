package org.ultra.rcrs.workflowservice.client;

public interface StatisticsClient {

    void recordEvent(String eventType, String entityId);

    String getStatistics(String entityType, String entityId);
}
