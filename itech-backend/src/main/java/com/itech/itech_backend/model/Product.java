package com.itech.itech_backend.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;
    
    @Column(length = 2000)
    private String description;
    
    @Column(nullable = false)
    private Double price;
    
    private Double originalPrice; // For discount calculations
    
    private String brand;
    
    private String model;
    
    private String sku;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;

    @ManyToOne
    @JoinColumn(name = "vendor_id", nullable = false)
    private User vendor; // The vendor who listed the product

    @Column(nullable = false)
    private int stock;
    
    @Builder.Default
    private int minOrderQuantity = 1;
    
    private String unit; // kg, piece, meter, etc.
    
    // Image URLs (stored as comma-separated string or JSON)
    @Column(length = 1000)
    private String imageUrls;
    
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<ProductImage> images = new ArrayList<>();
    
    // Product specifications as JSON string
    @Column(length = 2000)
    private String specifications;
    
    // SEO and search related
    private String metaTitle;
    private String metaDescription;
    private String tags; // comma-separated keywords
    
    // Status and visibility
    @Builder.Default
    private boolean isActive = true;
    
    @Builder.Default
    private boolean isApproved = false; // Admin approval required
    
    @Builder.Default
    private boolean isFeatured = false;
    
    // Vendor's GST selection for this product
    private String selectedGstNumber;
    private Double gstRate;
    
    // Statistics
    @Builder.Default
    private int viewCount = 0;
    
    @Builder.Default
    private int orderCount = 0;
    
    // Dimensions and weight
    private Double weight;
    private Double length;
    private Double width;
    private Double height;
    
    // Shipping
    @Builder.Default
    private boolean freeShipping = false;
    
    private Double shippingCharge;
    
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
    
    private LocalDateTime updatedAt;
    
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
    
    // Helper methods
    public boolean hasDiscount() {
        return originalPrice != null && originalPrice > price;
    }
    
    public double getDiscountPercentage() {
        if (!hasDiscount()) return 0;
        return ((originalPrice - price) / originalPrice) * 100;
    }
    
    public boolean isInStock() {
        return stock > 0;
    }
    
    public boolean canOrder(int quantity) {
        return isActive && isApproved && stock >= quantity && quantity >= minOrderQuantity;
    }
}
