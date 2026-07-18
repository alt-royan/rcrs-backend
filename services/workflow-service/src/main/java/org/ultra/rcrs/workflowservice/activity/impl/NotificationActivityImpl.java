package org.ultra.rcrs.workflowservice.activity.impl;

import org.springframework.stereotype.Component;
import org.ultra.rcrs.workflowservice.activity.NotificationActivity;
import org.ultra.rcrs.workflowservice.client.NotificationClient;

@Component
public class NotificationActivityImpl implements NotificationActivity {

    private final NotificationClient notificationClient;

    public NotificationActivityImpl(NotificationClient notificationClient) {
        this.notificationClient = notificationClient;
    }

    @Override
    public void sendNotification(String userId, String title, String message) {
        notificationClient.sendNotification(userId, title, message);
    }

    @Override
    public void sendBulkNotification(String[] userIds, String title, String message) {
        notificationClient.sendBulkNotification(userIds, title, message);
    }
}
