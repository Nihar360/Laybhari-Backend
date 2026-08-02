package com.laybhari.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CouponDto {
    private Long id;
    private String code;
    private String discountType; // PERCENTAGE | FLAT
    private BigDecimal discountValue;
    private BigDecimal minOrderAmount;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
}
