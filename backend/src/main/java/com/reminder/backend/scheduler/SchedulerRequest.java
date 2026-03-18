package com.reminder.backend.scheduler;

import jakarta.validation.constraints.NotBlank;

public class SchedulerRequest { //for admin
    @NotBlank
    private String cron;

    public String getCron() { return cron; }
    public void setCron(String cron) { this.cron = cron; }
}
