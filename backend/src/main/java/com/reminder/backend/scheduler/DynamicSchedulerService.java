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

    private ScheduledFuture<?> scheduledFuture;// current scheduled task
    private volatile String currentCron; // current cron expression for reference - can be accessed by multiple threads

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
            scheduledFuture.cancel(false); // stop current scheduler
        }

        scheduledFuture = taskScheduler.schedule(
                notificationScheduler::checkAndSendReminders, //the method to run
                new CronTrigger(cronExpression) //when to run it
        ); //starts fresh timer, that is, a new scheduler
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
