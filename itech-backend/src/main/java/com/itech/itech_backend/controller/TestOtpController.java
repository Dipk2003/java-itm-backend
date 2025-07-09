package com.itech.itech_backend.controller;

import com.itech.itech_backend.service.SmsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Random;

@RestController
@RequestMapping("/test")
@CrossOrigin
@RequiredArgsConstructor
public class TestOtpController {

    private final SmsService smsService;

    @PostMapping("/send-otp")
    public String testOtp(@RequestParam String phone) {
        // Generate a test OTP
        String otp = String.format("%06d", new Random().nextInt(999999));
        
        System.out.println("=".repeat(50));
        System.out.println("🧪 TEST OTP GENERATION");
        System.out.println("Phone: " + phone);
        System.out.println("Generated OTP: " + otp);
        System.out.println("=".repeat(50));
        
        // Send OTP via SMS service
        smsService.sendOtp(phone, otp);
        
        return "OTP sent to " + phone + ". Check console for simulated SMS.";
    }

    @GetMapping("/hello")
    public String hello() {
        System.out.println("👋 Hello Test Endpoint Called!");
        return "Hello! Server is running and console logging is working.";
    }
}
