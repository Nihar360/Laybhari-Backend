package com.laybhari.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "otp_verifications")
@Data
public class OtpVerification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 15)
    private String phone;

    @Column(nullable = false, length = 6)
    private String otpCode;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    private boolean isVerified = false;

    @Column(updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
