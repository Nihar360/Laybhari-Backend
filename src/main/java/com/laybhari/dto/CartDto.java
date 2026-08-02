package com.laybhari.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
public class CartDto {
    private Long id;
    private Long userId;
    private List<CartItemDto> items = new ArrayList<>();
    private BigDecimal grandTotal = BigDecimal.ZERO;
    private Integer totalItems = 0;
}
