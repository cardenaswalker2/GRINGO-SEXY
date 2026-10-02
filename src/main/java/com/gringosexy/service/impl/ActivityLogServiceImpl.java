package com.gringosexy.service.impl;

import com.gringosexy.model.ActivityLog;
import com.gringosexy.repository.ActivityLogRepository;
import com.gringosexy.service.ActivityLogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ActivityLogServiceImpl implements ActivityLogService {

    private static final Logger log = LoggerFactory.getLogger(ActivityLogServiceImpl.class);

    private final ActivityLogRepository activityLogRepository;

    public ActivityLogServiceImpl(ActivityLogRepository activityLogRepository) {
        this.activityLogRepository = activityLogRepository;
    }

    @Async
    @Override
    public void log(String userId, String username, String action, String description, String ipAddress) {
        try {
            ActivityLog activityLog = new ActivityLog(userId, username, action, description, ipAddress);
            activityLogRepository.save(activityLog);
            log.info("[ACTIVITY LOG] Action: {} | User: {} ({}) | Desc: {}", action, username, userId, description);
        } catch (Exception e) {
            log.error("Failed to save activity log: {}", e.getMessage());
        }
    }

    @Override
    public List<ActivityLog> getRecentLogs() {
        return activityLogRepository.findTop10ByOrderByTimestampDesc();
    }

    @Override
    public Page<ActivityLog> getLogs(Pageable pageable) {
        return activityLogRepository.findAllByOrderByTimestampDesc(pageable);
    }

    @Override
    public Page<ActivityLog> getLogsByAction(String action, Pageable pageable) {
        return activityLogRepository.findByActionOrderByTimestampDesc(action, pageable);
    }
}
