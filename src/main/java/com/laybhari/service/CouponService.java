package com.laybhari.service;

import com.laybhari.dto.CouponValidateRequest;
import com.laybhari.dto.CouponValidateResponse;
import com.laybhari.entity.Coupon;
import com.laybhari.repository.CouponRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class CouponService {

    private final CouponRepository couponRepository;

    public CouponService(CouponRepository couponRepository) {
        this.couponRepository = couponRepository;
    }

    @Transactional(readOnly = true)
    public CouponValidateResponse validateCoupon(CouponValidateRequest request) {
        if (request == null || request.getCode() == null || request.getCode().trim().isEmpty()) {
            return CouponValidateResponse.builder()
                    .valid(false)
                    .discountAmount(BigDecimal.ZERO)
                    .message("Coupon code is required.")
                    .build();
        }

        BigDecimal orderAmount = request.getOrderAmount() != null ? request.getOrderAmount() : BigDecimal.ZERO;
        String code = request.getCode().trim();

        Optional<Coupon> couponOpt = couponRepository.findByCodeIgnoreCase(code);
        if (couponOpt.isEmpty()) {
            return CouponValidateResponse.builder()
                    .valid(false)
                    .discountAmount(BigDecimal.ZERO)
                    .message("Invalid coupon code '" + code + "'.")
                    .build();
        }

        Coupon coupon = couponOpt.get();

        if (!Boolean.TRUE.equals(coupon.getIsActive())) {
            return CouponValidateResponse.builder()
                    .valid(false)
                    .discountAmount(BigDecimal.ZERO)
                    .message("Coupon '" + coupon.getCode() + "' is currently inactive.")
                    .build();
        }

        if (coupon.getExpiresAt() != null && coupon.getExpiresAt().isBefore(LocalDateTime.now())) {
            return CouponValidateResponse.builder()
                    .valid(false)
                    .discountAmount(BigDecimal.ZERO)
                    .message("Coupon '" + coupon.getCode() + "' has expired.")
                    .build();
        }

        if (coupon.getMinOrderAmount() != null && orderAmount.compareTo(coupon.getMinOrderAmount()) < 0) {
            return CouponValidateResponse.builder()
                    .valid(false)
                    .discountAmount(BigDecimal.ZERO)
                    .message("Minimum order amount of ₹" + coupon.getMinOrderAmount() + " is required to apply coupon '" + coupon.getCode() + "'.")
                    .build();
        }

        BigDecimal discountAmount;
        if ("PERCENTAGE".equalsIgnoreCase(coupon.getDiscountType())) {
            discountAmount = orderAmount.multiply(coupon.getDiscountValue())
                    .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
            if (discountAmount.compareTo(orderAmount) > 0) {
                discountAmount = orderAmount;
            }
        } else {
            // FLAT discount
            discountAmount = coupon.getDiscountValue().min(orderAmount);
        }

        return CouponValidateResponse.builder()
                .valid(true)
                .discountAmount(discountAmount)
                .message("Coupon '" + coupon.getCode() + "' applied successfully!")
                .build();
    }
}
