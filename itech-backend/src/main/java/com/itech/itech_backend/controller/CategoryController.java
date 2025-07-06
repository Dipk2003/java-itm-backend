package com.itech.itech_backend.controller;

import com.itech.itech_backend.model.Category;
import com.itech.itech_backend.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
@CrossOrigin
public class CategoryController {

    private final CategoryService categoryService;

    @PostMapping
    public Category addCategory(@RequestBody CategoryRequest request) {
        return categoryService.addCategory(request.getName());
    }

    // Inner class for request body
    public static class CategoryRequest {
        private String name;
        
        public String getName() {
            return name;
        }
        
        public void setName(String name) {
            this.name = name;
        }
    }

    @GetMapping
    public List<Category> getAll() {
        return categoryService.getAllCategories();
    }
}
