package com.gringosexy.service;

import com.gringosexy.enums.Role;
import com.gringosexy.model.User;
import com.gringosexy.util.SecurityUtils;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service("permissionEvaluatorService")
public class PermissionService {

    private final SecurityUtils securityUtils;

    public PermissionService(SecurityUtils securityUtils) {
        this.securityUtils = securityUtils;
    }

    public boolean hasPermission(String categorySlug) {
        Optional<User> userOpt = securityUtils.getCurrentUser();
        if (userOpt.isEmpty()) {
            return false;
        }

        User user = userOpt.get();

        // Admin and Super Admin have full bypass access
        if (user.getRole() == Role.ADMIN || user.getRole() == Role.SUPER_ADMIN) {
            return true;
        }

        if (user.getPermissions() == null) {
            return false;
        }

        return user.getPermissions().hasPermissionFor(categorySlug);
    }
}
