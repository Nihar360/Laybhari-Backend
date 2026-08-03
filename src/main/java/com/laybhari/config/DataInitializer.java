package com.laybhari.config;

import com.laybhari.entity.User;
import com.laybhari.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    @Bean
    public CommandLineRunner initAdminUser(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            String adminEmail = "admin@laybhari.com";
            if (!userRepository.existsByEmail(adminEmail)) {
                User admin = new User();
                admin.setName("Laybhari Admin");
                admin.setEmail(adminEmail);
                admin.setPassword(passwordEncoder.encode("Admin@123"));
                admin.setRole("ADMIN");
                admin.setPhone("9999999999");
                userRepository.save(admin);

                log.info("=================================================");
                log.info("🔑 INITIAL ADMIN ACCOUNT CREATED SUCCESSFULLY");
                log.info("Username/Email: {}", adminEmail);
                log.info("Password: Admin@123");
                log.info("Role: ADMIN");
                log.info("=================================================");
            } else {
                log.info("Admin user [{}] already exists.", adminEmail);
            }
        };
    }
}
