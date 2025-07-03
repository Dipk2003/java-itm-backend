package com.itech.itech_backend.service;

import com.itech.itech_backend.repository.UserRepository;
import com.itech.itech_backend.repository.ProductRepository;
import com.itech.itech_backend.repository.VendorRankingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AdminService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private VendorRankingRepository vendorRankingRepository;

    public long getTotalUsers() {
        return userRepository.count();
    }

    public long getTotalProducts() {
        return productRepository.count();
    }

    public long getPremiumVendors() {
        return userRepository.findAll().stream()
                .filter(user -> "ROLE_VENDOR_PREMIUM".equals(user.getRole()))
                .count();
    }
}
