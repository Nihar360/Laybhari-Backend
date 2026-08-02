package com.laybhari.controller;

import com.laybhari.dto.ProductDto;
import com.laybhari.service.ProductService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/products")
public class AdminProductController {

    private final ProductService productService;

    public AdminProductController(ProductService productService) {
        this.productService = productService;
    }

    // GET /api/admin/products (ADMIN only - includes inactive products)
    @GetMapping
    public ResponseEntity<Page<ProductDto>> getAllForAdmin(Pageable pageable) {
        return ResponseEntity.ok(productService.getAllProductsAdmin(pageable));
    }
}
