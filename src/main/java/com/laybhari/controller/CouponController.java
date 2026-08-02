package com.laybhari.controller;

import com.laybhari.dto.CouponValidateRequest;
import com.laybhari.dto.CouponValidateResponse;
import com.laybhari.service.CouponService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/coupons")
public class CouponController {

    private final CouponService couponService;

    public CouponController(CouponService couponService) {
        this.couponService = couponService;
    }

    // POST /api/coupons/validate (Authenticated users)
    @PostMapping("/validate")
    public ResponseEntity<CouponValidateResponse> validateCoupon(@RequestBody CouponValidateRequest request) {
        return ResponseEntity.ok(couponService.validateCoupon(request));
    }
}
