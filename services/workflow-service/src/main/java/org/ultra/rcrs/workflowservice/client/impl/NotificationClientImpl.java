package org.ultra.rcrs.workflowservice.client.impl;

import org.springframework.stereotype.Component;
import org.ultra.rcrs.workflowservice.client.NotificationClient;

@Component
public class NotificationClientImpl implements NotificationClient {

    @Override
    public void sendNotification(String userId, String title, String message) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void sendBulkNotification(String[] userIds, String title, String message) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
