package com.laybhari.repository;

import com.laybhari.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {
    Page<Product> findByIsActiveTrue(Pageable pageable);
    Page<Product> findByCategoryIdAndIsActiveTrue(Long categoryId, Pageable pageable);
    Page<Product> findByNameContainingIgnoreCaseAndIsActiveTrue(String name, Pageable pageable);
    boolean existsByCategoryId(Long categoryId);
    long countByCategoryId(Long categoryId);
}
