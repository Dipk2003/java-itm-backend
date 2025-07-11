package com.itech.itech_backend.controller;

import com.itech.itech_backend.dto.ProductDto;
import com.itech.itech_backend.model.Product;
import com.itech.itech_backend.service.ProductService;
import com.itech.itech_backend.util.JwtTokenUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@CrossOrigin
@Slf4j
public class ProductController {

    private final ProductService productService;
    private final JwtTokenUtil jwtTokenUtil;

    // Public endpoints
    @GetMapping
    public ResponseEntity<Page<Product>> getAllProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<Product> products = productService.getProducts(pageable, category, search, minPrice, maxPrice, sortBy, sortDir);
            return ResponseEntity.ok(products);
        } catch (Exception e) {
            log.error("Error getting products", e);
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{productId}")
    public ResponseEntity<Product> getProductById(@PathVariable Long productId) {
        try {
            Product product = productService.getProductById(productId);
            // Increment view count
            productService.incrementViewCount(productId);
            return ResponseEntity.ok(product);
        } catch (Exception e) {
            log.error("Error getting product", e);
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/search")
    public ResponseEntity<Page<Product>> searchProducts(
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<Product> products = productService.searchProducts(query, pageable);
            return ResponseEntity.ok(products);
        } catch (Exception e) {
            log.error("Error searching products", e);
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<Page<Product>> getProductsByCategory(
            @PathVariable Long categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<Product> products = productService.getProductsByCategory(categoryId, pageable);
            return ResponseEntity.ok(products);
        } catch (Exception e) {
            log.error("Error getting products by category", e);
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/vendor/{vendorId}")
    public ResponseEntity<Page<Product>> getProductsByVendor(
            @PathVariable Long vendorId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<Product> products = productService.getProductsByVendor(vendorId, pageable);
            return ResponseEntity.ok(products);
        } catch (Exception e) {
            log.error("Error getting products by vendor", e);
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/featured")
    public ResponseEntity<List<Product>> getFeaturedProducts(
            @RequestParam(defaultValue = "8") int limit) {
        try {
            List<Product> products = productService.getFeaturedProducts(limit);
            return ResponseEntity.ok(products);
        } catch (Exception e) {
            log.error("Error getting featured products", e);
            return ResponseEntity.badRequest().build();
        }
    }

    // Vendor endpoints
    @PostMapping
    @PreAuthorize("hasRole('VENDOR')")
    public ResponseEntity<?> addProduct(@RequestBody ProductDto dto, HttpServletRequest request) {
        try {
            Long vendorId = jwtTokenUtil.extractUserIdFromRequest(request);
            if (vendorId == null) {
                return ResponseEntity.badRequest().body("Invalid vendor session");
            }
            Product product = productService.addProduct(vendorId, dto);
            return ResponseEntity.ok(product);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        } catch (Exception e) {
            log.error("Error adding product", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An unexpected error occurred: " + e.getMessage());
        }
    }

    @PostMapping(value = "/{productId}/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('VENDOR')")
    public ResponseEntity<?> uploadProductImages(
            @PathVariable Long productId,
            @RequestParam("images") MultipartFile[] images,
            HttpServletRequest request) {
        try {
            Long vendorId = jwtTokenUtil.extractUserIdFromRequest(request);
            if (vendorId == null) {
                return ResponseEntity.badRequest().body("Invalid vendor session");
            }
            List<String> imageUrls = productService.uploadProductImages(productId, vendorId, images);
            return ResponseEntity.ok(imageUrls);
        } catch (Exception e) {
            log.error("Error uploading product images", e);
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    @PutMapping("/{productId}")
    @PreAuthorize("hasRole('VENDOR')")
    public ResponseEntity<?> updateProduct(@PathVariable Long productId, @RequestBody ProductDto dto, HttpServletRequest request) {
        try {
            Long vendorId = jwtTokenUtil.extractUserIdFromRequest(request);
            if (vendorId == null) {
                return ResponseEntity.badRequest().body("Invalid vendor session");
            }
            Product product = productService.updateProduct(productId, vendorId, dto);
            return ResponseEntity.ok(product);
        } catch (Exception e) {
            log.error("Error updating product", e);
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    @DeleteMapping("/{productId}")
    @PreAuthorize("hasRole('VENDOR')")
    public ResponseEntity<?> deleteProduct(@PathVariable Long productId, HttpServletRequest request) {
        try {
            Long vendorId = jwtTokenUtil.extractUserIdFromRequest(request);
            if (vendorId == null) {
                return ResponseEntity.badRequest().body("Invalid vendor session");
            }
            productService.deleteProduct(productId, vendorId);
            return ResponseEntity.ok("Product deleted successfully");
        } catch (Exception e) {
            log.error("Error deleting product", e);
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/vendor/my-products")
    @PreAuthorize("hasRole('VENDOR')")
    public ResponseEntity<Page<Product>> getVendorProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            HttpServletRequest request) {
        try {
            Long vendorId = jwtTokenUtil.extractUserIdFromRequest(request);
            if (vendorId == null) {
                return ResponseEntity.badRequest().build();
            }
            Pageable pageable = PageRequest.of(page, size);
            Page<Product> products = productService.getProductsByVendor(vendorId, pageable);
            return ResponseEntity.ok(products);
        } catch (Exception e) {
            log.error("Error getting vendor products", e);
            return ResponseEntity.badRequest().build();
        }
    }

    @PatchMapping("/{productId}/status")
    @PreAuthorize("hasRole('VENDOR')")
    public ResponseEntity<?> updateProductStatus(
            @PathVariable Long productId,
            @RequestParam boolean isActive,
            HttpServletRequest request) {
        try {
            Long vendorId = jwtTokenUtil.extractUserIdFromRequest(request);
            if (vendorId == null) {
                return ResponseEntity.badRequest().body("Invalid vendor session");
            }
            Product product = productService.updateProductStatus(productId, vendorId, isActive);
            return ResponseEntity.ok(product);
        } catch (Exception e) {
            log.error("Error updating product status", e);
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    // Admin endpoints
    @PatchMapping("/{productId}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> approveProduct(@PathVariable Long productId) {
        try {
            Product product = productService.approveProduct(productId);
            return ResponseEntity.ok(product);
        } catch (Exception e) {
            log.error("Error approving product", e);
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    @PatchMapping("/{productId}/feature")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> featureProduct(@PathVariable Long productId, @RequestParam boolean featured) {
        try {
            Product product = productService.setFeaturedStatus(productId, featured);
            return ResponseEntity.ok(product);
        } catch (Exception e) {
            log.error("Error setting featured status", e);
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/pending-approval")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<Product>> getPendingProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<Product> products = productService.getPendingApprovalProducts(pageable);
            return ResponseEntity.ok(products);
        } catch (Exception e) {
            log.error("Error getting pending products", e);
            return ResponseEntity.badRequest().build();
        }
    }

}
