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

        // Get recent pending users awaiting payment/activation
        org.springframework.data.domain.Pageable pendingPageable = org.springframework.data.domain.PageRequest.of(0, 10, org.springframework.data.domain.Sort.by("createdAt").descending());
        org.springframework.data.domain.Page<User> pendingUsersPage = userService.getUsers(null, com.gringosexy.enums.UserStatus.PENDING, null, null, pendingPageable);

        // Get latest registered users
        org.springframework.data.domain.Pageable latestPageable = org.springframework.data.domain.PageRequest.of(0, 8, org.springframework.data.domain.Sort.by("createdAt").descending());
        org.springframework.data.domain.Page<User> latestUsersPage = userService.getUsers(null, null, null, null, latestPageable);

        model.addAttribute("currentUser", admin);
        model.addAttribute("stats", stats);
        model.addAttribute("pendingUsers", pendingUsersPage.getContent());
        model.addAttribute("latestUsers", latestUsersPage.getContent());
        model.addAttribute("recentLogs", activityLogService.getRecentLogs());
        model.addAttribute("pageTitle", "Admin Dashboard — GRINGO SEXY");
        return "admin/dashboard";
    }
}
