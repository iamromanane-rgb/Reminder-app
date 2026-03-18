package com.reminder.backend.models;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;

@Entity
@Table(name = "events")
@Data

public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long Id;

    @Column(nullable = false)
    private String eventType;

    @Column(nullable = false)
    private LocalDate eventDate;

    private String description;

    @ManyToOne
    @JoinColumn(name ="user_id",nullable =false)
    private User user;


}
