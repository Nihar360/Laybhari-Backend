package com.laybhari.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class CouponValidateRequest {
    private String code;
    private BigDecimal orderAmount;
}
