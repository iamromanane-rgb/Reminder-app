package com.reminder.backend.controllers;

import com.reminder.backend.admin.AdminAccessService;
import com.reminder.backend.models.Event;
import com.reminder.backend.models.User;
import com.reminder.backend.repositories.EventRepository;
import com.reminder.backend.repositories.UserRepository;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

import java.time.LocalDate;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/api/users/{userId}/events")
@SecurityRequirement(name = "bearerAuth")
public class EventController {

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AdminAccessService adminAccessService;

    @GetMapping
    public ResponseEntity<List<Event>> getAllEventsForUser(@PathVariable Long userId) {
        if (!isSelfOrAdmin(userId)) {
            return ResponseEntity.status(403).build();
        }
        if (!userRepository.existsById(userId)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(eventRepository.findByUserId(userId));
    }

    @PostMapping
    public ResponseEntity<?> createEvent(@PathVariable Long userId, @RequestBody Event event) { //Request body takes the json sent by user and converts it into java
        if (!isSelfOrAdmin(userId)) {
            return ResponseEntity.status(403).body("forbidden");
        }

        Optional<User> userOptional = userRepository.findById(userId);

        if (userOptional.isPresent()) {
            event.setUser(userOptional.get());
            Event savedEvent = eventRepository.save(event);
            return ResponseEntity.ok(savedEvent);
        } else {
            return ResponseEntity.badRequest().body("Error: User with ID " + userId + " not found.");
        }
    }

    @GetMapping("/{eventId}")
    public ResponseEntity<Event> getEventById(@PathVariable Long userId, @PathVariable Long eventId) {
        if (!isSelfOrAdmin(userId)) {
            return ResponseEntity.status(403).build();
        }
        return eventRepository.findByIdAndUserId(eventId, userId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }


    @PutMapping("/{eventId}")
    public ResponseEntity<Event> updateEvent(@PathVariable Long userId,
                                             @PathVariable Long eventId,
                                             @RequestBody Event eventDetails) {
        if (!isSelfOrAdmin(userId)) {
            return ResponseEntity.status(403).build();
        }

        return eventRepository.findByIdAndUserId(eventId, userId).map(existingEvent -> {

            if (eventDetails.getDescription() != null) existingEvent.setDescription(eventDetails.getDescription());
            if (eventDetails.getEventDate() != null) existingEvent.setEventDate(eventDetails.getEventDate());
            if (eventDetails.getEventType() != null) existingEvent.setEventType(eventDetails.getEventType());

            // Save the existing object
            Event updatedEvent = eventRepository.save(existingEvent);
            return ResponseEntity.ok(updatedEvent);

        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{eventId}")
    public ResponseEntity<?> deleteEvent(@PathVariable Long userId, @PathVariable Long eventId) {
        if (!isSelfOrAdmin(userId)) {
            return ResponseEntity.status(403).body("forbidden");
        }

        return eventRepository.findByIdAndUserId(eventId, userId).map(event -> {
            eventRepository.delete(event);
            return ResponseEntity.ok().build();
        }).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/search")
    public ResponseEntity<?> searchUserEvents(@PathVariable Long userId, @RequestParam String keyword) {//@RequestParam reads the ?keyword=... part of the url
        if (!isSelfOrAdmin(userId)) {
            return ResponseEntity.status(403).body("forbidden");
        }
        return ResponseEntity.ok(eventRepository.findByUserIdAndDescriptionContainingIgnoreCase(userId, keyword));
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
