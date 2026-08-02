package com.laybhari.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class ProductVariantDto {
    private Long id;
    private String weightLabel;
    private BigDecimal price;
    private Integer stock;
    private Boolean isActive;
}
