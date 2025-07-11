package com.itech.itech_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExcelImportDto {
    private String category;
    private String subcategory;
    private String productName;
    private String description;
    private BigDecimal price;
    private BigDecimal originalPrice;
    private String brand;
    private String model;
    private String sku;
    private Integer minOrderQuantity;
    private String unit;
    private String specifications;
    private String tags;
    private Double gstRate;
    private BigDecimal weight;
    private BigDecimal length;
    private BigDecimal width;
    private BigDecimal height;
    private Boolean freeShipping;
    private BigDecimal shippingCharge;
    
    // Validation fields
    private Integer rowNumber;
    private String errorMessage;
    private Boolean isValid;
}
