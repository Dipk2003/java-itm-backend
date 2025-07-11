package com.itech.itech_backend.service;

import com.itech.itech_backend.dto.AddToCartDto;
import com.itech.itech_backend.dto.CartDto;
import com.itech.itech_backend.model.*;
import com.itech.itech_backend.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public CartDto getUserCart(Long userId) {
        Cart cart = getOrCreateCart(userId);
        return convertToDto(cart);
    }

    public CartDto addToCart(Long userId, AddToCartDto addToCartDto) {
        log.info("Adding product {} to cart for user {}", addToCartDto.getProductId(), userId);
        
        // Validate product
        Product product = productRepository.findById(addToCartDto.getProductId())
                .orElseThrow(() -> new RuntimeException("Product not found"));
        
        if (!product.canOrder(addToCartDto.getQuantity())) {
            throw new RuntimeException("Product is not available for order or insufficient stock");
        }
        
        Cart cart = getOrCreateCart(userId);
        
        // Check if item already exists in cart
        Optional<CartItem> existingItem = cartItemRepository.findByCartAndProduct(cart, product);
        
        if (existingItem.isPresent()) {
            // Update quantity
            CartItem item = existingItem.get();
            int newQuantity = item.getQuantity() + addToCartDto.getQuantity();
            
            if (!product.canOrder(newQuantity)) {
                throw new RuntimeException("Cannot add more items. Insufficient stock or exceeds maximum order limit");
            }
            
            item.setQuantity(newQuantity);
            cartItemRepository.save(item);
        } else {
            // Create new cart item
            CartItem cartItem = CartItem.builder()
                    .cart(cart)
                    .product(product)
                    .quantity(addToCartDto.getQuantity())
                    .price(product.getPrice())
                    .build();
            
            cartItemRepository.save(cartItem);
        }
        
        return convertToDto(cart);
    }

    public CartDto updateCartItem(Long userId, Long cartItemId, int quantity) {
        Cart cart = getOrCreateCart(userId);
        
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new RuntimeException("Cart item not found"));
        
        if (!cartItem.getCart().getId().equals(cart.getId())) {
            throw new RuntimeException("Cart item does not belong to user");
        }
        
        if (quantity <= 0) {
            cartItemRepository.delete(cartItem);
        } else {
            Product product = cartItem.getProduct();
            if (!product.canOrder(quantity)) {
                throw new RuntimeException("Invalid quantity or insufficient stock");
            }
            
            cartItem.setQuantity(quantity);
            cartItemRepository.save(cartItem);
        }
        
        return convertToDto(cart);
    }

    public CartDto removeFromCart(Long userId, Long cartItemId) {
        Cart cart = getOrCreateCart(userId);
        
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new RuntimeException("Cart item not found"));
        
        if (!cartItem.getCart().getId().equals(cart.getId())) {
            throw new RuntimeException("Cart item does not belong to user");
        }
        
        cartItemRepository.delete(cartItem);
        return convertToDto(cart);
    }

    public void clearCart(Long userId) {
        Cart cart = getOrCreateCart(userId);
        cartItemRepository.deleteByCartId(cart.getId());
    }

    private Cart getOrCreateCart(Long userId) {
        return cartRepository.findByUserId(userId)
                .orElseGet(() -> {
                    User user = userRepository.findById(userId)
                            .orElseThrow(() -> new RuntimeException("User not found"));
                    
                    Cart cart = Cart.builder()
                            .user(user)
                            .build();
                    
                    return cartRepository.save(cart);
                });
    }

    private CartDto convertToDto(Cart cart) {
        List<CartDto.CartItemDto> itemDtos = cart.getItems().stream()
                .map(this::convertItemToDto)
                .collect(Collectors.toList());
        
        return CartDto.builder()
                .id(cart.getId())
                .userId(cart.getUser().getId())
                .items(itemDtos)
                .totalAmount(cart.getTotalAmount())
                .totalItems(cart.getTotalItems())
                .build();
    }

    private CartDto.CartItemDto convertItemToDto(CartItem item) {
        Product product = item.getProduct();
        String productImage = null;
        
        // Get first image if available
        if (product.getImages() != null && !product.getImages().isEmpty()) {
            productImage = product.getImages().get(0).getImageUrl();
        } else if (product.getImageUrls() != null && !product.getImageUrls().isEmpty()) {
            productImage = product.getImageUrls().split(",")[0];
        }
        
        return CartDto.CartItemDto.builder()
                .id(item.getId())
                .productId(product.getId())
                .productName(product.getName())
                .productImage(productImage)
                .price(item.getPrice())
                .quantity(item.getQuantity())
                .subtotal(item.getSubtotal())
                .vendorName(product.getVendor().getName())
                .vendorId(product.getVendor().getId())
                .inStock(product.isInStock())
                .availableStock(product.getStock())
                .build();
    }
}
