package org.ultra.rcrs.workflowservice.client;

public interface NotificationClient {

    void sendNotification(String userId, String title, String message);

    void sendBulkNotification(String[] userIds, String title, String message);
}
