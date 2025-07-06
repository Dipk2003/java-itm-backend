package com.itech.itech_backend.controller;

import com.itech.itech_backend.dto.JwtResponse;
import com.itech.itech_backend.dto.RegisterRequestDto;
import com.itech.itech_backend.dto.VerifyOtpRequestDto;
import com.itech.itech_backend.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@CrossOrigin
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public String register(@RequestBody RegisterRequestDto dto) {
        return authService.register(dto);
    }
    
    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody Map<String, String> loginRequest) {
        String contact = loginRequest.get("emailOrPhone");
        if (contact == null) {
            contact = loginRequest.get("phone");
        }
        if (contact == null) {
            contact = loginRequest.get("email");
        }
        
        if (contact == null) {
            return ResponseEntity.badRequest().body("Email or Phone is required");
        }
        
        String result = authService.sendLoginOtp(contact);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/verify")
    public ResponseEntity<?> verifyOtp(@RequestBody VerifyOtpRequestDto dto) {
        System.out.println("hhhhhhhhh");
        JwtResponse response = authService.verifyOtpAndGenerateToken(dto);
        System.out.println(response);
        if (response == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid or Expired OTP!");
        }
        return ResponseEntity.ok(response);
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtpAlternate(@RequestBody VerifyOtpRequestDto dto) {
        System.out.println("🔍 OTP Verification Request Received");
        System.out.println("📱 Contact: " + dto.getEmailOrPhone());
        System.out.println("🔢 OTP: " + dto.getOtp());
        
        JwtResponse response = authService.verifyOtpAndGenerateToken(dto);
        
        if (response == null) {
            System.out.println("❌ OTP Verification Failed");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid or Expired OTP!");
        }
        
        System.out.println("✅ OTP Verification Successful");
        return ResponseEntity.ok(response);
    }
}
