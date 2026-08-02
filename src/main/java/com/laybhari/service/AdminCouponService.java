package com.laybhari.service;

import com.laybhari.dto.CouponDto;
import com.laybhari.dto.CouponRequest;
import com.laybhari.entity.Coupon;
import com.laybhari.repository.CouponRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AdminCouponService {

    private final CouponRepository couponRepository;

    public AdminCouponService(CouponRepository couponRepository) {
        this.couponRepository = couponRepository;
    }

    @Transactional(readOnly = true)
    public List<CouponDto> getAllCoupons() {
        return couponRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(this::toCouponDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CouponDto getCouponById(Long id) {
        Coupon coupon = couponRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Coupon not found with ID: " + id));
        return toCouponDto(coupon);
    }

    @Transactional
    public CouponDto createCoupon(CouponRequest request) {
        validateCouponRequest(request, null);

        String upperCode = request.getCode().trim().toUpperCase();
        if (couponRepository.existsByCodeIgnoreCase(upperCode)) {
            throw new IllegalArgumentException("Coupon code '" + upperCode + "' already exists.");
        }

        Coupon coupon = new Coupon();
        coupon.setCode(upperCode);
        coupon.setDiscountType(request.getDiscountType().trim().toUpperCase());
        coupon.setDiscountValue(request.getDiscountValue());
        coupon.setMinOrderAmount(request.getMinOrderAmount());
        coupon.setIsActive(request.getIsActive() != null ? request.getIsActive() : true);
        coupon.setExpiresAt(request.getExpiresAt());
        coupon.setCreatedAt(LocalDateTime.now());

        Coupon saved = couponRepository.save(coupon);
        return toCouponDto(saved);
    }

    @Transactional
    public CouponDto updateCoupon(Long id, CouponRequest request) {
        validateCouponRequest(request, id);

        Coupon coupon = couponRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Coupon not found with ID: " + id));

        String upperCode = request.getCode().trim().toUpperCase();
        if (!coupon.getCode().equalsIgnoreCase(upperCode) && couponRepository.existsByCodeIgnoreCase(upperCode)) {
            throw new IllegalArgumentException("Coupon code '" + upperCode + "' is already in use by another coupon.");
        }

        coupon.setCode(upperCode);
        coupon.setDiscountType(request.getDiscountType().trim().toUpperCase());
        coupon.setDiscountValue(request.getDiscountValue());
        coupon.setMinOrderAmount(request.getMinOrderAmount());
        if (request.getIsActive() != null) {
            coupon.setIsActive(request.getIsActive());
        }
        coupon.setExpiresAt(request.getExpiresAt());

        Coupon saved = couponRepository.save(coupon);
        return toCouponDto(saved);
    }

    @Transactional
    public void deleteCoupon(Long id) {
        if (!couponRepository.existsById(id)) {
            throw new IllegalArgumentException("Coupon not found with ID: " + id);
        }
        couponRepository.deleteById(id);
    }

    private void validateCouponRequest(CouponRequest request, Long existingId) {
        if (request == null) {
            throw new IllegalArgumentException("Coupon request body is required.");
        }
        if (request.getCode() == null || request.getCode().trim().isEmpty()) {
            throw new IllegalArgumentException("Coupon code is required.");
        }
        if (request.getDiscountType() == null ||
                (! "PERCENTAGE".equalsIgnoreCase(request.getDiscountType().trim()) &&
                 ! "FLAT".equalsIgnoreCase(request.getDiscountType().trim()))) {
            throw new IllegalArgumentException("Discount type must be either 'PERCENTAGE' or 'FLAT'.");
        }
        if (request.getDiscountValue() == null || request.getDiscountValue().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Discount value must be greater than 0.");
        }
        if ("PERCENTAGE".equalsIgnoreCase(request.getDiscountType().trim()) &&
                request.getDiscountValue().compareTo(new BigDecimal("100")) > 0) {
            throw new IllegalArgumentException("Percentage discount cannot exceed 100%.");
        }
    }

    public CouponDto toCouponDto(Coupon coupon) {
        if (coupon == null) return null;
        return CouponDto.builder()
                .id(coupon.getId())
                .code(coupon.getCode())
                .discountType(coupon.getDiscountType())
                .discountValue(coupon.getDiscountValue())
                .minOrderAmount(coupon.getMinOrderAmount())
                .isActive(coupon.getIsActive())
                .createdAt(coupon.getCreatedAt())
                .expiresAt(coupon.getExpiresAt())
                .build();
    }
}
