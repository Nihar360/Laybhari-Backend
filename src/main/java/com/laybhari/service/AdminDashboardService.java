package com.laybhari.service;

import com.laybhari.dto.AdminDashboardDto;
import com.laybhari.dto.AdminDashboardDto.RecentOrderSummary;
import com.laybhari.entity.Order;
import com.laybhari.repository.OrderRepository;
import com.laybhari.repository.ProductRepository;
import com.laybhari.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AdminDashboardService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    public AdminDashboardService(OrderRepository orderRepository,
                                 UserRepository userRepository,
                                 ProductRepository productRepository) {
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
    }

    @Transactional(readOnly = true)
    public AdminDashboardDto getDashboardMetrics() {
        long totalOrders = orderRepository.count();
        BigDecimal totalRevenue = orderRepository.sumTotalAmountByPaymentStatus("PAID");
        if (totalRevenue == null) {
            totalRevenue = BigDecimal.ZERO;
        }

        long totalCustomers = userRepository.countByRole("CUSTOMER");
        long totalProducts = productRepository.count();

        // Recent orders (last 10)
        List<Order> recentOrdersList = orderRepository.findTop10ByOrderByCreatedAtDesc();
        List<RecentOrderSummary> recentOrders = recentOrdersList.stream()
                .map(o -> RecentOrderSummary.builder()
                        .id(o.getId())
                        .customerName(o.getUser() != null ? o.getUser().getName() : "N/A")
                        .customerEmail(o.getUser() != null ? o.getUser().getEmail() : "N/A")
                        .totalAmount(o.getTotalAmount())
                        .status(o.getStatus())
                        .paymentStatus(o.getPaymentStatus())
                        .createdAt(o.getCreatedAt())
                        .build())
                .collect(Collectors.toList());

        // Orders by status
        Map<String, Long> ordersByStatus = new LinkedHashMap<>();
        // Initialize default statuses
        List.of("PLACED", "CONFIRMED", "SHIPPED", "DELIVERED", "CANCELLED")
                .forEach(status -> ordersByStatus.put(status, 0L));

        List<Object[]> statusCounts = orderRepository.countOrdersByStatus();
        for (Object[] row : statusCounts) {
            if (row != null && row.length >= 2 && row[0] != null && row[1] != null) {
                String status = row[0].toString();
                Long count = ((Number) row[1]).longValue();
                ordersByStatus.put(status, count);
            }
        }

        return AdminDashboardDto.builder()
                .totalOrders(totalOrders)
                .totalRevenue(totalRevenue)
                .totalCustomers(totalCustomers)
                .totalProducts(totalProducts)
                .recentOrders(recentOrders)
                .ordersByStatus(ordersByStatus)
                .build();
    }
}
