package com.gringosexy;

import com.gringosexy.dto.RegisterRequest;
import com.gringosexy.enums.DeviceType;
import com.gringosexy.enums.Role;
import com.gringosexy.enums.UserStatus;
import com.gringosexy.model.User;
import com.gringosexy.repository.UserRepository;
import com.gringosexy.service.UserService;
import com.gringosexy.service.VerificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@SpringBootTest
@AutoConfigureMockMvc
class GringoSexyApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private VerificationService verificationService;

    @Test
    @DisplayName("1. Home page renders publicly without authentication")
    void contextLoadsAndHomePageIsPublic() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("home/index"));
    }

    @Test
    @DisplayName("2. User registration flow creates user with PENDING state and triggers verification")
    void testUserRegistrationFlow() {
        String testEmail = "testuser_" + System.currentTimeMillis() + "@gmail.com";
        String testUsername = "user_" + System.currentTimeMillis();

        RegisterRequest req = new RegisterRequest();
        req.setFullName("Test User Mobile");
        req.setUsername(testUsername);
        req.setEmail(testEmail);
        req.setPassword("Password123!");
        req.setConfirmPassword("Password123!");
        req.setDeviceType(DeviceType.IPHONE);

        User registered = userService.registerUser(req, "127.0.0.1");

        assertNotNull(registered.getId());
        assertEquals(UserStatus.PENDING, registered.getStatus());
        assertEquals(Role.USER, registered.getRole());
        assertEquals(DeviceType.IPHONE, registered.getDeviceType());
    }

    @Test
    @DisplayName("3. Unauthenticated user trying to access /admin is redirected to login")
    void testAdminRouteRequiresAuthentication() throws Exception {
        mockMvc.perform(get("/admin"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    @WithMockUser(username = "regular_user", roles = {"USER"})
    @DisplayName("4. Standard USER cannot access /admin (Forbidden 403)")
    void testStandardUserCannotAccessAdmin() throws Exception {
        mockMvc.perform(get("/admin"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin_user", roles = {"ADMIN"})
    @DisplayName("5. ADMIN can access /admin successfully")
    void testAdminCanAccessAdminDashboard() throws Exception {
        mockMvc.perform(get("/admin"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/dashboard"));
    }
}
