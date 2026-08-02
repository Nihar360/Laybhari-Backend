package com.laybhari.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

public class AuthDtos {

    @Data
    public static class RegisterRequest {
        @NotBlank
        private String name;

        @NotBlank @Email
        private String email;

        @NotBlank @Size(min = 6, message = "Password must be at least 6 characters")
        private String password;

        private String phone;
    }

    @Data
    public static class LoginRequest {
        @NotBlank @Email
        private String email;

        @NotBlank
        private String password;
    }

    @Data
    public static class SendOtpRequest {
        @NotBlank(message = "Phone number is required")
        private String phone;
    }

    @Data
    public static class VerifyOtpRequest {
        @NotBlank(message = "Phone number is required")
        private String phone;

        @NotBlank(message = "OTP code is required")
        private String otp;
    }

    @Data
    public static class UpdateProfileRequest {
        private String name;
        private String email;
        private String password;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AuthResponse {
        private String token;
        private Long userId;
        private String name;
        private String email;
        private String phone;
        private String role;

        public AuthResponse(String token, Long userId, String name, String email, String role) {
            this.token = token;
            this.userId = userId;
            this.name = name;
            this.email = email;
            this.phone = null;
            this.role = role;
        }
    }
}
