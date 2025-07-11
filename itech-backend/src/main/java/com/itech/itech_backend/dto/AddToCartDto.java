package com.itech.itech_backend.dto;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddToCartDto {
    private Long productId;
    private int quantity;
}
