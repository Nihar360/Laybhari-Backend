package com.laybhari.controller;

import com.laybhari.dto.OrderDto;
import com.laybhari.service.OrderService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/orders")
public class AdminOrderController {

    private final OrderService orderService;

    public AdminOrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    // GET /api/admin/orders?page=0&size=20&status=SHIPPED (ADMIN only)
    @GetMapping
    public ResponseEntity<Page<OrderDto>> getAllOrders(
            @RequestParam(name = "status", required = false) String status,
            Pageable pageable) {
        return ResponseEntity.ok(orderService.getAllOrdersAdmin(status, pageable));
    }

    // GET /api/admin/orders/{id} (ADMIN only)
    @GetMapping("/{id}")
    public ResponseEntity<OrderDto> getOrderById(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.getOrderByIdAdmin(id));
    }

    // PUT /api/admin/orders/{id}/status (ADMIN only) body: { "status": "SHIPPED" }
    @PutMapping("/{id}/status")
    public ResponseEntity<OrderDto> updateOrderStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        String newStatus = body != null ? body.get("status") : null;
        return ResponseEntity.ok(orderService.updateOrderStatusAdmin(id, newStatus));
    }
}
