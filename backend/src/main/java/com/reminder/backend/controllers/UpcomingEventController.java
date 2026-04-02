package com.reminder.backend.controllers;

import com.reminder.backend.models.Event;
import com.reminder.backend.repositories.EventRepository;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/upcoming")
@CrossOrigin(origins = "http://localhost:3000")
@SecurityRequirement(name = "bearerAuth")
public class UpcomingEventController {

    @Autowired
    private EventRepository eventRepository;

    @GetMapping
    public List<Event> getUpcomingEvents(
            @RequestParam(value = "days", defaultValue = "7") int days
    ) {
        List<Event> allEvents = eventRepository.findAll();

        LocalDate today = LocalDate.now();
        LocalDate endDate = today.plusDays(days);

        return allEvents.stream()
                .filter(event -> {

                    LocalDate eventDate = event.getEventDate();
                    LocalDate currentYearDate = eventDate.withYear(today.getYear());

                    // Edge Case: If today is Dec 30 and event is Jan 1,
                    // 'currentYearDate' will be Jan 1, 2026 (Past), but we want Jan 1, 2027 (Future).
                    if (currentYearDate.isBefore(today) || currentYearDate.equals(today.minusDays(1))) {// Handle edge case for year-end events
                        currentYearDate = currentYearDate.plusYears(1);
                    }

                    // Check if the adjusted date falls within the specified time range
                    return !currentYearDate.isBefore(today) && !currentYearDate.isAfter(endDate);
                })

                .sorted(Comparator.comparing(e -> {
                    LocalDate d = e.getEventDate().withYear(today.getYear());
                    if (d.isBefore(today)) d = d.plusYears(1);
                    return d;
                }))
                .collect(Collectors.toList());
    }
}
