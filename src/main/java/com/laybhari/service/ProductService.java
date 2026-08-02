package com.laybhari.service;

import com.laybhari.dto.ProductDto;
import com.laybhari.dto.ProductDto.ProductRequest;
import com.laybhari.dto.ProductVariantDto;
import com.laybhari.entity.Category;
import com.laybhari.entity.Product;
import com.laybhari.entity.ProductVariant;
import com.laybhari.repository.CategoryRepository;
import com.laybhari.repository.ProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    public ProductService(ProductRepository productRepository, CategoryRepository categoryRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
    }

    @Transactional(readOnly = true)
    public Page<ProductDto> getAllProducts(Pageable pageable) {
        return productRepository.findByIsActiveTrue(pageable).map(this::toDto);
    }

    @Transactional(readOnly = true)
    public Page<ProductDto> getAllProductsAdmin(Pageable pageable) {
        return productRepository.findAll(pageable).map(this::toDtoAdmin);
    }

    @Transactional(readOnly = true)
    public Page<ProductDto> getByCategory(Long categoryId, Pageable pageable) {
        return productRepository.findByCategoryIdAndIsActiveTrue(categoryId, pageable).map(this::toDto);
    }

    @Transactional(readOnly = true)
    public Page<ProductDto> search(String query, Pageable pageable) {
        return productRepository.findByNameContainingIgnoreCaseAndIsActiveTrue(query, pageable).map(this::toDto);
    }

    @Transactional(readOnly = true)
    public ProductDto getById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Product not found with ID: " + id));
        return toDto(product);
    }

    @Transactional
    public ProductDto create(ProductRequest request) {
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("Category not found with ID: " + request.getCategoryId()));

        Product product = new Product();
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setImageUrl(request.getImageUrl());
        product.setCategory(category);

        if (request.getVariants() != null && !request.getVariants().isEmpty()) {
            for (ProductVariantDto vDto : request.getVariants()) {
                ProductVariant variant = new ProductVariant();
                variant.setProduct(product);
                variant.setWeightLabel(vDto.getWeightLabel());
                variant.setPrice(vDto.getPrice());
                variant.setStock(vDto.getStock() != null ? vDto.getStock() : 0);
                variant.setIsActive(vDto.getIsActive() != null ? vDto.getIsActive() : true);
                product.getVariants().add(variant);
            }
        }

        return toDto(productRepository.save(product));
    }

    @Transactional
    public ProductDto update(Long id, ProductRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Product not found with ID: " + id));

        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new IllegalArgumentException("Category not found with ID: " + request.getCategoryId()));
            product.setCategory(category);
        }

        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setImageUrl(request.getImageUrl());
        product.setUpdatedAt(LocalDateTime.now());

        if (request.getVariants() != null) {
            List<ProductVariant> existingVariants = product.getVariants();
            List<ProductVariantDto> incomingVariants = request.getVariants();

            List<ProductVariant> matchedExisting = new ArrayList<>();

            for (ProductVariantDto vDto : incomingVariants) {
                ProductVariant matched = null;

                // 1. Try matching by ID if present
                if (vDto.getId() != null) {
                    matched = existingVariants.stream()
                            .filter(ev -> vDto.getId().equals(ev.getId()))
                            .findFirst()
                            .orElse(null);
                }

                // 2. Try matching by weight label (case-insensitive)
                if (matched == null && vDto.getWeightLabel() != null) {
                    matched = existingVariants.stream()
                            .filter(ev -> ev.getWeightLabel() != null && ev.getWeightLabel().trim().equalsIgnoreCase(vDto.getWeightLabel().trim()))
                            .findFirst()
                            .orElse(null);
                }

                if (matched != null) {
                    matched.setWeightLabel(vDto.getWeightLabel());
                    matched.setPrice(vDto.getPrice());
                    matched.setStock(vDto.getStock() != null ? vDto.getStock() : 0);
                    matched.setIsActive(vDto.getIsActive() != null ? vDto.getIsActive() : true);
                    matchedExisting.add(matched);
                } else {
                    ProductVariant newVariant = new ProductVariant();
                    newVariant.setProduct(product);
                    newVariant.setWeightLabel(vDto.getWeightLabel());
                    newVariant.setPrice(vDto.getPrice());
                    newVariant.setStock(vDto.getStock() != null ? vDto.getStock() : 0);
                    newVariant.setIsActive(vDto.getIsActive() != null ? vDto.getIsActive() : true);
                    existingVariants.add(newVariant);
                    matchedExisting.add(newVariant);
                }
            }

            // Soft-delete un-matched variants instead of physical deletion
            for (ProductVariant ev : existingVariants) {
                if (!matchedExisting.contains(ev)) {
                    ev.setIsActive(false);
                }
            }
        }

        return toDtoAdmin(productRepository.save(product));
    }

    @Transactional
    public void delete(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Product not found with ID: " + id));
        product.setIsActive(false);
        productRepository.save(product);
    }

    public ProductDto toDto(Product product) {
        ProductDto dto = new ProductDto();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setDescription(product.getDescription());
        dto.setImageUrl(product.getImageUrl());
        dto.setIsActive(product.getIsActive());
        if (product.getCategory() != null) {
            dto.setCategoryId(product.getCategory().getId());
            dto.setCategoryName(product.getCategory().getName());
        }

        if (product.getVariants() != null) {
            List<ProductVariantDto> variantDtos = product.getVariants().stream()
                    .filter(v -> Boolean.TRUE.equals(v.getIsActive()))
                    .map(v -> {
                        ProductVariantDto vDto = new ProductVariantDto();
                        vDto.setId(v.getId());
                        vDto.setWeightLabel(v.getWeightLabel());
                        vDto.setPrice(v.getPrice());
                        vDto.setStock(v.getStock());
                        vDto.setIsActive(v.getIsActive());
                        return vDto;
                    })
                    .collect(Collectors.toList());
            dto.setVariants(variantDtos);
        }

        return dto;
    }

    public ProductDto toDtoAdmin(Product product) {
        ProductDto dto = new ProductDto();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setDescription(product.getDescription());
        dto.setImageUrl(product.getImageUrl());
        dto.setIsActive(product.getIsActive());
        if (product.getCategory() != null) {
            dto.setCategoryId(product.getCategory().getId());
            dto.setCategoryName(product.getCategory().getName());
        }

        if (product.getVariants() != null) {
            List<ProductVariantDto> variantDtos = product.getVariants().stream()
                    .map(v -> {
                        ProductVariantDto vDto = new ProductVariantDto();
                        vDto.setId(v.getId());
                        vDto.setWeightLabel(v.getWeightLabel());
                        vDto.setPrice(v.getPrice());
                        vDto.setStock(v.getStock());
                        vDto.setIsActive(v.getIsActive());
                        return vDto;
                    })
                    .collect(Collectors.toList());
            dto.setVariants(variantDtos);
        }

        return dto;
    }
}
