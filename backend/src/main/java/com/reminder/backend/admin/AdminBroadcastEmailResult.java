package com.reminder.backend.admin;

public class AdminBroadcastEmailResult {
    private final int totalRecipients;
    private final int sentCount;
    private final int failedCount;

    public AdminBroadcastEmailResult(int totalRecipients, int sentCount, int failedCount) {
        this.totalRecipients = totalRecipients;
        this.sentCount = sentCount;
        this.failedCount = failedCount;
    }

    public int getTotalRecipients() {
        return totalRecipients;
    }

    public int getSentCount() {
        return sentCount;
    }

    public int getFailedCount() {
        return failedCount;
    }
}
