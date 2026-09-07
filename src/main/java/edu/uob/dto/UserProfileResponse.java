package edu.uob.dto;

import java.time.LocalDateTime;

public class UserProfileResponse {
    private String username;
    private String role;
    private LocalDateTime createdAt;

    public UserProfileResponse() {}

    public UserProfileResponse(String username, String role, LocalDateTime createdAt) {
        this.username = username;
        this.role = role;
        this.createdAt = createdAt;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
