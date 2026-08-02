package com.laybhari.controller;

import com.laybhari.dto.AddToCartRequest;
import com.laybhari.dto.CartDto;
import com.laybhari.dto.UpdateCartItemRequest;
import com.laybhari.service.CartService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    // GET /api/cart → returns user's cart with items, line totals, and grand total
    @GetMapping
    public ResponseEntity<CartDto> getCart(Authentication authentication) {
        String email = authentication.getName();
        return ResponseEntity.ok(cartService.getCart(email));
    }

    // POST /api/cart → body: { productVariantId, quantity }
    @PostMapping
    public ResponseEntity<CartDto> addToCart(Authentication authentication,
                                             @RequestBody AddToCartRequest request) {
        String email = authentication.getName();
        return ResponseEntity.ok(cartService.addToCart(email, request));
    }

    // PUT /api/cart/{cartItemId} → body: { quantity }
    @PutMapping("/{cartItemId}")
    public ResponseEntity<CartDto> updateCartItem(Authentication authentication,
                                                   @PathVariable Long cartItemId,
                                                   @RequestBody UpdateCartItemRequest request) {
        String email = authentication.getName();
        return ResponseEntity.ok(cartService.updateCartItem(email, cartItemId, request));
    }

    // DELETE /api/cart/{cartItemId} → removes item from cart
    @DeleteMapping("/{cartItemId}")
    public ResponseEntity<CartDto> removeFromCart(Authentication authentication,
                                                    @PathVariable Long cartItemId) {
        String email = authentication.getName();
        return ResponseEntity.ok(cartService.removeFromCart(email, cartItemId));
    }
}
