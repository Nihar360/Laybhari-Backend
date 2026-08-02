package com.laybhari.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderItemDto {
    private Long id;
    private Long productVariantId;
    private String productName;
    private String weightLabel;
    private BigDecimal price;
    private Integer quantity;
    private BigDecimal lineTotal;
}
