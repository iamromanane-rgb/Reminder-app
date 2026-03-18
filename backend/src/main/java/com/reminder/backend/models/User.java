package com.reminder.backend.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Data

public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false,unique = true)
    private String email;

    @Column(nullable = false)
    private String username;

    @Column(nullable = false)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String passwordHash;

    @Column(unique= true, nullable=false)
    private Long empId;

    @Enumerated(EnumType.STRING)
    @Column
    private AccessLevel accessLevel = AccessLevel.READ;

    private LocalDateTime createdAt = LocalDateTime.now();


}
