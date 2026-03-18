package com.reminder.backend.scheduler;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.CronExpression;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Service;

import java.util.concurrent.ScheduledFuture;

@Service //loads the class automatically
public class DynamicSchedulerService {

    private final TaskScheduler taskScheduler;
    private final NotificationScheduler notificationScheduler;
    private final String defaultCron;

    private ScheduledFuture<?> scheduledFuture; //holds current scheduler
    private volatile String currentCron; //where error came

    public DynamicSchedulerService(
            TaskScheduler taskScheduler,
            NotificationScheduler notificationScheduler,
            @Value("${scheduler.notifications.cron}") String defaultCron
    ) {
        this.taskScheduler = taskScheduler;
        this.notificationScheduler = notificationScheduler;
        this.defaultCron = defaultCron;
    }

    @PostConstruct
    public void initialize() {
        updateCron(defaultCron);
    }

    public synchronized void updateCron(String cronExpression) { //prevents 2 admins updating at the same time
        CronExpression.parse(cronExpression); //validity chk

        if (scheduledFuture != null) {
            scheduledFuture.cancel(false);
        }

        scheduledFuture = taskScheduler.schedule(
                notificationScheduler::checkAndSendReminders,
                new CronTrigger(cronExpression)
        ); //starts fresh timer
        currentCron = cronExpression;
    }

    public void runNow() {
        notificationScheduler.checkAndSendReminders();
    }

    public String getCurrentCron() {
        return currentCron;
    }

    public boolean isScheduled() {
        return scheduledFuture != null && !scheduledFuture.isCancelled();
    }
}
