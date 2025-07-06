package com.itech.itech_backend.controller;

import com.itech.itech_backend.dto.ProductDto;
import com.itech.itech_backend.model.Product;
import com.itech.itech_backend.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
@CrossOrigin
public class ProductController {

    private final ProductService productService;

    @PostMapping
    public ResponseEntity<?> addProduct(@RequestBody ProductDto dto) {
        try {
            Product product = productService.addProduct(dto);
            return ResponseEntity.ok(product);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An unexpected error occurred: " + e.getMessage());
        }
    }

    @GetMapping
    public List<Product> getAllProducts() {
        return productService.getAllProducts();
    }

    @GetMapping("/vendor/{vendorId}")
    public List<Product> getByVendor(@PathVariable Long vendorId) {
        return productService.getProductsByVendor(vendorId);
    }
}
