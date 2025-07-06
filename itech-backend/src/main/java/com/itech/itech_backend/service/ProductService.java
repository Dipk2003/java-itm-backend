package com.itech.itech_backend.service;

import com.itech.itech_backend.dto.ProductDto;
import com.itech.itech_backend.model.*;
import com.itech.itech_backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepo;
    private final CategoryRepository categoryRepo;
    private final UserRepository userRepo;

    public Product addProduct(ProductDto dto) {
        // Validate required fields
        if (dto.getCategoryId() == null) {
            throw new IllegalArgumentException("Category ID is required");
        }
        if (dto.getVendorId() == null) {
            throw new IllegalArgumentException("Vendor ID is required");
        }

        Category category = categoryRepo.findById(dto.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("Category not found with ID: " + dto.getCategoryId()));
        User vendor = userRepo.findById(dto.getVendorId())
                .orElseThrow(() -> new IllegalArgumentException("Vendor not found with ID: " + dto.getVendorId()));

        Product product = Product.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .price(dto.getPrice())
                .category(category)
                .vendor(vendor)
                .stock(dto.getStock())
                .build();

        return productRepo.save(product);
    }

    public List<Product> getProductsByVendor(Long vendorId) {
        User vendor = userRepo.findById(vendorId).orElseThrow();
        return productRepo.findByVendor(vendor);
    }

    public List<Product> getAllProducts() {
        return productRepo.findAll();
    }
}
