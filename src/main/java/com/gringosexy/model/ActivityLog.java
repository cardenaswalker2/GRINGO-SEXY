package com.gringosexy.model;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "activity_logs")
public class ActivityLog {

    @Id
    private String id;

    @Indexed
    private String userId;

    private String username;

    @Indexed
    private String action;

    private String description;

    private String ipAddress;

    @CreatedDate
    @Indexed
    private Instant timestamp = Instant.now();

    public ActivityLog() {
    }

    public ActivityLog(String userId, String username, String action, String description, String ipAddress) {
        this.userId = userId;
        this.username = username;
        this.action = action;
        this.description = description;
        this.ipAddress = ipAddress;
        this.timestamp = Instant.now();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }
}
