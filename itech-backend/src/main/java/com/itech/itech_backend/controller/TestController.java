package com.itech.itech_backend.controller;

import com.itech.itech_backend.model.User;
import com.itech.itech_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/test")
@CrossOrigin
@RequiredArgsConstructor
public class TestController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @GetMapping("/users")
    public List<Map<String, Object>> getAllUsers() {
        return userRepository.findAll().stream()
                .map(user -> {
                    Map<String, Object> userMap = new HashMap<>();
                    userMap.put("id", user.getId());
                    userMap.put("name", user.getName());
                    userMap.put("email", user.getEmail());
                    userMap.put("phone", user.getPhone() != null ? user.getPhone() : "N/A");
                    userMap.put("hasPassword", user.getPassword() != null && !user.getPassword().isEmpty());
                    userMap.put("isVerified", user.isVerified());
                    return userMap;
                })
                .collect(Collectors.toList());
    }
    
    @PostMapping("/set-password-for-user")
    public String setPasswordForUser(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String password = request.get("password");
        
        User user = userRepository.findByEmailOrPhone(email, email)
                .orElse(null);
                
        if (user == null) {
            return "User not found";
        }
        
        user.setPassword(passwordEncoder.encode(password));
        userRepository.save(user);
        
        return "Password set for user: " + user.getName() + " (" + user.getEmail() + ")";
    }
    
    @PostMapping("/create-test-user")
    public String createTestUser() {
        User testUser = User.builder()
                .name("Test User")
                .email("test@example.com")
                .phone("1234567890")
                .password(passwordEncoder.encode("password123"))
                .verified(true)
                .build();
                
        userRepository.save(testUser);
        return "Test user created: test@example.com / password123";
    }
    
    @PostMapping("/create-vendor-user")
    public String createVendorUser() {
        // Check if vendor already exists
        if (userRepository.findByEmailOrPhone("vendor@abc.com", "vendor@abc.com").isPresent()) {
            return "Vendor user already exists: vendor@abc.com";
        }
        
        User vendorUser = User.builder()
                .name("ABC Vendor")
                .email("vendor@abc.com")
                .phone("9876543213")
                .password(passwordEncoder.encode("password123"))
                .role("ROLE_VENDOR")
                .verified(true)
                .businessName("ABC Company")
                .businessAddress("123 Main Street, Mumbai, Maharashtra, 400001")
                .build();
                
        userRepository.save(vendorUser);
        return "Vendor user created: vendor@abc.com / password123";
    }
}
