package com.laybhari.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class CartItemDto {
    private Long id;
    private Long productId;
    private String productName;
    private String productImageUrl;
    private Long productVariantId;
    private String weightLabel;
    private BigDecimal unitPrice;
    private Integer quantity;
    private BigDecimal lineTotal;
}
