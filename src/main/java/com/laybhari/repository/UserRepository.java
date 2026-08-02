package com.laybhari.repository;

import com.laybhari.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Optional<User> findByPhone(String phone);
    boolean existsByEmail(String email);
    boolean existsByPhone(String phone);
    long countByRole(String role);
    Page<User> findByRoleOrderByCreatedAtDesc(String role, Pageable pageable);
}
