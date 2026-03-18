package com.reminder.backend.repositories;

import com.reminder.backend.models.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {

    List<Event> findByUserId(Long userId);

    Optional<Event> findByIdAndUserId(Long id, Long userId);

    List<Event> findByUserIdAndEventDateBetween(Long userId, LocalDate startDate, LocalDate endDate);

    List<Event> findByUserIdAndDescriptionContainingIgnoreCase(Long userId, String keyword);

    @Query("SELECT e FROM Event e WHERE EXTRACT(MONTH FROM e.eventDate) = :month AND EXTRACT(DAY FROM e.eventDate) = :day")
    List<Event> findByMonthAndDay(@Param("month") int month, @Param("day") int day);
}