package com.gringosexy.model;

import com.gringosexy.enums.DeviceType;
import com.gringosexy.enums.Role;
import com.gringosexy.enums.UserStatus;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "users")
public class User {

    @Id
    private String id;

    private String fullName;

    @Indexed(unique = true)
    private String username;

    @Indexed(unique = true)
    private String email;

    private String password;

    @Indexed
    private Role role = Role.USER;

    @Indexed
    private UserStatus status = UserStatus.PENDING;

    @Indexed
    private DeviceType deviceType = DeviceType.IPHONE;

    private String requestedModule = "ALL"; // MODIFICATIONS, SENSITIVITIES, OPTIMIZATIONS, ALL

    private boolean emailVerified = false;

    private UserPermissions permissions = new UserPermissions();

    @CreatedDate
    @Indexed
    private Instant createdAt = Instant.now();

    @LastModifiedDate
    private Instant updatedAt = Instant.now();

    public User() {
    }

    public User(String fullName, String username, String email, String password, DeviceType deviceType) {
        this.fullName = fullName;
        this.username = username;
        this.email = email;
        this.password = password;
        this.deviceType = deviceType;
        this.role = Role.USER;
        this.status = UserStatus.PENDING;
        this.emailVerified = false;
        this.permissions = new UserPermissions();
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public UserStatus getStatus() {
        return status;
    }

    public void setStatus(UserStatus status) {
        this.status = status;
    }

    public DeviceType getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(DeviceType deviceType) {
        this.deviceType = deviceType;
    }

    public boolean isEmailVerified() {
        return emailVerified;
    }

    public void setEmailVerified(boolean emailVerified) {
        this.emailVerified = emailVerified;
    }

    public UserPermissions getPermissions() {
        return permissions != null ? permissions : new UserPermissions();
    }

    public void setPermissions(UserPermissions permissions) {
        this.permissions = permissions;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getRequestedModule() {
        return requestedModule != null ? requestedModule : "ALL";
    }

    public void setRequestedModule(String requestedModule) {
        this.requestedModule = requestedModule;
    }

    public boolean isActive() {
        return this.status == UserStatus.ACTIVE;
    }

    public boolean isAdmin() {
        return this.role == Role.ADMIN || this.role == Role.SUPER_ADMIN;
    }

    public boolean isSuperAdmin() {
        return this.role == Role.SUPER_ADMIN;
    }
}
