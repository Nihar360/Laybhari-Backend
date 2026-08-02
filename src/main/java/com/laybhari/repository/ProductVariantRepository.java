package com.laybhari.repository;

import com.laybhari.entity.ProductVariant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ProductVariantRepository extends JpaRepository<ProductVariant, Long> {
    List<ProductVariant> findByProductIdAndIsActiveTrue(Long productId);
    Page<ProductVariant> findAllByOrderByProduct_NameAscWeightLabelAsc(Pageable pageable);
    Page<ProductVariant> findByStockLessThanOrderByStockAsc(Integer threshold, Pageable pageable);
}
