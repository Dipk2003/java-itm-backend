package com.itech.itech_backend.service;

import com.itech.itech_backend.model.Category;
import com.itech.itech_backend.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepo;

    public Category addCategory(String name) {
        if (!categoryRepo.existsByName(name)) {
            return categoryRepo.save(Category.builder().name(name).build());
        }
        return null; // Already exists
    }

    public List<Category> getAllCategories() {
        return categoryRepo.findAll();
    }
}
