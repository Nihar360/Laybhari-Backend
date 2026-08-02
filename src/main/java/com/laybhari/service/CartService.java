package com.laybhari.service;

import com.laybhari.dto.*;
import com.laybhari.entity.*;
import com.laybhari.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductVariantRepository productVariantRepository;
    private final UserRepository userRepository;

    public CartService(CartRepository cartRepository,
                       CartItemRepository cartItemRepository,
                       ProductVariantRepository productVariantRepository,
                       UserRepository userRepository) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.productVariantRepository = productVariantRepository;
        this.userRepository = userRepository;
    }

    private User getUserByEmail(String identifier) {
        return userRepository.findByEmail(identifier)
                .or(() -> userRepository.findByPhone(identifier))
                .orElseThrow(() -> new IllegalArgumentException("User not found for: " + identifier));
    }

    private Cart getOrCreateCartForUser(User user) {
        return cartRepository.findByUser(user).orElseGet(() -> {
            Cart newCart = new Cart();
            newCart.setUser(user);
            return cartRepository.save(newCart);
        });
    }

    @Transactional(readOnly = true)
    public CartDto getCart(String userEmail) {
        User user = getUserByEmail(userEmail);
        Cart cart = getOrCreateCartForUser(user);
        return toCartDto(cart);
    }

    @Transactional
    public CartDto addToCart(String userEmail, AddToCartRequest request) {
        if (request.getProductVariantId() == null) {
            throw new IllegalArgumentException("productVariantId is required");
        }
        int qtyToAdd = request.getQuantity() != null && request.getQuantity() > 0 ? request.getQuantity() : 1;

        User user = getUserByEmail(userEmail);
        Cart cart = getOrCreateCartForUser(user);

        ProductVariant variant = productVariantRepository.findById(request.getProductVariantId())
                .orElseThrow(() -> new IllegalArgumentException("Product variant not found with ID: " + request.getProductVariantId()));

        if (!Boolean.TRUE.equals(variant.getIsActive())) {
            throw new IllegalArgumentException("Product variant is inactive");
        }

        CartItem cartItem = cartItemRepository
                .findByCartIdAndProductVariantId(cart.getId(), variant.getId())
                .orElse(null);

        if (cartItem != null) {
            cartItem.setQuantity(cartItem.getQuantity() + qtyToAdd);
            cartItem.setUpdatedAt(LocalDateTime.now());
        } else {
            cartItem = new CartItem();
            cartItem.setCart(cart);
            cartItem.setProductVariant(variant);
            cartItem.setQuantity(qtyToAdd);
            cart.getItems().add(cartItem);
        }

        cartItemRepository.save(cartItem);
        return toCartDto(cartRepository.findById(cart.getId()).orElse(cart));
    }

    @Transactional
    public CartDto updateCartItem(String userEmail, Long cartItemId, UpdateCartItemRequest request) {
        User user = getUserByEmail(userEmail);
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new IllegalArgumentException("Cart item not found with ID: " + cartItemId));

        if (!cartItem.getCart().getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("You are not authorized to modify this cart item");
        }

        Cart cart = cartItem.getCart();

        if (request.getQuantity() == null || request.getQuantity() <= 0) {
            cart.getItems().remove(cartItem);
            cartItemRepository.delete(cartItem);
        } else {
            cartItem.setQuantity(request.getQuantity());
            cartItem.setUpdatedAt(LocalDateTime.now());
            cartItemRepository.save(cartItem);
        }

        return toCartDto(cartRepository.findById(cart.getId()).orElse(cart));
    }

    @Transactional
    public CartDto removeFromCart(String userEmail, Long cartItemId) {
        User user = getUserByEmail(userEmail);
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new IllegalArgumentException("Cart item not found with ID: " + cartItemId));

        if (!cartItem.getCart().getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("You are not authorized to modify this cart item");
        }

        Cart cart = cartItem.getCart();
        cart.getItems().remove(cartItem);
        cartItemRepository.delete(cartItem);

        return toCartDto(cartRepository.findById(cart.getId()).orElse(cart));
    }

    public CartDto toCartDto(Cart cart) {
        CartDto dto = new CartDto();
        dto.setId(cart.getId());
        dto.setUserId(cart.getUser().getId());

        BigDecimal grandTotal = BigDecimal.ZERO;
        int totalItemsCount = 0;

        List<CartItemDto> itemDtos = new ArrayList<>();
        if (cart.getItems() != null) {
            for (CartItem item : cart.getItems()) {
                CartItemDto itemDto = new CartItemDto();
                itemDto.setId(item.getId());
                itemDto.setQuantity(item.getQuantity());

                ProductVariant variant = item.getProductVariant();
                if (variant != null) {
                    itemDto.setProductVariantId(variant.getId());
                    itemDto.setWeightLabel(variant.getWeightLabel());
                    itemDto.setUnitPrice(variant.getPrice());

                    BigDecimal lineTotal = variant.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
                    itemDto.setLineTotal(lineTotal);

                    grandTotal = grandTotal.add(lineTotal);
                    totalItemsCount += item.getQuantity();

                    Product product = variant.getProduct();
                    if (product != null) {
                        itemDto.setProductId(product.getId());
                        itemDto.setProductName(product.getName());
                        itemDto.setProductImageUrl(product.getImageUrl());
                    }
                }
                itemDtos.add(itemDto);
            }
        }

        dto.setItems(itemDtos);
        dto.setGrandTotal(grandTotal);
        dto.setTotalItems(totalItemsCount);
        return dto;
    }
}
