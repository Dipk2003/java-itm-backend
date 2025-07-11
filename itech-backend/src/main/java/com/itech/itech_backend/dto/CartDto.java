package com.itech.itech_backend.dto;

import lombok.*;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartDto {
    private Long id;
    private Long userId;
    private List<CartItemDto> items;
    private double totalAmount;
    private int totalItems;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CartItemDto {
        private Long id;
        private Long productId;
        private String productName;
        private String productImage;
        private double price;
        private int quantity;
        private double subtotal;
        private String vendorName;
        private Long vendorId;
        private boolean inStock;
        private int availableStock;
    }
}
