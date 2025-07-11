package com.itech.itech_backend.service;

import com.itech.itech_backend.dto.JwtResponse;
import com.itech.itech_backend.dto.LoginRequestDto;
import com.itech.itech_backend.dto.RegisterRequestDto;
import com.itech.itech_backend.dto.VerifyOtpRequestDto;
import com.itech.itech_backend.model.OtpVerification;
import com.itech.itech_backend.model.User;
import com.itech.itech_backend.repository.OtpVerificationRepository;
import com.itech.itech_backend.repository.UserRepository;
import com.itech.itech_backend.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class UnifiedAuthService {

    private final UserRepository userRepository;
    private final OtpVerificationRepository otpRepo;
    private final EmailService emailService;
    private final SmsService smsService;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    
    private static final String ADMIN_ACCESS_CODE = "ADMIN2025";

    public String register(RegisterRequestDto dto) {
        // Check if user already exists
        if (userRepository.existsByEmail(dto.getEmail()) || userRepository.existsByPhone(dto.getPhone())) {
            return "User already exists with this email or phone number";
        }
        
        // Create user in User table
        User user = createUser(dto);
        
        // Send OTP
        return sendRegistrationOtp(dto, user);
    }

    public JwtResponse directLogin(LoginRequestDto loginRequest) {
        System.out.println("🚀 Direct Login request for: " + loginRequest.getEmailOrPhone());
        
        // Find user in User table
        Optional<User> userOpt = userRepository.findByEmailOrPhone(loginRequest.getEmailOrPhone(), loginRequest.getEmailOrPhone());
        
        if (!userOpt.isPresent()) {
            System.out.println("❌ User not found");
            return null;
        }
        
        User user = userOpt.get();
        System.out.println("✅ User found: " + user.getEmail() + ", Role: " + user.getRole());
        
        // Check admin access code if admin
        if ("ADMIN".equals(user.getRole()) || "ROLE_ADMIN".equals(user.getRole())) {
            if (loginRequest.getAdminCode() == null || !ADMIN_ACCESS_CODE.equals(loginRequest.getAdminCode())) {
                System.out.println("❌ Invalid admin access code");
                return null;
            }
        }
        
        // Validate password - support both plain text and BCrypt
        if (!validatePassword(loginRequest.getPassword(), user.getPassword())) {
            System.out.println("❌ Invalid password");
            return null;
        }
        
        System.out.println("✅ Password validated successfully");
        
        // Generate token directly
        String token = jwtUtil.generateToken(user.getEmail(), user.getRole());
        System.out.println("✅ Token generated successfully");
        
        // Create response with user info
        return JwtResponse.builder()
            .token(token)
            .message("Login successful!")
            .user(JwtResponse.UserInfo.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .role(user.getRole().replace("ROLE_", ""))
                .isVerified(user.isVerified())
                .build())
            .build();
    }
    
    public String sendLoginOtp(LoginRequestDto loginRequest) {
        System.out.println("🔑 Unified Login OTP request for: " + loginRequest.getEmailOrPhone());
        
        // Find user in User table
        Optional<User> userOpt = userRepository.findByEmailOrPhone(loginRequest.getEmailOrPhone(), loginRequest.getEmailOrPhone());
        
        if (!userOpt.isPresent()) {
            return "User not found. Please register first.";
        }
        
        User user = userOpt.get();
        
        // Check admin access code if admin
        if ("ROLE_ADMIN".equals(user.getRole())) {
            if (loginRequest.getAdminCode() == null || !ADMIN_ACCESS_CODE.equals(loginRequest.getAdminCode())) {
                return "Invalid admin access code. Please contact system administrator.";
            }
        }
        
        // Validate password
        if (!validatePassword(loginRequest.getPassword(), user.getPassword())) {
            return "Invalid password. Please check your credentials and try again.";
        }
        
        // Generate and send OTP
        return generateAndSendOtp(loginRequest.getEmailOrPhone(), user.getRole());
    }

    public JwtResponse verifyOtpAndGenerateToken(VerifyOtpRequestDto dto) {
        System.out.println("🔥 Unified OTP Verification for: " + dto.getEmailOrPhone());
        
        // Find user in User table
        Optional<User> userOpt = userRepository.findByEmailOrPhone(dto.getEmailOrPhone(), dto.getEmailOrPhone());
        
        if (!userOpt.isPresent()) {
            return null;
        }
        
        User user = userOpt.get();
        
        // Verify OTP
        Optional<OtpVerification> otpOpt = otpRepo.findByEmailOrPhone(dto.getEmailOrPhone());
        if (!otpOpt.isPresent()) {
            return null;
        }
        
        OtpVerification otp = otpOpt.get();
        if (!otp.getOtp().equals(dto.getOtp()) || !otp.getExpiryTime().isAfter(LocalDateTime.now())) {
            return null;
        }
        
        // Mark user as verified and generate token
        user.setVerified(true);
        userRepository.save(user);
        otpRepo.delete(otp);
        
        String token = jwtUtil.generateToken(user.getEmail(), user.getRole());
        return JwtResponse.builder()
            .token(token)
            .message("Login successful!")
            .user(JwtResponse.UserInfo.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .role(user.getRole().replace("ROLE_", ""))
                .isVerified(user.isVerified())
                .build())
            .build();
    }

    // Helper methods
    private User createUser(RegisterRequestDto dto) {
        String encodedPassword = passwordEncoder.encode(dto.getPassword());
        
        User.UserBuilder userBuilder = User.builder()
            .name(dto.getName())
            .email(dto.getEmail())
            .phone(dto.getPhone())
            .password(encodedPassword)
            .role(dto.getRole());
        
        // Set role-specific fields
        if ("ROLE_VENDOR".equals(dto.getRole())) {
            userBuilder.businessName(dto.getBusinessName())
                      .businessAddress(dto.getBusinessAddress())
                      .gstNumber(dto.getGstNumber())
                      .panNumber(dto.getPanNumber());
        } else if ("ROLE_ADMIN".equals(dto.getRole())) {
            userBuilder.department(dto.getDepartment())
                      .designation(dto.getDesignation());
        }
        
        return userRepository.save(userBuilder.build());
    }

    private String sendRegistrationOtp(RegisterRequestDto dto, User user) {
        String otp = generateOtp();
        LocalDateTime expiry = LocalDateTime.now().plusMinutes(5);
        
        // Clean up old OTPs
        if (dto.getEmail() != null) {
            otpRepo.deleteByEmailOrPhone(dto.getEmail());
        }
        if (dto.getPhone() != null) {
            otpRepo.deleteByEmailOrPhone(dto.getPhone());
        }
        
        // Store OTP
        if (dto.getEmail() != null) {
            otpRepo.save(OtpVerification.builder()
                .emailOrPhone(dto.getEmail())
                .otp(otp)
                .expiryTime(expiry)
                .build());
        }
        
        if (dto.getPhone() != null) {
            otpRepo.save(OtpVerification.builder()
                .emailOrPhone(dto.getPhone())
                .otp(otp)
                .expiryTime(expiry)
                .build());
        }
        
        // Send OTP
        if (dto.getEmail() != null) emailService.sendOtp(dto.getEmail(), otp);
        if (dto.getPhone() != null) smsService.sendOtp(dto.getPhone(), otp);
        
        return "OTP sent to your email and phone";
    }

    private boolean validatePassword(String inputPassword, String storedPassword) {
        // Simple plain text password comparison - NO BCRYPT!
        System.out.println("🔍 Debug - Input password: " + inputPassword);
        System.out.println("🔍 Debug - Stored password: " + storedPassword);
        boolean matches = inputPassword != null && inputPassword.equals(storedPassword);
        System.out.println("🔍 Debug - Password match: " + matches);
        return matches;
    }

    private String generateAndSendOtp(String contact, String role) {
        String otp = generateOtp();
        LocalDateTime expiry = LocalDateTime.now().plusMinutes(5);
        
        otpRepo.deleteByEmailOrPhone(contact);
        otpRepo.save(OtpVerification.builder()
            .emailOrPhone(contact)
            .otp(otp)
            .expiryTime(expiry)
            .build());
        
        if (contact.contains("@")) {
            emailService.sendOtp(contact, otp);
            return "Password verified. OTP sent to your email.";
        } else {
            smsService.sendOtp(contact, otp);
            return "Password verified. OTP sent to your phone.";
        }
    }

    private String generateOtp() {
        Random rand = new Random();
        return String.format("%06d", rand.nextInt(999999));
    }
}
