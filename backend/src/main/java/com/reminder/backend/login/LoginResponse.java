package com.reminder.backend.login;

public class LoginResponse {
    private String token;
    private Long userId;
    private String email;
    private String username;
    private Long empId;
    private String accessLevel;

    public LoginResponse(String token, Long userId, String email, String username, Long empId, String accessLevel) {
        this.token = token;
        this.userId = userId;
        this.email = email;
        this.username = username;
        this.empId = empId;
        this.accessLevel = accessLevel;
    }

    public String getToken() { return token; }
    public Long getUserId() { return userId; }
    public String getEmail() { return email; }
    public String getUsername() { return username; }
    public Long getEmpId() { return empId; }
    public String getAccessLevel() { return accessLevel; }
}
