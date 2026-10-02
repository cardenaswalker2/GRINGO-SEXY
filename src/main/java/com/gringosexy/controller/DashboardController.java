package com.gringosexy.controller;

import com.gringosexy.enums.ContentCategory;
import com.gringosexy.exception.ResourceNotFoundException;
import com.gringosexy.exception.UnauthorizedException;
import com.gringosexy.model.Category;
import com.gringosexy.model.Content;
import com.gringosexy.model.User;
import com.gringosexy.service.CategoryService;
import com.gringosexy.service.ContentService;
import com.gringosexy.service.PermissionService;
import com.gringosexy.util.SecurityUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@Controller
public class DashboardController {

    private final SecurityUtils securityUtils;
    private final CategoryService categoryService;
    private final ContentService contentService;
    private final PermissionService permissionService;

    public DashboardController(SecurityUtils securityUtils,
                               CategoryService categoryService,
                               ContentService contentService,
                               PermissionService permissionService) {
        this.securityUtils = securityUtils;
        this.categoryService = categoryService;
        this.contentService = contentService;
        this.permissionService = permissionService;
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        User user = securityUtils.getCurrentUser()
                .orElseThrow(() -> new UnauthorizedException("Sesión no válida"));

        List<Category> allCategories = categoryService.getActiveCategories();

        model.addAttribute("currentUser", user);
        model.addAttribute("categories", allCategories);
        model.addAttribute("pageTitle", "Panel de Usuario — GRINGO SEXY");
        return "dashboard/index";
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

        Content content = contentService.findBySlug(contentSlug);

        // Verify device compatibility
        if (!content.isAvailableFor(user.getDeviceType()) && !user.isAdmin()) {
            throw new UnauthorizedException("Este contenido no está optimizado para tu dispositivo registrado (" + user.getDeviceType().getDisplayName() + ").");
        }

        model.addAttribute("currentUser", user);
        model.addAttribute("content", content);
        model.addAttribute("pageTitle", content.getTitle() + " — GRINGO SEXY");
        return "dashboard/content-detail";
    }
}
