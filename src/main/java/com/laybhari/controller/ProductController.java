package com.laybhari.controller;

import com.laybhari.dto.ProductDto;
import com.laybhari.dto.ProductDto.ProductRequest;
import com.laybhari.service.ProductService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    // GET /api/products?page=0&size=20
    @GetMapping
    public ResponseEntity<Page<ProductDto>> getAll(Pageable pageable) {
        return ResponseEntity.ok(productService.getAllProducts(pageable));
    }

    // GET /api/products/category/3
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<Page<ProductDto>> getByCategory(@PathVariable Long categoryId, Pageable pageable) {
        return ResponseEntity.ok(productService.getByCategory(categoryId, pageable));
    }

    // GET /api/products/search?q=saree
    @GetMapping("/search")
    public ResponseEntity<Page<ProductDto>> search(@RequestParam("q") String query, Pageable pageable) {
        return ResponseEntity.ok(productService.search(query, pageable));
    }

    // GET /api/products/5
    @GetMapping("/{id}")
    public ResponseEntity<ProductDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getById(id));
    }

    // POST /api/products  (ADMIN only)
    @PostMapping
    public ResponseEntity<ProductDto> create(@RequestBody ProductRequest request) {
        return ResponseEntity.ok(productService.create(request));
    }

    // PUT /api/products/5  (ADMIN only)
    @PutMapping("/{id}")
    public ResponseEntity<ProductDto> update(@PathVariable Long id, @RequestBody ProductRequest request) {
        return ResponseEntity.ok(productService.update(id, request));
    }

    // DELETE /api/products/5  (ADMIN only, soft delete)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        productService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
