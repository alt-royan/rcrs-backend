package org.ultra.rcrs.workflowservice.activity.impl;

import org.springframework.stereotype.Component;
import org.ultra.rcrs.workflowservice.activity.StatisticsActivity;
import org.ultra.rcrs.workflowservice.client.StatisticsClient;

@Component
public class StatisticsActivityImpl implements StatisticsActivity {

    private final StatisticsClient statisticsClient;

    public StatisticsActivityImpl(StatisticsClient statisticsClient) {
        this.statisticsClient = statisticsClient;
    }

    @Override
    public void recordEvent(String eventType, String entityId) {
        statisticsClient.recordEvent(eventType, entityId);
    }

    @Override
    public String getStatistics(String entityType, String entityId) {
        return statisticsClient.getStatistics(entityType, entityId);
    }
}
