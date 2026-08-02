package com.laybhari.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class OrderDto {
    private Long id;
    private Long userId;
    private String customerName;
    private String customerEmail;
    private AddressDto address;
    private String status;
    private BigDecimal subtotal;
    private BigDecimal shippingFee;
    private BigDecimal totalAmount;
    private String paymentStatus;
    private String razorpayOrderId;
    private String razorpayPaymentId;
    private String razorpaySignature;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<OrderItemDto> items = new ArrayList<>();
}
