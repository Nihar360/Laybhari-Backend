package com.laybhari.controller;

import com.laybhari.dto.CustomerDetailDto;
import com.laybhari.dto.CustomerDto;
import com.laybhari.service.AdminCustomerService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/customers")
public class AdminCustomerController {

    private final AdminCustomerService adminCustomerService;

    public AdminCustomerController(AdminCustomerService adminCustomerService) {
        this.adminCustomerService = adminCustomerService;
    }

    // GET /api/admin/customers?page=0&size=20 (ADMIN only)
    @GetMapping
    public ResponseEntity<Page<CustomerDto>> getAllCustomers(Pageable pageable) {
        return ResponseEntity.ok(adminCustomerService.getAllCustomers(pageable));
    }

    // GET /api/admin/customers/{id} (ADMIN only)
    @GetMapping("/{id}")
    public ResponseEntity<CustomerDetailDto> getCustomerById(@PathVariable Long id) {
        return ResponseEntity.ok(adminCustomerService.getCustomerById(id));
    }
}
