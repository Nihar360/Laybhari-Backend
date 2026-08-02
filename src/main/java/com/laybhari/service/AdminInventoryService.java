package com.laybhari.service;

import com.laybhari.dto.InventoryItemDto;
import com.laybhari.entity.Product;
import com.laybhari.entity.ProductVariant;
import com.laybhari.repository.ProductVariantRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class AdminInventoryService {

    private static final int LOW_STOCK_THRESHOLD = 10;

    private final ProductVariantRepository productVariantRepository;

    public AdminInventoryService(ProductVariantRepository productVariantRepository) {
        this.productVariantRepository = productVariantRepository;
    }

    @Transactional(readOnly = true)
    public Page<InventoryItemDto> getInventory(Boolean lowStockOnly, Pageable pageable) {
        if (Boolean.TRUE.equals(lowStockOnly)) {
            return productVariantRepository.findByStockLessThanOrderByStockAsc(LOW_STOCK_THRESHOLD, pageable)
                    .map(this::toInventoryItemDto);
        }
        return productVariantRepository.findAllByOrderByProduct_NameAscWeightLabelAsc(pageable)
                .map(this::toInventoryItemDto);
    }

    @Transactional
    public InventoryItemDto updateVariantStock(Long variantId, Integer newStock) {
        if (newStock == null || newStock < 0) {
            throw new IllegalArgumentException("Stock quantity must be non-negative.");
        }

        ProductVariant variant = productVariantRepository.findById(variantId)
                .orElseThrow(() -> new IllegalArgumentException("Product variant not found with ID: " + variantId));

        variant.setStock(newStock);
        variant.setUpdatedAt(LocalDateTime.now());
        ProductVariant saved = productVariantRepository.save(variant);

        return toInventoryItemDto(saved);
    }

    public InventoryItemDto toInventoryItemDto(ProductVariant variant) {
        Product product = variant.getProduct();
        String productName = product != null ? product.getName() : "Unknown Product";
        String categoryName = (product != null && product.getCategory() != null)
                ? product.getCategory().getName()
                : "Uncategorized";

        int currentStock = variant.getStock() != null ? variant.getStock() : 0;

        return InventoryItemDto.builder()
                .variantId(variant.getId())
                .productId(product != null ? product.getId() : null)
                .productName(productName)
                .categoryName(categoryName)
                .weightLabel(variant.getWeightLabel())
                .price(variant.getPrice())
                .stock(currentStock)
                .isActive(variant.getIsActive())
                .lowStock(currentStock < LOW_STOCK_THRESHOLD)
                .build();
    }
}
