package com.laybhari.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryItemDto {
    private Long variantId;
    private Long productId;
    private String productName;
    private String categoryName;
    private String weightLabel;
    private BigDecimal price;
    private Integer stock;
    private Boolean isActive;
    private boolean lowStock;
}
