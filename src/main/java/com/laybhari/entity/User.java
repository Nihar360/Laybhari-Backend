package com.laybhari.entity;

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

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = true, unique = true, length = 150)
    private String email;

    @Column(nullable = true)
    private String password; // BCrypt hash (null for phone-only accounts)

    @Column(nullable = false, length = 20)
    private String role = "CUSTOMER"; // CUSTOMER | ADMIN

    @Column(nullable = true, unique = true, length = 20)
    private String phone;

    @Column(updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime updatedAt = LocalDateTime.now();
}