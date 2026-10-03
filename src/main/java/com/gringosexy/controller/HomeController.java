package com.gringosexy.controller;

import com.gringosexy.service.CategoryService;
import com.gringosexy.service.DeviceService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    private final CategoryService categoryService;
    private final DeviceService deviceService;

    @Value("${app.social.discord:https://discord.gg/gringosexy}")
    private String socialDiscord;

    @Value("${app.social.tiktok:https://tiktok.com/@gringosexy}")
    private String socialTiktok;

    @Value("${app.social.instagram:https://instagram.com/gringosexy}")
    private String socialInstagram;

    @Value("${app.social.youtube:https://youtube.com/@gringosexy}")
    private String socialYoutube;

    private final com.gringosexy.service.ContentService contentService;

    @Value("${app.contact.whatsapp-number:+12392450044}")
    private String whatsappNumber;

    public HomeController(CategoryService categoryService, DeviceService deviceService, com.gringosexy.service.ContentService contentService) {
        this.categoryService = categoryService;
        this.deviceService = deviceService;
        this.contentService = contentService;
    }

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("pageTitle", "GRINGO SEXY — Modificaciones, Sensibilidades & Optimización Móvil");
        model.addAttribute("categories", categoryService.getActiveCategories());
        model.addAttribute("devices", deviceService.getActiveDevices());
        model.addAttribute("socialDiscord", socialDiscord);
        model.addAttribute("socialTiktok", socialTiktok);
        model.addAttribute("socialInstagram", socialInstagram);
        model.addAttribute("socialYoutube", socialYoutube);
        return "home/index";
    }

    @GetMapping("/preview/{categorySlug}")
    public String previewCategory(@org.springframework.web.bind.annotation.PathVariable("categorySlug") String categorySlug, Model model) {
        com.gringosexy.enums.ContentCategory categoryEnum = com.gringosexy.enums.ContentCategory.fromSlug(categorySlug);
        if (categoryEnum == null) {
            return "redirect:/#funciones";
        }

        com.gringosexy.model.Category category = categoryService.getByCode(categoryEnum);
        java.util.List<com.gringosexy.model.Content> sampleContents = contentService.getContentsForUserCategory(null, categoryEnum);

        String cleanNumber = whatsappNumber.replaceAll("[^0-9]", "");
        String encodedMsg = java.net.URLEncoder.encode("Hola! Vi las funciones de " + category.getName() + " en GRINGO SEXY y quiero activar mi cuenta para desbloquear todo el contenido.", java.nio.charset.StandardCharsets.UTF_8);
        String whatsappDirectUrl = "https://wa.me/" + cleanNumber + "?text=" + encodedMsg;

        model.addAttribute("category", category);
        model.addAttribute("contents", sampleContents);
        model.addAttribute("whatsappDirectUrl", whatsappDirectUrl);
        model.addAttribute("whatsappNumberDisplay", whatsappNumber);
        model.addAttribute("pageTitle", "Vista Previa: " + category.getName() + " — GRINGO SEXY");
        return "home/preview";
    }
}
