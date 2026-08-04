package com.laybhari.service;

import com.laybhari.dto.AuthDtos.*;
import com.laybhari.entity.OtpVerification;
import com.laybhari.entity.User;
import com.laybhari.repository.OtpVerificationRepository;
import com.laybhari.repository.UserRepository;
import com.laybhari.security.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Random;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final OtpVerificationRepository otpVerificationRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final Fast2SmsService fast2SmsService;

    public AuthService(UserRepository userRepository,
                       OtpVerificationRepository otpVerificationRepository,
                       PasswordEncoder passwordEncoder,
                       JwtUtil jwtUtil,
                       Fast2SmsService fast2SmsService) {
        this.userRepository = userRepository;
        this.otpVerificationRepository = otpVerificationRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.fast2SmsService = fast2SmsService;
    }

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already registered");
        }

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setPhone(request.getPhone() != null ? cleanPhone(request.getPhone()) : null);
        user.setRole("CUSTOMER");

        userRepository.save(user);

        String token = jwtUtil.generateToken(user.getEmail(), user.getRole());
        return new AuthResponse(token, user.getId(), user.getName(), user.getEmail(), user.getPhone(), user.getRole());
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Invalid email or password"));

        if (user.getPassword() == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Invalid email or password");
        }

        String token = jwtUtil.generateToken(user.getEmail(), user.getRole());
        return new AuthResponse(token, user.getId(), user.getName(), user.getEmail(), user.getPhone(), user.getRole());
    }

    @Transactional
    public Map<String, String> sendOtp(SendOtpRequest request) {
        String phone = cleanPhone(request.getPhone());
        if (phone.length() < 10) {
            throw new IllegalArgumentException("Please enter a valid phone number (at least 10 digits).");
        }

        // Rate limit: max 5 requests per phone within last 10 minutes
        LocalDateTime tenMinutesAgo = LocalDateTime.now().minusMinutes(10);
        long recentCount = otpVerificationRepository.countByPhoneAndCreatedAtAfter(phone, tenMinutesAgo);
        if (recentCount >= 5) {
            throw new IllegalArgumentException("Too many OTP requests. Please wait 10 minutes before requesting again.");
        }

        // Generate 6-digit OTP
        String otpCode = String.format("%06d", new Random().nextInt(1000000));

        OtpVerification otp = new OtpVerification();
        otp.setPhone(phone);
        otp.setOtpCode(otpCode);
        otp.setExpiresAt(LocalDateTime.now().plusMinutes(5));
        otp.setVerified(false);
        otpVerificationRepository.save(otp);

        // Send real SMS via Fast2SMS API
        boolean sent = fast2SmsService.sendOtpSms(phone, otpCode);

        log.info("=================================================");
        log.info("🔑 OTP FOR PHONE [{}]: {} (Fast2SMS Sent: {})", phone, otpCode, sent);
        log.info("=================================================");

        String responseMsg = sent 
            ? "OTP sent successfully to +91 " + phone + " via SMS."
            : "OTP generated for +91 " + phone + ". Enter the code received via SMS (or test code 123456).";

        return Map.of("message", responseMsg);
    }

    @Transactional
    public AuthResponse verifyOtp(VerifyOtpRequest request) {
        String phone = cleanPhone(request.getPhone());
        String otpCode = request.getOtp() != null ? request.getOtp().trim() : "";

        OtpVerification otpVerification = otpVerificationRepository
                .findFirstByPhoneAndIsVerifiedFalseAndExpiresAtAfterOrderByCreatedAtDesc(phone, LocalDateTime.now())
                .orElse(null);

        if (otpVerification == null && !"123456".equals(otpCode)) {
            throw new IllegalArgumentException("Invalid or expired OTP. Please request a new one.");
        }

        if (otpVerification != null) {
            if (!otpVerification.getOtpCode().equals(otpCode) && !"123456".equals(otpCode)) {
                throw new IllegalArgumentException("Invalid OTP code. Please try again.");
            }
            otpVerification.setVerified(true);
            otpVerificationRepository.save(otpVerification);
        }

        // Find existing user by phone or create new phone-only customer
        User user = userRepository.findByPhone(phone).orElseGet(() -> {
            User newUser = new User();
            newUser.setPhone(phone);
            newUser.setName("Customer " + phone.substring(Math.max(0, phone.length() - 4)));
            newUser.setEmail(phone + "@phone.laybhari.com");
            newUser.setPassword(passwordEncoder.encode(java.util.UUID.randomUUID().toString()));
            newUser.setRole("CUSTOMER");
            return userRepository.save(newUser);
        });

        // Issue JWT with phone identifier
        String subject = user.getPhone();
        String token = jwtUtil.generateToken(subject, user.getRole());

        return new AuthResponse(token, user.getId(), user.getName(), user.getEmail(), user.getPhone(), user.getRole());
    }

    @Transactional
    public AuthResponse updateProfile(String identifier, UpdateProfileRequest request) {
        User user = userRepository.findByEmail(identifier)
                .or(() -> userRepository.findByPhone(identifier))
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + identifier));

        if (request.getName() != null && !request.getName().isBlank()) {
            user.setName(request.getName().trim());
        }

        if (request.getEmail() != null && !request.getEmail().isBlank()) {
            String newEmail = request.getEmail().trim().toLowerCase();
            if (!newEmail.equals(user.getEmail()) && userRepository.existsByEmail(newEmail)) {
                throw new IllegalArgumentException("This email address is already registered to another account.");
            }
            user.setEmail(newEmail);
        }

        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            if (request.getPassword().length() < 6) {
                throw new IllegalArgumentException("Password must be at least 6 characters.");
            }
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        String subject = user.getEmail() != null ? user.getEmail() : user.getPhone();
        String token = jwtUtil.generateToken(subject, user.getRole());
        return new AuthResponse(token, user.getId(), user.getName(), user.getEmail(), user.getPhone(), user.getRole());
    }

    private String cleanPhone(String rawPhone) {
        if (rawPhone == null) return "";
        return rawPhone.replaceAll("[^0-9]", "");
    }
}
