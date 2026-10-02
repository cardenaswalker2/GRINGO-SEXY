package com.gringosexy.config;

import com.gringosexy.enums.DeviceType;
import com.gringosexy.enums.Role;
import com.gringosexy.enums.UserStatus;
import com.gringosexy.model.User;
import com.gringosexy.model.UserPermissions;
import com.gringosexy.repository.UserRepository;
import com.gringosexy.service.CategoryService;
import com.gringosexy.service.ContentService;
import com.gringosexy.service.DeviceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;

@Configuration
public class DataInitializer {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    @Value("${app.init.superadmin.username:admin}")
    private String adminUsername;

    @Value("${app.init.superadmin.email:admin@gringosexy.com}")
    private String adminEmail;

    @Value("${app.init.superadmin.password:Admin12345!}")
    private String adminPassword;

    @Value("${app.init.superadmin.full-name:Super Admin GRINGO SEXY}")
    private String adminFullName;

    @Bean
    public CommandLineRunner initDatabase(UserRepository userRepository,
                                          PasswordEncoder passwordEncoder,
                                          DeviceService deviceService,
                                          CategoryService categoryService,
                                          ContentService contentService) {
        return args -> {
            log.info("Initializing GRINGO SEXY database seeds...");

            // 1. Initialize Default Devices
            deviceService.initDefaultDevices();

            // 2. Initialize Default Categories
            categoryService.initDefaultCategories();

            // 3. Initialize Curated Contents
            contentService.initDefaultContents();

            // 4. Initialize Super Admin if none exists
            if (userRepository.countByRole(Role.SUPER_ADMIN) == 0 && !userRepository.existsByUsernameIgnoreCase(adminUsername)) {
                User superAdmin = new User();
                superAdmin.setFullName(adminFullName);
                superAdmin.setUsername(adminUsername.toLowerCase());
                superAdmin.setEmail(adminEmail.toLowerCase());
                superAdmin.setPassword(passwordEncoder.encode(adminPassword));
                superAdmin.setRole(Role.SUPER_ADMIN);
                superAdmin.setStatus(UserStatus.ACTIVE);
                superAdmin.setDeviceType(DeviceType.IPHONE);
                superAdmin.setEmailVerified(true);
                superAdmin.setPermissions(UserPermissions.allEnabled());
                superAdmin.setCreatedAt(Instant.now());
                superAdmin.setUpdatedAt(Instant.now());

                userRepository.save(superAdmin);
                log.info("=================================================================");
                log.info("SUPER ADMIN CREATED SUCCESSFULLY:");
                log.info("Username: {}", adminUsername);
                log.info("Email:    {}", adminEmail);
                log.info("Password: [CONFIGURED IN ENVIRONMENT/PROPERTIES]");
                log.info("Role:     SUPER_ADMIN");
                log.info("=================================================================");
            } else {
                log.info("Super Admin already exists in database.");
            }
        };
    }
}
