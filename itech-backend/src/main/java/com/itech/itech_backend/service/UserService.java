package com.itech.itech_backend.service;

import com.itech.itech_backend.model.User;
import com.itech.itech_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    // Get all users
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    // Get users by role
    public List<User> getUsersByRole(String role) {
        return userRepository.findByRole(role);
    }

    // Get user by ID
    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    // Get user by email
    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    // Get user by phone
    public Optional<User> getUserByPhone(String phone) {
        return userRepository.findByPhone(phone);
    }

    // Get user by email or phone
    public User getByEmailOrPhone(String identifier) {
        return userRepository.findByEmailOrPhone(identifier, identifier)
                .orElseThrow(() -> new RuntimeException("User not found with email/phone: " + identifier));
    }

    // Save user
    public User saveUser(User user) {
        return userRepository.save(user);
    }

    // Get verified users
    public List<User> getVerifiedUsers() {
        return userRepository.findByVerifiedTrue();
    }

    // Get unverified users
    public List<User> getUnverifiedUsers() {
        return userRepository.findByVerifiedFalse();
    }

    // Get active users
    public List<User> getActiveUsers() {
        return userRepository.findByIsActiveTrue();
    }

    // Get inactive users
    public List<User> getInactiveUsers() {
        return userRepository.findByIsActiveFalse();
    }

    // Update user
    public User updateUser(Long id, User userDetails) {
        Optional<User> userOpt = userRepository.findById(id);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setName(userDetails.getName());
            user.setEmail(userDetails.getEmail());
            user.setPhone(userDetails.getPhone());
            
            // Update role-specific fields
            if ("ROLE_VENDOR".equals(userDetails.getRole())) {
                user.setBusinessName(userDetails.getBusinessName());
                user.setBusinessAddress(userDetails.getBusinessAddress());
                user.setGstNumber(userDetails.getGstNumber());
                user.setPanNumber(userDetails.getPanNumber());
            } else if ("ROLE_ADMIN".equals(userDetails.getRole())) {
                user.setDepartment(userDetails.getDepartment());
                user.setDesignation(userDetails.getDesignation());
            }
            
            return userRepository.save(user);
        }
        return null;
    }

    // Delete user
    public boolean deleteUser(Long id) {
        if (userRepository.existsById(id)) {
            userRepository.deleteById(id);
            return true;
        }
        return false;
    }

    // Activate user
    public boolean activateUser(Long id) {
        Optional<User> userOpt = userRepository.findById(id);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setActive(true);
            userRepository.save(user);
            return true;
        }
        return false;
    }

    // Deactivate user
    public boolean deactivateUser(Long id) {
        Optional<User> userOpt = userRepository.findById(id);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setActive(false);
            userRepository.save(user);
            return true;
        }
        return false;
    }

    // Get user count
    public long getUserCount() {
        return userRepository.count();
    }

    // Get user count by role
    public long getUserCountByRole(String role) {
        return userRepository.countByRole(role);
    }

    // Check if user exists by email
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    // Check if user exists by phone
    public boolean existsByPhone(String phone) {
        return userRepository.existsByPhone(phone);
    }
}
