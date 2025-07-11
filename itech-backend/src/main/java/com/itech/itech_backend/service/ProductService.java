package com.itech.itech_backend.service;

import com.itech.itech_backend.dto.ProductDto;
import com.itech.itech_backend.model.*;
import com.itech.itech_backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
                .originalPrice(dto.getOriginalPrice())
                .category(category)
                .vendor(vendor)
                .stock(dto.getStock())
                .brand(dto.getBrand())
                .model(dto.getModel())
                .sku(dto.getSku())
                .minOrderQuantity(dto.getMinOrderQuantity() != null ? dto.getMinOrderQuantity() : 1)
                .unit(dto.getUnit())
                .specifications(dto.getSpecifications())
                .tags(dto.getTags())
                .gstRate(dto.getGstRate())
                .weight(dto.getWeight() != null ? dto.getWeight().doubleValue() : null)
                .length(dto.getLength() != null ? dto.getLength().doubleValue() : null)
                .width(dto.getWidth() != null ? dto.getWidth().doubleValue() : null)
                .height(dto.getHeight() != null ? dto.getHeight().doubleValue() : null)
                .freeShipping(dto.getFreeShipping() != null ? dto.getFreeShipping() : false)
                .shippingCharge(dto.getShippingCharge() != null ? dto.getShippingCharge().doubleValue() : null)
                .isActive(dto.getIsActive() != null ? dto.getIsActive() : true)
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

    // New methods to fix compilation errors
    public Page<Product> getProducts(Pageable pageable, String category, String search, Double minPrice, Double maxPrice, String sortBy, String sortDir) {
        // For now, return a basic implementation
        List<Product> products = productRepo.findAll();
        // Filter by approved and active products
        products = products.stream()
                .filter(product -> product.isApproved() && product.isActive())
                .collect(Collectors.toList());
        
        // Apply filters if provided
        if (category != null && !category.isEmpty()) {
            products = products.stream()
                    .filter(product -> product.getCategory() != null && product.getCategory().getName().equalsIgnoreCase(category))
                    .collect(Collectors.toList());
        }
        
        if (search != null && !search.isEmpty()) {
            products = products.stream()
                    .filter(product -> product.getName().toLowerCase().contains(search.toLowerCase()) || 
                                     (product.getDescription() != null && product.getDescription().toLowerCase().contains(search.toLowerCase())))
                    .collect(Collectors.toList());
        }
        
        if (minPrice != null) {
            products = products.stream()
                    .filter(product -> product.getPrice() >= minPrice)
                    .collect(Collectors.toList());
        }
        
        if (maxPrice != null) {
            products = products.stream()
                    .filter(product -> product.getPrice() <= maxPrice)
                    .collect(Collectors.toList());
        }
        
        // Simple pagination
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), products.size());
        List<Product> pageContent = products.subList(start, end);
        
        return new PageImpl<>(pageContent, pageable, products.size());
    }

    public Product getProductById(Long productId) {
        return productRepo.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found with ID: " + productId));
    }

    public void incrementViewCount(Long productId) {
        Product product = getProductById(productId);
        product.setViewCount(product.getViewCount() + 1);
        productRepo.save(product);
    }

    public Page<Product> searchProducts(String query, Pageable pageable) {
        List<Product> products = productRepo.findAll();
        products = products.stream()
                .filter(product -> product.isApproved() && product.isActive())
                .filter(product -> product.getName().toLowerCase().contains(query.toLowerCase()) || 
                                 (product.getDescription() != null && product.getDescription().toLowerCase().contains(query.toLowerCase())))
                .collect(Collectors.toList());
        
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), products.size());
        List<Product> pageContent = products.subList(start, end);
        
        return new PageImpl<>(pageContent, pageable, products.size());
    }

    public Page<Product> getProductsByCategory(Long categoryId, Pageable pageable) {
        List<Product> products = productRepo.findAll();
        products = products.stream()
                .filter(product -> product.isApproved() && product.isActive())
                .filter(product -> product.getCategory() != null && product.getCategory().getId().equals(categoryId))
                .collect(Collectors.toList());
        
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), products.size());
        List<Product> pageContent = products.subList(start, end);
        
        return new PageImpl<>(pageContent, pageable, products.size());
    }

    public Page<Product> getProductsByVendor(Long vendorId, Pageable pageable) {
        List<Product> products = getProductsByVendor(vendorId);
        
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), products.size());
        List<Product> pageContent = products.subList(start, end);
        
        return new PageImpl<>(pageContent, pageable, products.size());
    }

    public List<Product> getFeaturedProducts(int limit) {
        List<Product> products = productRepo.findAll();
        return products.stream()
                .filter(product -> product.isApproved() && product.isActive() && product.isFeatured())
                .limit(limit)
                .collect(Collectors.toList());
    }

    public Product addProduct(Long vendorId, ProductDto dto) {
        dto.setVendorId(vendorId);
        return addProduct(dto);
    }

    public List<String> uploadProductImages(Long productId, Long vendorId, MultipartFile[] images) {
        // Validate product ownership
        Product product = getProductById(productId);
        if (!product.getVendor().getId().equals(vendorId)) {
            throw new IllegalArgumentException("You can only upload images for your own products");
        }
        
        // For now, return empty list - implement actual file upload logic
        return List.of();
    }

    public Product updateProduct(Long productId, Long vendorId, ProductDto dto) {
        Product product = getProductById(productId);
        if (!product.getVendor().getId().equals(vendorId)) {
            throw new IllegalArgumentException("You can only update your own products");
        }
        
        // Update product fields
        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setPrice(dto.getPrice());
        product.setStock(dto.getStock());
        
        if (dto.getCategoryId() != null) {
            Category category = categoryRepo.findById(dto.getCategoryId())
                    .orElseThrow(() -> new IllegalArgumentException("Category not found"));
            product.setCategory(category);
        }
        
        return productRepo.save(product);
    }

    public void deleteProduct(Long productId, Long vendorId) {
        Product product = getProductById(productId);
        if (!product.getVendor().getId().equals(vendorId)) {
            throw new IllegalArgumentException("You can only delete your own products");
        }
        
        productRepo.delete(product);
    }

    public Product updateProductStatus(Long productId, Long vendorId, boolean isActive) {
        Product product = getProductById(productId);
        if (!product.getVendor().getId().equals(vendorId)) {
            throw new IllegalArgumentException("You can only update your own products");
        }
        
        product.setActive(isActive);
        return productRepo.save(product);
    }

    public Product approveProduct(Long productId) {
        Product product = getProductById(productId);
        product.setApproved(true);
        return productRepo.save(product);
    }

    public Product setFeaturedStatus(Long productId, boolean featured) {
        Product product = getProductById(productId);
        product.setFeatured(featured);
        return productRepo.save(product);
    }

    public Page<Product> getPendingApprovalProducts(Pageable pageable) {
        List<Product> products = productRepo.findAll();
        products = products.stream()
                .filter(product -> !product.isApproved())
                .collect(Collectors.toList());
        
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), products.size());
        List<Product> pageContent = products.subList(start, end);
        
        return new PageImpl<>(pageContent, pageable, products.size());
    }
}
