package com.gringosexy.service;

import com.gringosexy.dto.PermissionUpdateRequest;
import com.gringosexy.dto.RegisterRequest;
import com.gringosexy.dto.UserUpdateRequest;
import com.gringosexy.enums.DeviceType;
import com.gringosexy.enums.Role;
import com.gringosexy.enums.UserStatus;
import com.gringosexy.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Map;
import java.util.Optional;

public interface UserService {

    User registerUser(RegisterRequest request, String ipAddress);

    User findById(String id);

    Optional<User> findByEmail(String email);

    Optional<User> findByUsername(String username);

    User updateProfile(String userId, UserUpdateRequest request, String ipAddress);

    User updateStatus(String userId, UserStatus status, String adminId, String ipAddress);

    User updateRole(String userId, Role role, String adminId, String ipAddress);

    User updateDevice(String userId, DeviceType deviceType, String adminId, String ipAddress);

    User updatePermissions(String userId, PermissionUpdateRequest request, String adminId, String ipAddress);

    void resetPassword(String token, String newPassword, String ipAddress);

    void initiatePasswordReset(String email, String ipAddress);

    Page<User> getUsers(String search, UserStatus status, Role role, DeviceType deviceType, Pageable pageable);

    Map<String, Object> getUserStatistics();
}
