package com.laybhari.repository;

import com.laybhari.entity.OtpVerification;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.Optional;

public interface OtpVerificationRepository extends JpaRepository<OtpVerification, Long> {
    long countByPhoneAndCreatedAtAfter(String phone, LocalDateTime after);

    Optional<OtpVerification> findFirstByPhoneAndIsVerifiedFalseAndExpiresAtAfterOrderByCreatedAtDesc(
            String phone, LocalDateTime now
    );
}
