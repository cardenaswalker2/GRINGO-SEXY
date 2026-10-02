package com.gringosexy.controller;

import com.gringosexy.exception.UnauthorizedException;
import com.gringosexy.model.SupportTicket;
import com.gringosexy.model.User;
import com.gringosexy.service.SupportService;
import com.gringosexy.util.SecurityUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
public class SupportController {

    private final SecurityUtils securityUtils;
    private final SupportService supportService;

    public SupportController(SecurityUtils securityUtils, SupportService supportService) {
        this.securityUtils = securityUtils;
        this.supportService = supportService;
    }

    @GetMapping("/support")
    public String supportPage(Model model) {
        User user = securityUtils.getCurrentUser()
                .orElseThrow(() -> new UnauthorizedException("Sesión no válida"));

        List<SupportTicket> userTickets = supportService.getUserTickets(user.getId());

        model.addAttribute("currentUser", user);
        model.addAttribute("tickets", userTickets);
        model.addAttribute("pageTitle", "Centro de Soporte Técnico — GRINGO SEXY");
        return "support/index";
    }

    @PostMapping("/support")
    public String createTicket(@RequestParam("subject") String subject,
                               @RequestParam("message") String message,
                               HttpServletRequest request,
                               RedirectAttributes redirectAttributes) {
        User user = securityUtils.getCurrentUser()
                .orElseThrow(() -> new UnauthorizedException("Sesión no válida"));

        if (subject == null || subject.trim().isEmpty() || message == null || message.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "El asunto y el mensaje son campos obligatorios.");
            return "redirect:/support";
        }

        String clientIp = SecurityUtils.getClientIp(request);
        supportService.createTicket(user, subject, message, clientIp);
        redirectAttributes.addFlashAttribute("successMessage", "¡Tu ticket de soporte ha sido enviado con éxito! Nuestro equipo responderá a la brevedad.");
        return "redirect:/support";
    }
}
