package com.reminder.backend.controllers;

import com.reminder.backend.admin.AdminAccessService;
import com.reminder.backend.scheduler.DynamicSchedulerService;
import com.reminder.backend.scheduler.SchedulerRequest;
import com.reminder.backend.scheduler.SchedulerStatus;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/scheduler")
@CrossOrigin(origins = "http://localhost:3000")
@SecurityRequirement(name = "bearerAuth") // swagger-ui documentation annotation
public class SchedulerController {

    private final DynamicSchedulerService dynamicSchedulerService;
    private final AdminAccessService adminAccessService;

    public SchedulerController(DynamicSchedulerService dynamicSchedulerService, AdminAccessService adminAccessService) {
        this.dynamicSchedulerService = dynamicSchedulerService;
        this.adminAccessService = adminAccessService;
    }

    @GetMapping
    public SchedulerStatus getStatus() {
        if (!isAdmin()) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.FORBIDDEN, "admin access required");
        }
        return new SchedulerStatus(
                dynamicSchedulerService.getCurrentCron(), // 0 0 6 * * *
                dynamicSchedulerService.isScheduled() //boolean
        );
    }

    @PostMapping("/cron")
    public ResponseEntity<?> updateCron(@Valid @RequestBody SchedulerRequest request) {
        if (!isAdmin()) {
            return ResponseEntity.status(403).body("admin access required");
        }
        try {
            dynamicSchedulerService.updateCron(request.getCron().trim());
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body("invalid cron expression");
        }
        return ResponseEntity.ok(new SchedulerStatus(
                dynamicSchedulerService.getCurrentCron(),
                dynamicSchedulerService.isScheduled()
        ));
    }

    @PostMapping("/run")
    public ResponseEntity<?> runNow() {
        if (!isAdmin()) {
            return ResponseEntity.status(403).body("admin access required");
        }
        dynamicSchedulerService.runNow();
        return ResponseEntity.accepted().body("scheduler triggered");
    }

    private boolean isAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication != null ? (String) authentication.getPrincipal() : null;
        return adminAccessService.isAdmin(email);
    }
}
