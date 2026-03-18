package com.reminder.backend.scheduler;

public class SchedulerStatus {
    private String cron;
    private boolean scheduled;

    public SchedulerStatus(String cron, boolean scheduled) {
        this.cron = cron;
        this.scheduled = scheduled;
    }

    public String getCron() { return cron; }
    public boolean isScheduled() { return scheduled; }
}
