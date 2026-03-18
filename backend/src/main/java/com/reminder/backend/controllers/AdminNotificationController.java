package com.reminder.backend.controllers;

import com.reminder.backend.admin.AdminAccessService;
import com.reminder.backend.admin.AdminBroadcastEmailRequest;
import com.reminder.backend.admin.AdminBroadcastEmailResult;
import com.reminder.backend.admin.AdminBroadcastEmailService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/notifications")
@CrossOrigin(origins = "http://localhost:3000")
@SecurityRequirement(name = "bearerAuth")
public class AdminNotificationController {

    private final AdminAccessService adminAccessService;
    private final AdminBroadcastEmailService adminBroadcastEmailService;

    public AdminNotificationController(
            AdminAccessService adminAccessService,
            AdminBroadcastEmailService adminBroadcastEmailService
    ) {
        this.adminAccessService = adminAccessService;
        this.adminBroadcastEmailService = adminBroadcastEmailService;
    }

    @PostMapping("/broadcast-email")
    public ResponseEntity<?> sendEmergencyEmail(@Valid @RequestBody AdminBroadcastEmailRequest request) {
        if (!isAdmin()) {
            return ResponseEntity.status(403).body("admin access required");
        }

        AdminBroadcastEmailResult result = adminBroadcastEmailService.broadcast(
                request.getSubject().trim(),
                request.getMessage().trim()
        );
        return ResponseEntity.accepted().body(result);
    }

    private boolean isAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication != null ? (String) authentication.getPrincipal() : null;
        return adminAccessService.isAdmin(email);
    }
}
