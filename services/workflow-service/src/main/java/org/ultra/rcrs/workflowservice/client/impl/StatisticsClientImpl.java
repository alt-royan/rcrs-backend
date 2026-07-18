package org.ultra.rcrs.workflowservice.client.impl;

import org.springframework.stereotype.Component;
import org.ultra.rcrs.workflowservice.client.StatisticsClient;

@Component
public class StatisticsClientImpl implements StatisticsClient {

    @Override
    public void recordEvent(String eventType, String entityId) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public String getStatistics(String entityType, String entityId) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
