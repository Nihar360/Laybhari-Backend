package com.laybhari.controller;

import com.laybhari.entity.Category;
import com.laybhari.repository.CategoryRepository;
import com.laybhari.repository.ProductRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    public CategoryController(CategoryRepository categoryRepository, ProductRepository productRepository) {
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
    }

    // GET /api/categories (public — storefront & admin)
    @GetMapping
    public ResponseEntity<List<Category>> getAllCategories() {
        return ResponseEntity.ok(categoryRepository.findAll());
    }

    // GET /api/categories/{id} (public)
    @GetMapping("/{id}")
    public ResponseEntity<Category> getCategoryById(@PathVariable Long id) {
        return categoryRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // POST /api/categories (ADMIN only) body: {name, imageUrl}
    @PostMapping
    public ResponseEntity<?> createCategory(@RequestBody Category category) {
        if (category.getName() == null || category.getName().trim().isEmpty()) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Category name is required.");
            return ResponseEntity.badRequest().body(error);
        }
        Category saved = categoryRepository.save(category);
        return ResponseEntity.ok(saved);
    }

    // PUT /api/categories/{id} (ADMIN only)
    @PutMapping("/{id}")
    public ResponseEntity<?> updateCategory(@PathVariable Long id, @RequestBody Category request) {
        return categoryRepository.findById(id)
                .map(existing -> {
                    if (request.getName() != null && !request.getName().trim().isEmpty()) {
                        existing.setName(request.getName().trim());
                    }
                    if (request.getImageUrl() != null) {
                        existing.setImageUrl(request.getImageUrl());
                    }
                    Category updated = categoryRepository.save(existing);
                    return ResponseEntity.ok(updated);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // DELETE /api/categories/{id} (ADMIN only — block delete if products still reference this category)
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCategory(@PathVariable Long id) {
        if (!categoryRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }

        // Check if any products reference this category
        if (productRepository.existsByCategoryId(id)) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Cannot delete category: It is currently referenced by products.");
            return ResponseEntity.badRequest().body(error);
        }

        categoryRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
