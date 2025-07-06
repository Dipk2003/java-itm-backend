package com.itech.itech_backend.repository;

import com.itech.itech_backend.model.Product;
import com.itech.itech_backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByVendor(User vendor);
}
