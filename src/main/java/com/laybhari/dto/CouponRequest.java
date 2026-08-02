package com.laybhari.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class CouponRequest {
    private String code;
    private String discountType; // PERCENTAGE | FLAT
    private BigDecimal discountValue;
    private BigDecimal minOrderAmount;
    private Boolean isActive = true;
    private LocalDateTime expiresAt;
}
