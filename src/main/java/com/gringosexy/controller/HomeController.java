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

    public HomeController(CategoryService categoryService, DeviceService deviceService) {
        this.categoryService = categoryService;
        this.deviceService = deviceService;
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
}
