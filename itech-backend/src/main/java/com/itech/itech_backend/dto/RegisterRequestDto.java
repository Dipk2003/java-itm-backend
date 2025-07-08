package com.itech.itech_backend.dto;

import lombok.Data;

@Data
public class RegisterRequestDto {
    private String name;
    private String email;
    private String phone;
    private String password;
    private String role = "ROLE_VENDOR"; // Default role for vendor registration
    private String userType = "vendor"; // Default user type
}
