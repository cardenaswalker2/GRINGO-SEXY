package com.gringosexy.service;

import com.gringosexy.model.ActivityLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ActivityLogService {

    void log(String userId, String username, String action, String description, String ipAddress);

    List<ActivityLog> getRecentLogs();

    Page<ActivityLog> getLogs(Pageable pageable);

    Page<ActivityLog> getLogsByAction(String action, Pageable pageable);
}
