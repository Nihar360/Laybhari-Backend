package com.laybhari.repository;

import com.laybhari.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    Optional<CartItem> findByCartIdAndProductVariantId(Long cartId, Long productVariantId);
}
