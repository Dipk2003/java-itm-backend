package com.itech.itech_backend.controller;

import com.itech.itech_backend.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Temporary controller for frontend testing without authentication
 * This will be removed when frontend auth is implemented
 */
@RestController
@RequestMapping("/temp")
@CrossOrigin
@RequiredArgsConstructor
public class TempTestController {

    private final ProductService productService;
    private final CategoryService categoryService;
    private final VendorsService vendorsService;
    private final UserService userService;
    private final OrderService orderService;
    private final LeadService leadService;

    // Temp endpoints for direct dashboard access
    @GetMapping("/admin/dashboard")
    public ResponseEntity<Map<String, Object>> getAdminDashboard() {
        Map<String, Object> dashboard = new HashMap<>();
        
        try {
            dashboard.put("totalUsers", userService.getUserCount());
            dashboard.put("totalVendors", vendorsService.getVendorCount());
            dashboard.put("totalProducts", productService.getAllProducts().size());
            dashboard.put("totalOrders", 0); // Placeholder - no method available
            dashboard.put("totalRevenue", 0); // Placeholder - no method available
            dashboard.put("recentOrders", List.of()); // Placeholder - no method available
            dashboard.put("topProducts", productService.getFeaturedProducts(5));
            dashboard.put("recentUsers", List.of()); // Placeholder - no method available
            dashboard.put("orderStats", Map.of()); // Placeholder - no method available
            dashboard.put("message", "Admin dashboard data loaded successfully");
        } catch (Exception e) {
            dashboard.put("error", "Error loading dashboard: " + e.getMessage());
        }
        
        return ResponseEntity.ok(dashboard);
    }

    @GetMapping("/vendor/dashboard")
    public ResponseEntity<Map<String, Object>> getVendorDashboard() {
        Map<String, Object> dashboard = new HashMap<>();
        
        try {
            // Using dummy vendor ID for testing
            Long vendorId = 1L;
            
            dashboard.put("totalProducts", productService.getProductsByVendor(vendorId).size());
            dashboard.put("totalOrders", orderService.getVendorOrders(vendorId).size());
            dashboard.put("totalRevenue", 0); // Placeholder - no method available
            dashboard.put("recentOrders", List.of()); // Placeholder - no method available
            dashboard.put("topProducts", List.of()); // Placeholder - no method available
            dashboard.put("productStats", Map.of()); // Placeholder - no method available
            dashboard.put("message", "Vendor dashboard data loaded successfully");
        } catch (Exception e) {
            dashboard.put("error", "Error loading vendor dashboard: " + e.getMessage());
        }
        
        return ResponseEntity.ok(dashboard);
    }

    @GetMapping("/user/dashboard")
    public ResponseEntity<Map<String, Object>> getUserDashboard() {
        Map<String, Object> dashboard = new HashMap<>();
        
        try {
            // Using dummy user ID for testing
            Long userId = 1L;
            
            dashboard.put("recentOrders", orderService.getUserOrders(userId, PageRequest.of(0, 10)));
            dashboard.put("totalOrders", orderService.getUserOrders(userId).size());
            dashboard.put("totalSpent", 0.0); // Placeholder - calculate from orders
            dashboard.put("favoriteProducts", List.of()); // Placeholder - no method available
            dashboard.put("cartItems", 0); // Placeholder
            dashboard.put("wishlistItems", 0); // Placeholder
            dashboard.put("message", "User dashboard data loaded successfully");
        } catch (Exception e) {
            dashboard.put("error", "Error loading user dashboard: " + e.getMessage());
        }
        
        return ResponseEntity.ok(dashboard);
    }

    // Temp endpoints for direct data access
    @GetMapping("/products")
    public ResponseEntity<?> getAllProducts() {
        try {
            return ResponseEntity.ok(productService.getAllProducts());
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of("error", "Error loading products: " + e.getMessage()));
        }
    }

    @GetMapping("/categories")
    public ResponseEntity<?> getAllCategories() {
        try {
            return ResponseEntity.ok(categoryService.getAllCategories());
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of("error", "Error loading categories: " + e.getMessage()));
        }
    }

    @GetMapping("/vendors")
    public ResponseEntity<?> getAllVendors() {
        try {
            return ResponseEntity.ok(vendorsService.getAllVendors());
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of("error", "Error loading vendors: " + e.getMessage()));
        }
    }

    @GetMapping("/users")
    public ResponseEntity<?> getAllUsers() {
        try {
            return ResponseEntity.ok(userService.getAllUsers());
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of("error", "Error loading users: " + e.getMessage()));
        }
    }

    @GetMapping("/orders")
    public ResponseEntity<?> getAllOrders() {
        try {
            return ResponseEntity.ok(orderService.getAllOrders());
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of("error", "Error loading orders: " + e.getMessage()));
        }
    }

    @GetMapping("/leads")
    public ResponseEntity<?> getAllLeads() {
        try {
            return ResponseEntity.ok(leadService.getAllLeads());
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of("error", "Error loading leads: " + e.getMessage()));
        }
    }

    // Test endpoint to check if backend is working
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "Backend is running successfully!");
        health.put("timestamp", System.currentTimeMillis());
        health.put("message", "All services are operational");
        return ResponseEntity.ok(health);
    }

    // Test endpoint with sample data
    @GetMapping("/sample-data")
    public ResponseEntity<Map<String, Object>> getSampleData() {
        Map<String, Object> data = new HashMap<>();
        data.put("sampleProducts", createSampleProducts());
        data.put("sampleUsers", createSampleUsers());
        data.put("sampleOrders", createSampleOrders());
        return ResponseEntity.ok(data);
    }

    private Map<String, Object> createSampleProducts() {
        Map<String, Object> products = new HashMap<>();
        products.put("product1", Map.of("name", "Laptop", "price", 50000, "category", "Electronics"));
        products.put("product2", Map.of("name", "Mouse", "price", 500, "category", "Accessories"));
        products.put("product3", Map.of("name", "Keyboard", "price", 1500, "category", "Accessories"));
        return products;
    }

    private Map<String, Object> createSampleUsers() {
        Map<String, Object> users = new HashMap<>();
        users.put("user1", Map.of("name", "John Doe", "email", "john@example.com", "role", "USER"));
        users.put("user2", Map.of("name", "Jane Smith", "email", "jane@example.com", "role", "VENDOR"));
        users.put("user3", Map.of("name", "Admin User", "email", "admin@example.com", "role", "ADMIN"));
        return users;
    }

    private Map<String, Object> createSampleOrders() {
        Map<String, Object> orders = new HashMap<>();
        orders.put("order1", Map.of("id", 1, "total", 5000, "status", "COMPLETED"));
        orders.put("order2", Map.of("id", 2, "total", 15000, "status", "PENDING"));
        orders.put("order3", Map.of("id", 3, "total", 25000, "status", "PROCESSING"));
        return orders;
    }
}
