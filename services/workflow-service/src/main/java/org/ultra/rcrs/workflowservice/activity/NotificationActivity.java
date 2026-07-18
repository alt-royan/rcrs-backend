package org.ultra.rcrs.workflowservice.activity;

public interface NotificationActivity {

    void sendNotification(String userId, String title, String message);

    void sendBulkNotification(String[] userIds, String title, String message);
}
