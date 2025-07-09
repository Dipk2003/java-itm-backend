package com.itech.itech_backend.dto;

import lombok.Data;

@Data
public class RegisterRequestDto {
    private String name;
    private String email;
    private String phone;
    private String password;
    private String role = "ROLE_USER"; // Default role
    private String userType = "user"; // Default user type
    
    // Vendor-specific fields
    private String businessName;
    private String businessAddress;
    private String gstNumber;
    private String panNumber;
    
    // Admin-specific fields
    private String department;
    private String designation;
}
