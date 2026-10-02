package com.gringosexy.controller.admin;

import com.gringosexy.dto.ContentRequest;
import com.gringosexy.enums.ContentCategory;
import com.gringosexy.enums.DeviceType;
import com.gringosexy.model.Content;
import com.gringosexy.model.User;
import com.gringosexy.service.CategoryService;
import com.gringosexy.service.ContentService;
import com.gringosexy.service.DeviceService;
import com.gringosexy.util.SecurityUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/content")
public class AdminContentController {

    private final ContentService contentService;
    private final CategoryService categoryService;
    private final DeviceService deviceService;
    private final SecurityUtils securityUtils;

    public AdminContentController(ContentService contentService,
                                  CategoryService categoryService,
                                  DeviceService deviceService,
                                  SecurityUtils securityUtils) {
        this.contentService = contentService;
        this.categoryService = categoryService;
        this.deviceService = deviceService;
        this.securityUtils = securityUtils;
    }

    @GetMapping
    public String listContent(@RequestParam(value = "search", required = false) String search,
                              @RequestParam(value = "category", required = false) ContentCategory category,
                              @RequestParam(value = "deviceType", required = false) DeviceType deviceType,
                              @RequestParam(value = "page", defaultValue = "0") int page,
                              @RequestParam(value = "size", defaultValue = "10") int size,
                              Model model) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Content> contentsPage = contentService.searchContents(search, category, deviceType, pageable);

        model.addAttribute("contentsPage", contentsPage);
        model.addAttribute("search", search);
        model.addAttribute("selectedCategory", category);
        model.addAttribute("selectedDevice", deviceType);
        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("devices", deviceService.getAllDevices());
        model.addAttribute("pageTitle", "Gestión de Contenido — GRINGO SEXY");
        return "admin/content";
    }

    @GetMapping("/new")
    public String newContentForm(Model model) {
        ContentRequest contentRequest = new ContentRequest();
        contentRequest.setApplyToAllDevices(true);

        model.addAttribute("contentRequest", contentRequest);
        model.addAttribute("categories", categoryService.getActiveCategories());
        model.addAttribute("devices", deviceService.getActiveDevices());
        model.addAttribute("isNew", true);
        model.addAttribute("pageTitle", "Crear Nuevo Contenido — GRINGO SEXY");
        return "admin/content-form";
    }

    @PostMapping("/new")
    public String createContent(@Valid @ModelAttribute("contentRequest") ContentRequest contentRequest,
                                BindingResult bindingResult,
                                HttpServletRequest request,
                                RedirectAttributes redirectAttributes,
                                Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("categories", categoryService.getActiveCategories());
            model.addAttribute("devices", deviceService.getActiveDevices());
            model.addAttribute("isNew", true);
            return "admin/content-form";
        }

        String adminUsername = securityUtils.getCurrentUser().map(User::getUsername).orElse("Admin");
        String clientIp = SecurityUtils.getClientIp(request);
        contentService.createContent(contentRequest, adminUsername, clientIp);
        redirectAttributes.addFlashAttribute("successMessage", "¡Contenido creado exitosamente!");
        return "redirect:/admin/content";
    }

    @GetMapping("/{id}/edit")
    public String editContentForm(@PathVariable("id") String id, Model model) {
        Content content = contentService.findById(id);

        ContentRequest req = new ContentRequest();
        req.setId(content.getId());
        req.setTitle(content.getTitle());
        req.setSlug(content.getSlug());
        req.setSummary(content.getSummary());
        req.setBody(content.getBody());
        req.setCategory(content.getCategory());
        req.setApplyToAllDevices(content.isApplyToAllDevices());
        req.setTargetDevices(content.getTargetDevices());
        req.setImageUrl(content.getImageUrl());
        req.setVideoUrl(content.getVideoUrl());
        req.setActive(content.isActive());
        req.setFeatured(content.isFeatured());
        req.setSortOrder(content.getSortOrder());

        model.addAttribute("contentRequest", req);
        model.addAttribute("contentId", id);
        model.addAttribute("categories", categoryService.getActiveCategories());
        model.addAttribute("devices", deviceService.getActiveDevices());
        model.addAttribute("isNew", false);
        model.addAttribute("pageTitle", "Editar Contenido: " + content.getTitle());
        return "admin/content-form";
    }

    @PostMapping("/{id}/edit")
    public String updateContent(@PathVariable("id") String id,
                                @Valid @ModelAttribute("contentRequest") ContentRequest contentRequest,
                                BindingResult bindingResult,
                                HttpServletRequest request,
                                RedirectAttributes redirectAttributes,
                                Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("contentId", id);
            model.addAttribute("categories", categoryService.getActiveCategories());
            model.addAttribute("devices", deviceService.getActiveDevices());
            model.addAttribute("isNew", false);
            return "admin/content-form";
        }

        String clientIp = SecurityUtils.getClientIp(request);
        contentService.updateContent(id, contentRequest, clientIp);
        redirectAttributes.addFlashAttribute("successMessage", "¡Contenido actualizado correctamente!");
        return "redirect:/admin/content";
    }

    @PostMapping("/{id}/delete")
    public String deleteContent(@PathVariable("id") String id,
                                HttpServletRequest request,
                                RedirectAttributes redirectAttributes) {
        String clientIp = SecurityUtils.getClientIp(request);
        contentService.deleteContent(id, clientIp);
        redirectAttributes.addFlashAttribute("successMessage", "Contenido eliminado exitosamente.");
        return "redirect:/admin/content";
    }
}
