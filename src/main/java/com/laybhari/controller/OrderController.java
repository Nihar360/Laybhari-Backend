package com.laybhari.controller;

import com.laybhari.dto.CheckoutRequest;
import com.laybhari.dto.CreatePaymentResponse;
import com.laybhari.dto.OrderDto;
import com.laybhari.dto.VerifyPaymentRequest;
import com.laybhari.service.OrderService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    // POST /api/orders/checkout → body: { addressId }
    @PostMapping("/checkout")
    public ResponseEntity<OrderDto> checkout(Authentication authentication,
                                             @RequestBody CheckoutRequest request) {
        String email = authentication.getName();
        return ResponseEntity.status(HttpStatus.CREATED).body(orderService.checkout(email, request));
    }

    // POST /api/orders/{orderId}/create-payment → initiate Razorpay payment order
    @PostMapping("/{orderId}/create-payment")
    public ResponseEntity<CreatePaymentResponse> createPayment(Authentication authentication,
                                                                @PathVariable Long orderId) {
        String email = authentication.getName();
        return ResponseEntity.ok(orderService.createPayment(email, orderId));
    }

    // POST /api/orders/{orderId}/verify-payment → verify Razorpay signature and confirm order
    @PostMapping("/{orderId}/verify-payment")
    public ResponseEntity<OrderDto> verifyPayment(Authentication authentication,
                                                  @PathVariable Long orderId,
                                                  @RequestBody VerifyPaymentRequest request) {
        String email = authentication.getName();
        return ResponseEntity.ok(orderService.verifyPayment(email, orderId, request));
    }

    // GET /api/orders → list logged-in user's past orders (most recent first)
    @GetMapping
    public ResponseEntity<List<OrderDto>> getOrders(Authentication authentication) {
        String email = authentication.getName();
        return ResponseEntity.ok(orderService.getUserOrders(email));
    }

    // GET /api/orders/{id} → single order detail with all items — verify ownership
    @GetMapping("/{id}")
    public ResponseEntity<OrderDto> getOrderById(Authentication authentication,
                                                 @PathVariable Long id) {
        String email = authentication.getName();
        return ResponseEntity.ok(orderService.getOrderById(email, id));
    }
}
