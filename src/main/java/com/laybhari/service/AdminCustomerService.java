package com.laybhari.service;

import com.laybhari.dto.CustomerDetailDto;
import com.laybhari.dto.CustomerDto;
import com.laybhari.dto.OrderDto;
import com.laybhari.entity.Order;
import com.laybhari.entity.User;
import com.laybhari.repository.OrderRepository;
import com.laybhari.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AdminCustomerService {

    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final OrderService orderService;

    public AdminCustomerService(UserRepository userRepository,
                                OrderRepository orderRepository,
                                OrderService orderService) {
        this.userRepository = userRepository;
        this.orderRepository = orderRepository;
        this.orderService = orderService;
    }

    @Transactional(readOnly = true)
    public Page<CustomerDto> getAllCustomers(Pageable pageable) {
        return userRepository.findByRoleOrderByCreatedAtDesc("CUSTOMER", pageable)
                .map(this::toCustomerDto);
    }

    @Transactional(readOnly = true)
    public CustomerDetailDto getCustomerById(Long customerId) {
        User user = userRepository.findById(customerId)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found with ID: " + customerId));

        if (!"CUSTOMER".equalsIgnoreCase(user.getRole())) {
            throw new IllegalArgumentException("User with ID " + customerId + " is not a CUSTOMER.");
        }

        CustomerDto customerDto = toCustomerDto(user);

        List<Order> orders = orderRepository.findByUserOrderByCreatedAtDesc(user);
        List<OrderDto> orderHistory = orders.stream()
                .map(orderService::toOrderDto)
                .collect(Collectors.toList());

        return CustomerDetailDto.builder()
                .customer(customerDto)
                .orderHistory(orderHistory)
                .build();
    }

    public CustomerDto toCustomerDto(User user) {
        long totalOrders = orderRepository.countByUser(user);
        BigDecimal totalSpent = orderRepository.sumTotalAmountByUserAndPaymentStatus(user, "PAID");
        if (totalSpent == null) {
            totalSpent = BigDecimal.ZERO;
        }

        return CustomerDto.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .createdAt(user.getCreatedAt())
                .totalOrders(totalOrders)
                .totalSpent(totalSpent)
                .build();
    }
}
