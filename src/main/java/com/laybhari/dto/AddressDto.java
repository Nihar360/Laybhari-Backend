package com.laybhari.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AddressDto {
    private Long id;
    private Long userId;
    private String fullName;
    private String phone;
    private String line1;
    private String line2;
    private String city;
    private String state;
    private String pincode;
    private Boolean isDefault;
    private LocalDateTime createdAt;
}
