package com.itech.itech_backend.service;

import com.itech.itech_backend.dto.JwtResponse;
import com.itech.itech_backend.dto.RegisterRequestDto;
import com.itech.itech_backend.dto.VerifyOtpRequestDto;
import com.itech.itech_backend.model.OtpVerification;
import com.itech.itech_backend.model.User;
import com.itech.itech_backend.repository.OtpVerificationRepository;
import com.itech.itech_backend.repository.UserRepository;
import com.itech.itech_backend.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final OtpVerificationRepository otpRepo;
    private final EmailService emailService;
    private final SmsService smsService;
    private final JwtUtil jwtUtil;

    public String register(RegisterRequestDto dto) {
        Optional<User> existingUser = userRepository.findByEmailOrPhone(dto.getEmail(), dto.getPhone());

        User user = existingUser.orElseGet(() -> {
            User newUser = User.builder()
                    .name(dto.getName())
                    .email(dto.getEmail())
                    .phone(dto.getPhone())
                    .build();
            return userRepository.save(newUser);
        });

        String otp = generateOtp();
        LocalDateTime expiry = LocalDateTime.now().plusMinutes(5);

        // Clean up old OTP records for this user
        if (dto.getEmail() != null) {
            otpRepo.deleteByEmailOrPhone(dto.getEmail());
        }
        if (dto.getPhone() != null) {
            otpRepo.deleteByEmailOrPhone(dto.getPhone());
        }
        // Store OTP for email if provided
        if (dto.getEmail() != null) {
            otpRepo.save(OtpVerification.builder()
                    .emailOrPhone(dto.getEmail())
                    .otp(otp)
                    .expiryTime(expiry)
                    .build());
        }
        
        // Store OTP for phone if provided  
        if (dto.getPhone() != null) {
            otpRepo.save(OtpVerification.builder()
                    .emailOrPhone(dto.getPhone())
                    .otp(otp)
                    .expiryTime(expiry)
                    .build());
        }

        if (dto.getEmail() != null) emailService.sendOtp(dto.getEmail(), otp);
        if (dto.getPhone() != null) smsService.sendOtp(dto.getPhone(), otp);

        return "OTP sent to your email and phone";
    }
    
    public String sendLoginOtp(String contact) {
        System.out.println("🔑 Login OTP request for: " + contact);
        
        // Check if user exists
        Optional<User> userOpt = userRepository.findByEmailOrPhone(contact, contact);
        if (!userOpt.isPresent()) {
            return "User not found. Please register first.";
        }
        
        User user = userOpt.get();
        System.out.println("👤 User found: " + user.getName());
        
        String otp = generateOtp();
        LocalDateTime expiry = LocalDateTime.now().plusMinutes(5);
        
        // Delete existing OTP
        otpRepo.deleteByEmailOrPhone(contact);
        
        // Save new OTP
        otpRepo.save(OtpVerification.builder()
                .emailOrPhone(contact)
                .otp(otp)
                .expiryTime(expiry)
                .build());
        
        System.out.println("🔢 Generated OTP: " + otp + " for " + contact);
        
        // Send OTP via email or SMS
        if (contact.contains("@")) {
            emailService.sendOtp(contact, otp);
            return "OTP sent to your email";
        } else {
            smsService.sendOtp(contact, otp);
            return "OTP sent to your phone";
        }
    }

    public JwtResponse verifyOtpAndGenerateToken(VerifyOtpRequestDto dto) {
        System.out.println("\n🔥 === OTP VERIFICATION STARTED ===");
        
        if (dto.getEmailOrPhone() == null || dto.getOtp() == null) {
            System.out.println("❌ Email/Phone or OTP is null");
            System.out.println("📧 EmailOrPhone: " + dto.getEmailOrPhone());
            System.out.println("🔢 OTP: " + dto.getOtp());
            return null;
        }

        String contact = dto.getEmailOrPhone().trim();
        System.out.println("➡️ Incoming OTP Request for: " + contact);
        System.out.println("🔢 Received OTP: " + dto.getOtp());

        // First check if user exists
        Optional<User> userOpt = userRepository.findByEmailOrPhone(contact, contact);
        if (!userOpt.isPresent()) {
            System.out.println("❌ User not found with contact: " + contact);
            return null;
        }
        
        User user = userOpt.get();
        System.out.println("👤 User Found: " + user.getName() + " | Email: " + user.getEmail() + " | Phone: " + user.getPhone());

        // Try to find OTP record - check both user's email and phone
        Optional<OtpVerification> otpOptional = otpRepo.findByEmailOrPhone(contact);
        
        if (!otpOptional.isPresent() && user.getEmail() != null) {
            System.out.println("🔍 Trying with user's email: " + user.getEmail());
            otpOptional = otpRepo.findByEmailOrPhone(user.getEmail());
        }
        
        if (!otpOptional.isPresent() && user.getPhone() != null) {
            System.out.println("🔍 Trying with user's phone: " + user.getPhone());
            otpOptional = otpRepo.findByEmailOrPhone(user.getPhone());
        }
        
        if (!otpOptional.isPresent()) {
            System.out.println("❌ No OTP found for user: " + user.getName() + " with any contact method");
        }

        if (otpOptional.isPresent()) {
            OtpVerification otp = otpOptional.get();

            System.out.println("✅ OTP Found in DB: " + otp.getOtp());
            System.out.println("⏳ Expiry Time: " + otp.getExpiryTime());
            System.out.println("🕒 Current Time: " + LocalDateTime.now());
            System.out.println("🔢 Input OTP: " + dto.getOtp());
            System.out.println("🔍 OTP Match: " + otp.getOtp().equals(dto.getOtp()));
            System.out.println("⏰ Time Valid: " + otp.getExpiryTime().isAfter(LocalDateTime.now()));

            if (otp.getOtp().equals(dto.getOtp()) && otp.getExpiryTime().isAfter(LocalDateTime.now())) {
                System.out.println("🔍 User Role: " + user.getRole());

                user.setVerified(true);
                userRepository.save(user);
                System.out.println("✅ User marked as verified");

                otpRepo.delete(otp);
                System.out.println("🧹 OTP entry deleted after verification");

                try {
                    String token = jwtUtil.generateToken(user.getEmail(), user.getRole());
                    System.out.println("✅ JWT Token Generated Successfully!");
                    System.out.println("🔐 Token: " + token.substring(0, 20) + "...");

                    JwtResponse response = new JwtResponse(token, "OTP Verified. Login Successful!");
                    System.out.println("🎉 === OTP VERIFICATION SUCCESSFUL ===");
                    return response;
                    
                } catch (Exception e) {
                    System.out.println("❌ Error generating JWT token: " + e.getMessage());
                    e.printStackTrace();
                    return null;
                }
                
            } else {
                if (!otp.getOtp().equals(dto.getOtp())) {
                    System.out.println("❌ OTP mismatch! Expected: " + otp.getOtp() + ", Got: " + dto.getOtp());
                }
                if (!otp.getExpiryTime().isAfter(LocalDateTime.now())) {
                    System.out.println("❌ OTP expired! Expiry: " + otp.getExpiryTime() + ", Now: " + LocalDateTime.now());
                }
            }
        } else {
            System.out.println("❌ No OTP record found for: " + contact);
            // Check all OTP records for debugging
            System.out.println("🔍 Checking all OTP records...");
            var allOtps = otpRepo.findAll();
            for (var otpRecord : allOtps) {
                System.out.println("📝 Found OTP record: " + otpRecord.getEmailOrPhone() + " -> " + otpRecord.getOtp());
            }
        }

        System.out.println("💥 === OTP VERIFICATION FAILED ===");
        return null;
    }



    private String generateOtp() {
        Random rand = new Random();
        return String.format("%06d", rand.nextInt(999999));
    }
}
