package com.gringosexy.controller.admin;

import com.gringosexy.dto.PermissionUpdateRequest;
import com.gringosexy.enums.DeviceType;
import com.gringosexy.enums.Role;
import com.gringosexy.enums.UserStatus;
import com.gringosexy.model.User;
import com.gringosexy.service.DeviceService;
import com.gringosexy.service.UserService;
import com.gringosexy.util.SecurityUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/users")
public class AdminUserController {

    private final UserService userService;
    private final DeviceService deviceService;
    private final SecurityUtils securityUtils;

    public AdminUserController(UserService userService, DeviceService deviceService, SecurityUtils securityUtils) {
        this.userService = userService;
        this.deviceService = deviceService;
        this.securityUtils = securityUtils;
    }

    @GetMapping
    public String listUsers(@RequestParam(value = "search", required = false) String search,
                            @RequestParam(value = "status", required = false) UserStatus status,
                            @RequestParam(value = "role", required = false) Role role,
                            @RequestParam(value = "deviceType", required = false) DeviceType deviceType,
                            @RequestParam(value = "page", defaultValue = "0") int page,
                            @RequestParam(value = "size", defaultValue = "10") int size,
                            @RequestParam(value = "sortBy", defaultValue = "createdAt") String sortBy,
                            @RequestParam(value = "direction", defaultValue = "DESC") String direction,
                            Model model) {
        Sort sort = direction.equalsIgnoreCase("ASC") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<User> usersPage = userService.getUsers(search, status, role, deviceType, pageable);

        model.addAttribute("usersPage", usersPage);
        model.addAttribute("search", search);
        model.addAttribute("status", status);
        model.addAttribute("role", role);
        model.addAttribute("deviceType", deviceType);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("direction", direction);
        model.addAttribute("devices", deviceService.getAllDevices());
        model.addAttribute("statuses", UserStatus.values());
        model.addAttribute("roles", Role.values());
        model.addAttribute("pageTitle", "Gestión de Usuarios — GRINGO SEXY");

        return "admin/users";
    }

    @GetMapping("/{id}")
    public String userDetail(@PathVariable("id") String id, Model model) {
        User user = userService.findById(id);
        model.addAttribute("user", user);
        model.addAttribute("devices", deviceService.getAllDevices());
        model.addAttribute("statuses", UserStatus.values());
        model.addAttribute("roles", Role.values());
        model.addAttribute("permissionRequest", PermissionUpdateRequest.fromUserPermissions(user.getPermissions()));
        model.addAttribute("pageTitle", "Detalle de Usuario: " + user.getUsername() + " — GRINGO SEXY");
        return "admin/user-detail";
    }

    @PostMapping("/{id}/status")
    public String updateStatus(@PathVariable("id") String id,
                               @RequestParam("status") UserStatus status,
                               HttpServletRequest request,
                               RedirectAttributes redirectAttributes) {
        String adminId = securityUtils.getCurrentUser().map(User::getId).orElse("system");
        String clientIp = SecurityUtils.getClientIp(request);
        userService.updateStatus(id, status, adminId, clientIp);
        redirectAttributes.addFlashAttribute("successMessage", "Estado de usuario actualizado a: " + status);
        return "redirect:/admin/users/" + id;
    }

    @PostMapping("/{id}/role")
    public String updateRole(@PathVariable("id") String id,
                             @RequestParam("role") Role role,
                             HttpServletRequest request,
                             RedirectAttributes redirectAttributes) {
        String adminId = securityUtils.getCurrentUser().map(User::getId).orElse("system");
        String clientIp = SecurityUtils.getClientIp(request);
        userService.updateRole(id, role, adminId, clientIp);
        redirectAttributes.addFlashAttribute("successMessage", "Rol de usuario actualizado a: " + role);
        return "redirect:/admin/users/" + id;
    }

    @PostMapping("/{id}/device")
    public String updateDevice(@PathVariable("id") String id,
                               @RequestParam("deviceType") DeviceType deviceType,
                               HttpServletRequest request,
                               RedirectAttributes redirectAttributes) {
        String adminId = securityUtils.getCurrentUser().map(User::getId).orElse("system");
        String clientIp = SecurityUtils.getClientIp(request);
        userService.updateDevice(id, deviceType, adminId, clientIp);
        redirectAttributes.addFlashAttribute("successMessage", "Dispositivo de usuario actualizado a: " + deviceType.getDisplayName());
        return "redirect:/admin/users/" + id;
    }

    @PostMapping("/{id}/permissions")
    public String updatePermissions(@PathVariable("id") String id,
                                    @ModelAttribute("permissionRequest") PermissionUpdateRequest permissionRequest,
                                    HttpServletRequest request,
                                    RedirectAttributes redirectAttributes) {
        String adminId = securityUtils.getCurrentUser().map(User::getId).orElse("system");
        String clientIp = SecurityUtils.getClientIp(request);
        userService.updatePermissions(id, permissionRequest, adminId, clientIp);
        redirectAttributes.addFlashAttribute("successMessage", "Permisos de usuario actualizados correctamente.");
        return "redirect:/admin/users/" + id;
    }

    @PostMapping("/{id}/quick-activate")
    public String quickActivate(@PathVariable("id") String id,
                                HttpServletRequest request,
                                RedirectAttributes redirectAttributes) {
        String adminId = securityUtils.getCurrentUser().map(User::getId).orElse("system");
        String clientIp = SecurityUtils.getClientIp(request);
        userService.updateStatus(id, UserStatus.ACTIVE, adminId, clientIp);
        redirectAttributes.addFlashAttribute("successMessage", "¡Usuario activado exitosamente! Ahora tiene acceso a la plataforma.");
        return "redirect:/admin/users";
    }
}
