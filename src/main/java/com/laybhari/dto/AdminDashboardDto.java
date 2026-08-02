package com.laybhari.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminDashboardDto {
    private long totalOrders;
    private BigDecimal totalRevenue;
    private long totalCustomers;
    private long totalProducts;
    private List<RecentOrderSummary> recentOrders;
    private Map<String, Long> ordersByStatus;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecentOrderSummary {
        private Long id;
        private String customerName;
        private String customerEmail;
        private BigDecimal totalAmount;
        private String status;
        private String paymentStatus;
        private LocalDateTime createdAt;
    }
}
