package com.itech.itech_backend.service;

import com.itech.itech_backend.model.User;
import com.itech.itech_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepo;

    public List<User> getAllVendors() {
        return userRepo.findAll();
    }

    public User updateVendorType(Long userId, String vendorType) {
        User user = userRepo.findById(userId).orElseThrow();
        user.setVendorType(Enum.valueOf(com.itech.itech_backend.enums.VendorType.class, vendorType));
        return userRepo.save(user);
    }
}
