package com.laybhari.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
public class ProductDto {
    private Long id;
    private String name;
    private String description;
    private String imageUrl;
    private List<String> imageUrls = new ArrayList<>();
    private Boolean isActive;
    private Long categoryId;
    private String categoryName;
    private List<ProductVariantDto> variants = new ArrayList<>();

    // Backward compatibility helper getters for frontend root card views
    public BigDecimal getPrice() {
        if (variants != null && !variants.isEmpty()) {
            return variants.get(0).getPrice();
        }
        return BigDecimal.ZERO;
    }

    public Integer getStock() {
        if (variants != null && !variants.isEmpty()) {
            return variants.get(0).getStock();
        }
        return 0;
    }

    @Data
    public static class ProductRequest {
        private String name;
        private String description;
        private String imageUrl;
        private List<String> imageUrls = new ArrayList<>();
        private Long categoryId;
        private List<ProductVariantDto> variants = new ArrayList<>();
    }
}
