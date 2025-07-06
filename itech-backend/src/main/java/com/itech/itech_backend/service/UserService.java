package com.itech.itech_backend.service;

import com.itech.itech_backend.model.User;
import com.itech.itech_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepo;

    public User getById(Long id) {
        return userRepo.findById(id).orElseThrow(() -> new RuntimeException("User not found with ID: " + id));
    }

    public User getByEmailOrPhone(String identifier) {
        return userRepo.findByEmailOrPhone(identifier, identifier)
                .orElseThrow(() -> new RuntimeException("User not found with email/phone: " + identifier));
    }
}
