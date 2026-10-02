package com.gringosexy.controller.admin;

import com.gringosexy.enums.TicketStatus;
import com.gringosexy.model.Category;
import com.gringosexy.model.SupportTicket;
import com.gringosexy.model.User;
import com.gringosexy.service.ActivityLogService;
import com.gringosexy.service.CategoryService;
import com.gringosexy.service.SupportService;
import com.gringosexy.util.SecurityUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin")
public class AdminOperationsController {

    private final CategoryService categoryService;
    private final SupportService supportService;
    private final ActivityLogService activityLogService;
    private final SecurityUtils securityUtils;

    @Value("${app.social.discord:https://discord.gg/gringosexy}")
    private String socialDiscord;

    @Value("${app.social.tiktok:https://tiktok.com/@gringosexy}")
    private String socialTiktok;

    @Value("${app.social.instagram:https://instagram.com/gringosexy}")
    private String socialInstagram;

    @Value("${app.social.youtube:https://youtube.com/@gringosexy}")
    private String socialYoutube;

    public AdminOperationsController(CategoryService categoryService,
                                     SupportService supportService,
                                     ActivityLogService activityLogService,
                                     SecurityUtils securityUtils) {
        this.categoryService = categoryService;
        this.supportService = supportService;
        this.activityLogService = activityLogService;
        this.securityUtils = securityUtils;
    }

    // Categories
    @GetMapping("/categories")
    public String listCategories(Model model) {
        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("pageTitle", "Gestión de Categorías — GRINGO SEXY");
        return "admin/categories";
    }

    @PostMapping("/categories/save")
    public String saveCategory(@RequestParam(value = "id", required = false) String id,
                               @RequestParam("name") String name,
                               @RequestParam(value = "slug", required = false) String slug,
                               @RequestParam(value = "description", defaultValue = "") String description,
                               @RequestParam(value = "iconClass", defaultValue = "fas fa-folder") String iconClass,
                               @RequestParam(value = "sortOrder", defaultValue = "0") int sortOrder,
                               @RequestParam(value = "active", defaultValue = "true") boolean active,
                               RedirectAttributes redirectAttributes) {
        categoryService.createOrUpdateCategory(id, name, slug, description, iconClass, sortOrder, active);
        redirectAttributes.addFlashAttribute("successMessage", "¡Categoría guardada exitosamente!");
        return "redirect:/admin/categories";
    }

    @PostMapping("/categories/toggle/{id}")
    public String toggleCategory(@PathVariable("id") String id, RedirectAttributes redirectAttributes) {
        Category cat = categoryService.toggleActive(id);
        redirectAttributes.addFlashAttribute("successMessage", "Categoría '" + cat.getName() + "' " + (cat.isActive() ? "activada" : "desactivada"));
        return "redirect:/admin/categories";
    }

    @PostMapping("/categories/delete/{id}")
    public String deleteCategory(@PathVariable("id") String id, RedirectAttributes redirectAttributes) {
        categoryService.deleteCategory(id);
        redirectAttributes.addFlashAttribute("successMessage", "Categoría eliminada exitosamente.");
        return "redirect:/admin/categories";
    }

    // Support Tickets
    @GetMapping("/support")
    public String listTickets(@RequestParam(value = "status", required = false) TicketStatus status,
                              @RequestParam(value = "page", defaultValue = "0") int page,
                              @RequestParam(value = "size", defaultValue = "10") int size,
                              Model model) {
        Pageable pageable = PageRequest.of(page, size);
        Page<SupportTicket> ticketsPage = supportService.getTicketsByStatus(status, pageable);

        model.addAttribute("ticketsPage", ticketsPage);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("statuses", TicketStatus.values());
        model.addAttribute("pageTitle", "Gestión de Tickets de Soporte — GRINGO SEXY");
        return "admin/support";
    }

    @PostMapping("/support/{id}/reply")
    public String replyTicket(@PathVariable("id") String id,
                              @RequestParam("adminResponse") String adminResponse,
                              @RequestParam("status") TicketStatus status,
                              HttpServletRequest request,
                              RedirectAttributes redirectAttributes) {
        String adminId = securityUtils.getCurrentUser().map(User::getId).orElse("system");
        String clientIp = SecurityUtils.getClientIp(request);
        supportService.replyAndChangeStatus(id, adminResponse, status, adminId, clientIp);
        redirectAttributes.addFlashAttribute("successMessage", "Respuesta enviada y ticket actualizado.");
        return "redirect:/admin/support";
    }

    // Activity Logs
    @GetMapping("/activity")
    public String listActivity(@RequestParam(value = "action", required = false) String action,
                               @RequestParam(value = "page", defaultValue = "0") int page,
                               @RequestParam(value = "size", defaultValue = "20") int size,
                               Model model) {
        Pageable pageable = PageRequest.of(page, size);
        var logsPage = (action != null && !action.trim().isEmpty())
                ? activityLogService.getLogsByAction(action.trim(), pageable)
                : activityLogService.getLogs(pageable);

        model.addAttribute("logsPage", logsPage);
        model.addAttribute("selectedAction", action);
        model.addAttribute("pageTitle", "Registro de Actividad y Auditoría — GRINGO SEXY");
        return "admin/activity";
    }

    // Settings
    @GetMapping("/settings")
    public String settingsPage(Model model) {
        model.addAttribute("socialDiscord", socialDiscord);
        model.addAttribute("socialTiktok", socialTiktok);
        model.addAttribute("socialInstagram", socialInstagram);
        model.addAttribute("socialYoutube", socialYoutube);
        model.addAttribute("pageTitle", "Configuración del Sistema — GRINGO SEXY");
        return "admin/settings";
    }
}
