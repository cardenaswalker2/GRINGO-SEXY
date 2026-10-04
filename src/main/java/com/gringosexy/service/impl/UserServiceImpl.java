package com.gringosexy.service.impl;

import com.gringosexy.dto.PermissionUpdateRequest;
import com.gringosexy.dto.RegisterRequest;
import com.gringosexy.dto.UserUpdateRequest;
import com.gringosexy.enums.DeviceType;
import com.gringosexy.enums.Role;
import com.gringosexy.enums.UserStatus;
import com.gringosexy.exception.ResourceNotFoundException;
import com.gringosexy.exception.ValidationException;
import com.gringosexy.model.User;
import com.gringosexy.model.UserPermissions;
import com.gringosexy.model.VerificationToken;
import com.gringosexy.repository.UserRepository;
import com.gringosexy.service.ActivityLogService;
import com.gringosexy.service.EmailService;
import com.gringosexy.service.UserService;
import com.gringosexy.service.VerificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final VerificationService verificationService;
    private final EmailService emailService;
    private final ActivityLogService activityLogService;
    private final MongoTemplate mongoTemplate;

    public UserServiceImpl(UserRepository userRepository,
                           PasswordEncoder passwordEncoder,
                           VerificationService verificationService,
                           EmailService emailService,
                           ActivityLogService activityLogService,
                           MongoTemplate mongoTemplate) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.verificationService = verificationService;
        this.emailService = emailService;
        this.activityLogService = activityLogService;
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public User registerUser(RegisterRequest request, String ipAddress) {
        // Backend Validations
        if (!request.isPasswordMatching()) {
            throw new ValidationException("Las contraseñas no coinciden.");
        }

        String email = request.getEmail().trim().toLowerCase();
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new ValidationException("El correo electrónico ya se encuentra registrado.");
        }

        String username = request.getUsername().trim().toLowerCase();
        if (userRepository.existsByUsernameIgnoreCase(username)) {
            throw new ValidationException("El nombre de usuario ya está en uso.");
        }

        User user = new User();
        user.setFullName(request.getFullName().trim());
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setDeviceType(request.getDeviceType());
        user.setRequestedModule(request.getRequestedModule() != null ? request.getRequestedModule() : "ALL");
        user.setRole(Role.USER);
        user.setStatus(UserStatus.PENDING);
        user.setEmailVerified(false);
        user.setPermissions(UserPermissions.allEnabled());
        user.setCreatedAt(Instant.now());
        user.setUpdatedAt(Instant.now());

        User savedUser = userRepository.save(user);

        // Generate email verification token & send email
        VerificationToken token = verificationService.createEmailVerificationToken(savedUser.getId(), savedUser.getEmail());
        emailService.sendVerificationEmail(savedUser.getEmail(), savedUser.getFullName(), token.getToken());

        activityLogService.log(savedUser.getId(), savedUser.getUsername(), "USER_REGISTERED", "Nuevo usuario registrado. Dispositivo: " + savedUser.getDeviceType() + " | Módulo: " + savedUser.getRequestedModule(), ipAddress);

        return savedUser;
    }

    @Override
    public User findById(String id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con ID: " + id));
    }

    @Override
    public Optional<User> findByEmail(String email) {
        if (email == null) return Optional.empty();
        return userRepository.findByEmailIgnoreCase(email.trim());
    }

    @Override
    public Optional<User> findByUsername(String username) {
        if (username == null) return Optional.empty();
        return userRepository.findByUsernameIgnoreCase(username.trim());
    }

    @Override
    public User updateProfile(String userId, UserUpdateRequest request, String ipAddress) {
        User user = findById(userId);

        user.setFullName(request.getFullName().trim());
        user.setDeviceType(request.getDeviceType());

        // Password change if provided
        if (request.getNewPassword() != null && !request.getNewPassword().trim().isEmpty()) {
            if (request.getCurrentPassword() == null || !passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
                throw new ValidationException("La contraseña actual es incorrecta.");
            }
            if (request.getNewPassword().length() < 6) {
                throw new ValidationException("La nueva contraseña debe tener al menos 6 caracteres.");
            }
            if (!request.getNewPassword().equals(request.getConfirmNewPassword())) {
                throw new ValidationException("La nueva contraseña y su confirmación no coinciden.");
            }
            user.setPassword(passwordEncoder.encode(request.getNewPassword()));
            activityLogService.log(user.getId(), user.getUsername(), "PASSWORD_CHANGED", "El usuario cambió su contraseña desde el perfil", ipAddress);
        }

        user.setUpdatedAt(Instant.now());
        User updated = userRepository.save(user);
        activityLogService.log(user.getId(), user.getUsername(), "PROFILE_UPDATED", "Perfil actualizado", ipAddress);
        return updated;
    }

    @Override
    public User updateStatus(String userId, UserStatus status, String adminId, String ipAddress) {
        User user = findById(userId);
        UserStatus prevStatus = user.getStatus();
        user.setStatus(status);
        user.setUpdatedAt(Instant.now());
        User updated = userRepository.save(user);

        emailService.sendAccountStatusNotification(user.getEmail(), user.getFullName(), status.name());
        activityLogService.log(adminId, "Admin", "USER_STATUS_CHANGED", "Estado de usuario " + user.getUsername() + " cambiado de " + prevStatus + " a " + status, ipAddress);
        return updated;
    }

    @Override
    public User updateRole(String userId, Role role, String adminId, String ipAddress) {
        User user = findById(userId);
        Role prevRole = user.getRole();
        user.setRole(role);
        user.setUpdatedAt(Instant.now());
        User updated = userRepository.save(user);

        activityLogService.log(adminId, "Admin", "ROLE_CHANGED", "Rol de " + user.getUsername() + " cambiado de " + prevRole + " a " + role, ipAddress);
        return updated;
    }

    @Override
    public User updateDevice(String userId, DeviceType deviceType, String adminId, String ipAddress) {
        User user = findById(userId);
        DeviceType prev = user.getDeviceType();
        user.setDeviceType(deviceType);
        user.setUpdatedAt(Instant.now());
        User updated = userRepository.save(user);

        activityLogService.log(adminId, "Admin", "DEVICE_CHANGED", "Dispositivo de " + user.getUsername() + " cambiado de " + prev + " a " + deviceType, ipAddress);
        return updated;
    }

    @Override
    public User updatePermissions(String userId, PermissionUpdateRequest request, String adminId, String ipAddress) {
        User user = findById(userId);
        user.setPermissions(request.toUserPermissions());
        user.setUpdatedAt(Instant.now());
        User updated = userRepository.save(user);

        activityLogService.log(adminId, "Admin", "PERMISSIONS_UPDATED", "Permisos actualizados para el usuario " + user.getUsername(), ipAddress);
        return updated;
    }

    @Override
    public void resetPassword(String token, String newPassword, String ipAddress) {
        VerificationToken resetToken = verificationService.validatePasswordResetToken(token);

        if (newPassword == null || newPassword.length() < 6) {
            throw new ValidationException("La nueva contraseña debe tener al menos 6 caracteres.");
        }

        User user = findById(resetToken.getUserId());
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setUpdatedAt(Instant.now());
        userRepository.save(user);

        verificationService.invalidateToken(token);
        activityLogService.log(user.getId(), user.getUsername(), "PASSWORD_RESET_SUCCESS", "Contraseña restablecida con éxito mediante token", ipAddress);
    }

    @Override
    public void initiatePasswordReset(String email, String ipAddress) {
        Optional<User> userOpt = findByEmail(email);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            VerificationToken token = verificationService.createPasswordResetToken(user.getId(), user.getEmail());
            emailService.sendPasswordResetEmail(user.getEmail(), user.getFullName(), token.getToken());
            activityLogService.log(user.getId(), user.getUsername(), "PASSWORD_RESET_REQUESTED", "Solicitud de restablecimiento de contraseña iniciada", ipAddress);
        } else {
            // Log for security auditing without throwing exception to prevent email enumeration
            log.info("Password reset requested for non-existent email: {}", email);
        }
    }

    @Override
    public Page<User> getUsers(String search, UserStatus status, Role role, DeviceType deviceType, Pageable pageable) {
        Query query = new Query();
        List<Criteria> criteriaList = new ArrayList<>();

        if (search != null && !search.trim().isEmpty()) {
            String s = search.trim();
            criteriaList.add(new Criteria().orOperator(
                    Criteria.where("fullName").regex(s, "i"),
                    Criteria.where("username").regex(s, "i"),
                    Criteria.where("email").regex(s, "i")
            ));
        }

        if (status != null) {
            criteriaList.add(Criteria.where("status").is(status));
        }

        if (role != null) {
            criteriaList.add(Criteria.where("role").is(role));
        }

        if (deviceType != null) {
            criteriaList.add(Criteria.where("deviceType").is(deviceType));
        }

        if (!criteriaList.isEmpty()) {
            query.addCriteria(new Criteria().andOperator(criteriaList.toArray(new Criteria[0])));
        }

        long count = mongoTemplate.count(query, User.class);
        query.with(pageable);
        List<User> list = mongoTemplate.find(query, User.class);

        return new PageImpl<>(list, pageable, count);
    }

    @Override
    public Map<String, Object> getUserStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalUsers", userRepository.count());
        stats.put("activeUsers", userRepository.countByStatus(UserStatus.ACTIVE));
        stats.put("pendingUsers", userRepository.countByStatus(UserStatus.PENDING));
        stats.put("suspendedUsers", userRepository.countByStatus(UserStatus.SUSPENDED));
        stats.put("blockedUsers", userRepository.countByStatus(UserStatus.BLOCKED));
        stats.put("iphoneUsers", userRepository.countByDeviceType(DeviceType.IPHONE));
        stats.put("samsungUsers", userRepository.countByDeviceType(DeviceType.SAMSUNG));
        stats.put("xiaomiUsers", userRepository.countByDeviceType(DeviceType.XIAOMI));
        stats.put("otherUsers", userRepository.countByDeviceType(DeviceType.OTHER));
        return stats;
    }
}
