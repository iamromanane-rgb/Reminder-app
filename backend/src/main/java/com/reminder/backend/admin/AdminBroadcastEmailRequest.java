package com.reminder.backend.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class AdminBroadcastEmailRequest {

    @NotBlank
    @Size(max = 200)
    private String subject;

    @NotBlank
    @Size(max = 5000)
    private String message;

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
