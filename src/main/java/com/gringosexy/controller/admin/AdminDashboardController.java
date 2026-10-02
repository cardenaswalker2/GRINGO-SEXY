package com.gringosexy.controller.admin;

import com.gringosexy.enums.TicketStatus;
import com.gringosexy.model.User;
import com.gringosexy.repository.ContentRepository;
import com.gringosexy.service.ActivityLogService;
import com.gringosexy.service.SupportService;
import com.gringosexy.service.UserService;
import com.gringosexy.util.SecurityUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Map;

@Controller
@RequestMapping("/admin")
public class AdminDashboardController {

    private final SecurityUtils securityUtils;
    private final UserService userService;
    private final ContentRepository contentRepository;
    private final SupportService supportService;
    private final ActivityLogService activityLogService;

    public AdminDashboardController(SecurityUtils securityUtils,
                                    UserService userService,
                                    ContentRepository contentRepository,
                                    SupportService supportService,
                                    ActivityLogService activityLogService) {
        this.securityUtils = securityUtils;
        this.userService = userService;
        this.contentRepository = contentRepository;
        this.supportService = supportService;
        this.activityLogService = activityLogService;
    }

    @GetMapping
    public String dashboard(Model model) {
        User admin = securityUtils.getCurrentUser().orElse(null);
        Map<String, Object> stats = userService.getUserStatistics();

        stats.put("totalContents", contentRepository.count());
        stats.put("openTickets", supportService.getTicketsByStatus(TicketStatus.OPEN, null).getTotalElements());

        model.addAttribute("currentUser", admin);
        model.addAttribute("stats", stats);
        model.addAttribute("recentLogs", activityLogService.getRecentLogs());
        model.addAttribute("pageTitle", "Admin Dashboard — GRINGO SEXY");
        return "admin/dashboard";
    }
}
