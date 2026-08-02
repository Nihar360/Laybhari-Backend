package com.laybhari.controller;

import com.laybhari.dto.CouponDto;
import com.laybhari.dto.CouponRequest;
import com.laybhari.service.AdminCouponService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/coupons")
public class AdminCouponController {

    private final AdminCouponService adminCouponService;

    public AdminCouponController(AdminCouponService adminCouponService) {
        this.adminCouponService = adminCouponService;
    }

    // GET /api/admin/coupons (ADMIN only)
    @GetMapping
    public ResponseEntity<List<CouponDto>> getAllCoupons() {
        return ResponseEntity.ok(adminCouponService.getAllCoupons());
    }

    // GET /api/admin/coupons/{id} (ADMIN only)
    @GetMapping("/{id}")
    public ResponseEntity<CouponDto> getCouponById(@PathVariable Long id) {
        return ResponseEntity.ok(adminCouponService.getCouponById(id));
    }

    // POST /api/admin/coupons (ADMIN only)
    @PostMapping
    public ResponseEntity<CouponDto> createCoupon(@RequestBody CouponRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(adminCouponService.createCoupon(request));
    }

    // PUT /api/admin/coupons/{id} (ADMIN only)
    @PutMapping("/{id}")
    public ResponseEntity<CouponDto> updateCoupon(@PathVariable Long id, @RequestBody CouponRequest request) {
        return ResponseEntity.ok(adminCouponService.updateCoupon(id, request));
    }

    // DELETE /api/admin/coupons/{id} (ADMIN only)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCoupon(@PathVariable Long id) {
        adminCouponService.deleteCoupon(id);
        return ResponseEntity.noContent().build();
    }
}
