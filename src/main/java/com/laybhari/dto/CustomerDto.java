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
public class CustomerDto {
    private Long id;
    private String name;
    private String email;
    private String phone;
    private LocalDateTime createdAt;
    private long totalOrders;
    private BigDecimal totalSpent;
}
