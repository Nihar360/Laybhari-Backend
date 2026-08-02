package com.laybhari.dto;

import lombok.Data;

@Data
public class AddToCartRequest {
    private Long productVariantId;
    private Integer quantity;
}
