package com.itech.itech_backend.controller;

import com.itech.itech_backend.dto.AuthResponse;
import com.itech.itech_backend.model.User;
import com.itech.itech_backend.repository.UserRepository;
import com.itech.itech_backend.service.AuthService;
import com.itech.itech_backend.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@CrossOrigin
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/register/email")
    public String registerWithEmail(@RequestBody Map<String, String> payload) {
        return authService.registerWithEmail(payload.get("name"), payload.get("email"));
    }

    @PostMapping("/register/phone")
    public String registerWithPhone(@RequestBody Map<String, String> payload) {
        return authService.registerWithPhone(payload.get("name"), payload.get("phone"));
    }

    @PostMapping("/verify")
    public ResponseEntity<?> verifyOtp(@RequestBody Map<String, String> payload) {
        String input = payload.get("emailOrPhone");
        String otp = payload.get("otp");

        boolean verified = authService.verifyOTP(input, otp);
        if (!verified) {
            return ResponseEntity.status(401).body("Invalid OTP!");
        }

        User user = userRepository.findByEmail(input).orElseGet(() ->
                userRepository.findByPhone(input).orElseThrow());

        String token = jwtUtil.generateToken(user.getEmail(), user.getRole());

        return ResponseEntity.ok(new AuthResponse(token, user.getRole()));
    }
}
