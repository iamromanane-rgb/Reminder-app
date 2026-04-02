package com.reminder.backend.controllers;

import com.reminder.backend.admin.AdminAccessService;
import com.reminder.backend.admin.AdminUserUpdateRequest;
import com.reminder.backend.models.AccessLevel;
import com.reminder.backend.models.User;
import com.reminder.backend.repositories.UserRepository;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/admin/users")
@CrossOrigin(origins = "http://localhost:3000")
@SecurityRequirement(name = "bearerAuth")
public class AdminUserController {

    private final UserRepository userRepository;
    private final AdminAccessService adminAccessService;

    public AdminUserController(UserRepository userRepository, AdminAccessService adminAccessService) {
        this.userRepository = userRepository;
        this.adminAccessService = adminAccessService;

    }

    @GetMapping("/search/empid")
    public ResponseEntity<?> getUserByEmpId(@RequestParam Long empId) {
        if (!isAdmin()) {
            return ResponseEntity.status(403).body("admin access required");
        }
        return userRepository.findByEmpId(empId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/empid/{empId}")
    public ResponseEntity<?> updateUser(@PathVariable Long empId, @Valid @RequestBody AdminUserUpdateRequest request) {
        if (!isAdmin()) {
            return ResponseEntity.status(403).body("admin access required");
        }
        return userRepository.findByEmpId(empId).map(existingUser -> {
            if (request.getEmail() != null && !request.getEmail().isBlank()) {
                if (!request.getEmail().equals(existingUser.getEmail())
                        && userRepository.existsByEmail(request.getEmail())) {
                    return ResponseEntity.status(409).body("email already exists");
                }
                existingUser.setEmail(request.getEmail());
            }
            if (request.getUsername() != null && !request.getUsername().isBlank()) {
                existingUser.setUsername(request.getUsername());
            }
            if (request.getEmpId() != null) {
                if (!request.getEmpId().equals(existingUser.getEmpId())
                        && userRepository.existsByEmpId(request.getEmpId())) {
                    return ResponseEntity.status(409).body("empId already exists");
                }
                existingUser.setEmpId(request.getEmpId());
            }
            if (request.getAccessLevel() != null && !request.getAccessLevel().isBlank()) {
                try {
                    existingUser.setAccessLevel(AccessLevel.fromInput(request.getAccessLevel()));
                } catch (IllegalArgumentException ex) {
                    return ResponseEntity.badRequest().body("invalid access level; use read or read_write");
                }
            }

            User updatedUser = userRepository.save(existingUser);
            return ResponseEntity.ok(updatedUser);
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/empid/{empId}")
    public ResponseEntity<?> deleteUser(@PathVariable Long empId) {
        if (!isAdmin()) {
            return ResponseEntity.status(403).body("admin access required");
        }
        Optional<User> userOpt = userRepository.findByEmpId(empId);
        if (userOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        userRepository.delete(userOpt.get());
        return ResponseEntity.ok().build();
    }

    private boolean isAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication(); // get current user's authentication
        String email = authentication != null ? (String) authentication.getPrincipal() : null; //getprincipal returns email in our security setup
        return adminAccessService.isAdmin(email);
    }
}
