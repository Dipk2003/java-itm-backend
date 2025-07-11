package com.itech.itech_backend.controller;

import com.itech.itech_backend.dto.AddToCartDto;
import com.itech.itech_backend.dto.CartDto;
import com.itech.itech_backend.service.CartService;
import com.itech.itech_backend.util.JwtTokenUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/cart")
@CrossOrigin
@RequiredArgsConstructor
@Slf4j
public class CartController {

    private final CartService cartService;
    private final JwtTokenUtil jwtTokenUtil;

    @GetMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<CartDto> getUserCart(HttpServletRequest request) {
        try {
            Long userId = jwtTokenUtil.extractUserIdFromRequest(request);
            if (userId == null) {
                return ResponseEntity.badRequest().build();
            }
            CartDto cart = cartService.getUserCart(userId);
            return ResponseEntity.ok(cart);
        } catch (Exception e) {
            log.error("Error getting user cart", e);
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/add")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> addToCart(@RequestBody AddToCartDto addToCartDto, HttpServletRequest request) {
        try {
            Long userId = jwtTokenUtil.extractUserIdFromRequest(request);
            if (userId == null) {
                return ResponseEntity.badRequest().body("Invalid user session");
            }
            CartDto cart = cartService.addToCart(userId, addToCartDto);
            return ResponseEntity.ok(cart);
        } catch (Exception e) {
            log.error("Error adding to cart", e);
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    @PutMapping("/item/{cartItemId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> updateCartItem(@PathVariable Long cartItemId, @RequestParam int quantity, HttpServletRequest request) {
        try {
            Long userId = jwtTokenUtil.extractUserIdFromRequest(request);
            if (userId == null) {
                return ResponseEntity.badRequest().body("Invalid user session");
            }
            CartDto cart = cartService.updateCartItem(userId, cartItemId, quantity);
            return ResponseEntity.ok(cart);
        } catch (Exception e) {
            log.error("Error updating cart item", e);
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    @DeleteMapping("/item/{cartItemId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> removeFromCart(@PathVariable Long cartItemId, HttpServletRequest request) {
        try {
            Long userId = jwtTokenUtil.extractUserIdFromRequest(request);
            if (userId == null) {
                return ResponseEntity.badRequest().body("Invalid user session");
            }
            CartDto cart = cartService.removeFromCart(userId, cartItemId);
            return ResponseEntity.ok(cart);
        } catch (Exception e) {
            log.error("Error removing from cart", e);
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    @DeleteMapping("/clear")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> clearCart(HttpServletRequest request) {
        try {
            Long userId = jwtTokenUtil.extractUserIdFromRequest(request);
            if (userId == null) {
                return ResponseEntity.badRequest().body("Invalid user session");
            }
            cartService.clearCart(userId);
            return ResponseEntity.ok("Cart cleared successfully");
        } catch (Exception e) {
            log.error("Error clearing cart", e);
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

}
