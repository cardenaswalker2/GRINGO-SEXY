package com.gringosexy.controller;

import com.gringosexy.dto.UserUpdateRequest;
import com.gringosexy.exception.UnauthorizedException;
import com.gringosexy.exception.ValidationException;
import com.gringosexy.model.User;
import com.gringosexy.service.DeviceService;
import com.gringosexy.service.UserService;
import com.gringosexy.util.SecurityUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class ProfileController {

    private final SecurityUtils securityUtils;
    private final UserService userService;
    private final DeviceService deviceService;

    public ProfileController(SecurityUtils securityUtils,
                             UserService userService,
                             DeviceService deviceService) {
        this.securityUtils = securityUtils;
        this.userService = userService;
        this.deviceService = deviceService;
    }

    @GetMapping("/profile")
    public String profilePage(Model model) {
        User user = securityUtils.getCurrentUser()
                .orElseThrow(() -> new UnauthorizedException("Sesión no válida"));

        UserUpdateRequest updateRequest = new UserUpdateRequest();
        updateRequest.setFullName(user.getFullName());
        updateRequest.setDeviceType(user.getDeviceType());

        model.addAttribute("currentUser", user);
        model.addAttribute("updateRequest", updateRequest);
        model.addAttribute("devices", deviceService.getActiveDevices());
        model.addAttribute("pageTitle", "Mi Perfil — GRINGO SEXY");
        return "profile/index";
    }

    @PostMapping("/profile")
    public String updateProfile(@Valid @ModelAttribute("updateRequest") UserUpdateRequest updateRequest,
                                BindingResult bindingResult,
                                HttpServletRequest request,
                                RedirectAttributes redirectAttributes,
                                Model model) {
        User user = securityUtils.getCurrentUser()
                .orElseThrow(() -> new UnauthorizedException("Sesión no válida"));

        if (bindingResult.hasErrors()) {
            model.addAttribute("currentUser", user);
            model.addAttribute("devices", deviceService.getActiveDevices());
            return "profile/index";
        }

        try {
            String clientIp = SecurityUtils.getClientIp(request);
            userService.updateProfile(user.getId(), updateRequest, clientIp);
            redirectAttributes.addFlashAttribute("successMessage", "¡Tu perfil ha sido actualizado exitosamente!");
            return "redirect:/profile";
        } catch (ValidationException e) {
            model.addAttribute("currentUser", user);
            model.addAttribute("devices", deviceService.getActiveDevices());
            model.addAttribute("errorMessage", e.getMessage());
            return "profile/index";
        }
    }
}
