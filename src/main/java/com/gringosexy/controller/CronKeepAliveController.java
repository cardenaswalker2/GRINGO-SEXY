package com.gringosexy.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Dedicated, lightweight RestController for external scheduled tasks / health checks (e.g. cron-job.org).
 * Designed to keep Render instances warm without authenticating or touching private user data.
 */
@RestController
@RequestMapping("/api/cron")
public class CronKeepAliveController {

    private static final Logger logger = LoggerFactory.getLogger(CronKeepAliveController.class);

    @GetMapping(value = "/keep-alive", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> keepAlive() {
        try {
            logger.info("Cron keep-alive ping received successfully at {}", Instant.now());

            Map<String, Object> response = new LinkedHashMap<>();
            response.put("status", "UP");
            response.put("service", "GRINGO SEXY");
            response.put("timestamp", Instant.now().toString());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error processing cron keep-alive ping: {}", e.getMessage());
            Map<String, Object> errorResponse = new LinkedHashMap<>();
            errorResponse.put("status", "DOWN");
            errorResponse.put("service", "GRINGO SEXY");
            errorResponse.put("error", "Internal ping processing error");
            errorResponse.put("timestamp", Instant.now().toString());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
}
