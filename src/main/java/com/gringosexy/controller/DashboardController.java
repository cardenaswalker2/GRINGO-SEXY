package com.gringosexy.controller;

import com.gringosexy.dto.DeviceCatalogCardDto;
import com.gringosexy.enums.ContentCategory;
import com.gringosexy.enums.DeviceType;
import com.gringosexy.exception.ResourceNotFoundException;
import com.gringosexy.exception.UnauthorizedException;
import com.gringosexy.model.Category;
import com.gringosexy.model.Content;
import com.gringosexy.model.Device;
import com.gringosexy.model.User;
import com.gringosexy.service.CategoryService;
import com.gringosexy.service.ContentService;
import com.gringosexy.service.DeviceService;
import com.gringosexy.service.PermissionService;
import com.gringosexy.util.SecurityUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.ArrayList;
import java.util.List;

@Controller
public class DashboardController {

    private final SecurityUtils securityUtils;
    private final CategoryService categoryService;
    private final ContentService contentService;
    private final DeviceService deviceService;
    private final PermissionService permissionService;

    public DashboardController(SecurityUtils securityUtils,
                               CategoryService categoryService,
                               ContentService contentService,
                               DeviceService deviceService,
                               PermissionService permissionService) {
        this.securityUtils = securityUtils;
        this.categoryService = categoryService;
        this.contentService = contentService;
        this.deviceService = deviceService;
        this.permissionService = permissionService;
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        User user = securityUtils.getCurrentUser()
                .orElseThrow(() -> new UnauthorizedException("Sesión no válida"));

        List<Category> allCategories = categoryService.getActiveCategories();
        List<Device> activeDevices = deviceService.getActiveDevices();

        List<DeviceCatalogCardDto> deviceCards = new ArrayList<>();
        for (Device dev : activeDevices) {
            DeviceType type = dev.getType();
            if (type != null && type != DeviceType.OTHER) {
                long videoCount = contentService.countActiveVideosByDevice(type);
                long totalCount = contentService.countActiveContentsByDevice(type);
                boolean isUserDevice = (user.getDeviceType() == type);

                deviceCards.add(new DeviceCatalogCardDto(
                        type,
                        type.name().toLowerCase(),
                        dev.getName(),
                        dev.getDescription(),
                        dev.getIconClass(),
                        videoCount,
                        totalCount,
                        isUserDevice
                ));
            }
        }

        model.addAttribute("currentUser", user);
        model.addAttribute("categories", allCategories);
        model.addAttribute("deviceCards", deviceCards);
        model.addAttribute("pageTitle", "Panel de Usuario — GRINGO SEXY");
        return "dashboard/index";
    }

    @GetMapping("/devices/{deviceCode}")
    public String deviceCatalog(@PathVariable("deviceCode") String deviceCode, Model model) {
        User user = securityUtils.getCurrentUser()
                .orElseThrow(() -> new UnauthorizedException("Sesión no válida"));

        DeviceType deviceType;
        try {
            deviceType = DeviceType.valueOf(deviceCode.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ResourceNotFoundException("Dispositivo no encontrado: " + deviceCode);
        }

        Device device = deviceService.getActiveDevices().stream()
                .filter(d -> d.getType() == deviceType)
                .findFirst()
                .orElse(new Device(deviceType, deviceType.getDisplayName(), deviceType.getDescription(), deviceType.getIconClass(), 0));

        List<Content> contents = contentService.getContentsForDevice(deviceType);
        long videoCount = contents.stream().filter(c -> c.getVideoUrl() != null && !c.getVideoUrl().trim().isEmpty()).count();

        model.addAttribute("currentUser", user);
        model.addAttribute("device", device);
        model.addAttribute("deviceType", deviceType);
        model.addAttribute("contents", contents);
        model.addAttribute("videoCount", videoCount);
        model.addAttribute("categories", categoryService.getActiveCategories());
        model.addAttribute("pageTitle", "Catálogo de Videos — " + device.getName() + " — GRINGO SEXY");
        return "dashboard/device-catalog";
    }

    @GetMapping("/content/{categorySlug}")
    public String categoryContent(@PathVariable("categorySlug") String categorySlug, Model model) {
        User user = securityUtils.getCurrentUser()
                .orElseThrow(() -> new UnauthorizedException("Sesión no válida"));

        // 1. Double backend validation: check if category exists
        ContentCategory categoryEnum = ContentCategory.fromSlug(categorySlug);
        if (categoryEnum == null) {
            throw new ResourceNotFoundException("Categoría no encontrada: " + categorySlug);
        }

        // 2. Double backend security validation: check user permissions
        if (!permissionService.hasPermission(categorySlug)) {
            throw new UnauthorizedException("No tienes permiso para acceder a la sección de " + categoryEnum.getDisplayName());
        }

        Category category = categoryService.getByCode(categoryEnum);
        List<Content> contents = contentService.getContentsForUserCategory(user, categoryEnum);

        model.addAttribute("currentUser", user);
        model.addAttribute("category", category);
        model.addAttribute("contents", contents);
        model.addAttribute("pageTitle", category.getName() + " — GRINGO SEXY");
        return "dashboard/content";
    }

    @GetMapping("/content/{categorySlug}/{contentSlug}")
    public String contentDetail(@PathVariable("categorySlug") String categorySlug,
                                @PathVariable("contentSlug") String contentSlug,
                                Model model) {
        User user = securityUtils.getCurrentUser()
                .orElseThrow(() -> new UnauthorizedException("Sesión no válida"));

        if (!permissionService.hasPermission(categorySlug)) {
            throw new UnauthorizedException("No tienes permiso para acceder a este contenido.");
        }

        ContentCategory categoryEnum = ContentCategory.fromSlug(categorySlug);
        Category category = (categoryEnum != null) ? categoryService.getByCode(categoryEnum) : null;

        Content content = contentService.findBySlug(contentSlug);
        boolean isDeviceCompatible = content.isAvailableFor(user.getDeviceType()) || user.isAdmin();

        List<Content> relatedContents = (categoryEnum != null)
                ? contentService.getContentsForUserCategory(user, categoryEnum)
                : List.of();

        model.addAttribute("currentUser", user);
        model.addAttribute("category", category);
        model.addAttribute("content", content);
        model.addAttribute("relatedContents", relatedContents);
        model.addAttribute("isDeviceCompatible", isDeviceCompatible);
        model.addAttribute("pageTitle", content.getTitle() + " — GRINGO SEXY");
        return "dashboard/content-detail";
    }
}
