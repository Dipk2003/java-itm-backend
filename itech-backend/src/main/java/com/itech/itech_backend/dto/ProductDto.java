package com.itech.itech_backend.dto;

import lombok.Data;

@Data
public class ProductDto {
    private String name;
    private String description;
    private Double price;
    private int stock;
    private Long categoryId;
    private Long vendorId;
}
