package com.reminder.backend.controllers;

import com.reminder.backend.auth.JwtService;
import com.reminder.backend.admin.AdminAccessService;
import com.reminder.backend.login.LoginResponse;
import com.reminder.backend.models.AccessLevel;
import com.reminder.backend.models.User;
import com.reminder.backend.repositories.UserRepository;
import com.reminder.backend.users.CreateUserRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import com.reminder.backend.login.LoginRequest;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "http://localhost:3000") // Allow React/Node to access
public class UserController {

    @Autowired //Dependency injection
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private AdminAccessService adminAccessService;

    @PostMapping
    public ResponseEntity<?> createUser(@Valid @RequestBody CreateUserRequest request) { //CreateUserRequest is a DTO that contains the fields needed to create a user
        if (userRepository.existsByEmail(request.getEmail())) {
            return ResponseEntity.status(409).body("email already exists");
        }
        if (userRepository.existsByEmpId(request.getEmpId())) {
            return ResponseEntity.status(409).body("Employee Id already exists");
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setUsername(request.getUsername());
        user.setEmpId(request.getEmpId());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));

        User savedUser = userRepository.save(user);
        return ResponseEntity.ok(savedUser);
    }

    @GetMapping
    public ResponseEntity<?> getAllUsers() {
        if (!isAdmin()) {
            return ResponseEntity.status(403).body("admin access required");
        }
        return ResponseEntity.ok(userRepository.findAll()); // Returns array of users
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@Valid @RequestBody LoginRequest loginRequest) { //LoginRequest is a DTO that contains email and password
        Optional<User> userOpt = userRepository.findByEmail(loginRequest.getEmail());

        if (userOpt.isPresent()) {
            User user = userOpt.get();

            if (passwordEncoder.matches(loginRequest.getPassword(), user.getPasswordHash())) {
                String token = jwtService.generateToken(user);
                LoginResponse response = new LoginResponse(
                        token,
                        user.getId(),
                        user.getEmail(),
                        user.getUsername(),
                        user.getEmpId(),
                        (user.getAccessLevel() == null ? AccessLevel.READ : user.getAccessLevel()).getClaimValue()
                );
                return ResponseEntity.ok(response);
            }
        }


        return ResponseEntity.status(401).body("Invalid Email or Password");
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        if (!isSelfOrAdmin(id)) {
            return ResponseEntity.status(403).build();
        }
        return userRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }


    @GetMapping("/search/email")
    public ResponseEntity<User> getUserByEmail(@RequestParam String email) {
        if (!isAdmin()) {
            return ResponseEntity.status(403).build();
        }
        return userRepository.findByEmail(email)//returns array of users with matching email
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/search/name")
    public ResponseEntity<?> searchUsersByName(@RequestParam String name) {
        if (!isAdmin()) {
            return ResponseEntity.status(403).body("admin access required");
        }
        return ResponseEntity.ok(userRepository.findByUsernameContainingIgnoreCase(name)); // returns array of users
    }

    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable Long id, @RequestBody User userDetails) {
        if (!isSelfOrAdmin(id)) {
            return ResponseEntity.status(403).build();
        }
        return userRepository.findById(id).map(existingUser -> {
            if (userDetails.getUsername() != null) existingUser.setUsername(userDetails.getUsername());
            if (userDetails.getEmail() != null) existingUser.setEmail(userDetails.getEmail());

            User updatedUser = userRepository.save(existingUser);
            return ResponseEntity.ok(updatedUser);
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Object> deleteUser(@PathVariable Long id) {
        if (!isSelfOrAdmin(id)) {
            return ResponseEntity.status(403).build();
        }
        return userRepository.findById(id).map(user -> {
            userRepository.delete(user);
            return ResponseEntity.ok().build();
        }).orElse(ResponseEntity.notFound().build());
    }

    private boolean isSelfOrAdmin(Long userId) {
        if (isAdmin()) {
            return true;
        }
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication != null ? (String) authentication.getPrincipal() : null;
        if (email == null) {
            return false;
        }
        return userRepository.findById(userId)
                .map(user -> email.equalsIgnoreCase(user.getEmail()))
                .orElse(false);
    }

    private boolean isAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication != null ? (String) authentication.getPrincipal() : null;
        return adminAccessService.isAdmin(email);
    }
}
