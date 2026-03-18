package com.reminder.backend.admin;

import jakarta.validation.constraints.Email;

public class AdminUserUpdateRequest {
    @Email
    private String email;
    private String username;
    private Long empId;
    private String accessLevel;

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public Long getEmpId() { return empId; }
    public void setEmpId(Long empId) { this.empId = empId; }

    public String getAccessLevel() { return accessLevel; }
    public void setAccessLevel(String accessLevel) { this.accessLevel = accessLevel; }
}
