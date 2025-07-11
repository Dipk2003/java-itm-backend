package com.itech.itech_backend.service;

import com.itech.itech_backend.dto.JwtResponse;
import com.itech.itech_backend.dto.LoginRequestDto;
import com.itech.itech_backend.dto.RegisterRequestDto;
import com.itech.itech_backend.dto.SetPasswordDto;
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
public class AuthService {

    private final UserRepository userRepository;
    private final OtpVerificationRepository otpRepo;
    private final EmailService emailService;
    private final SmsService smsService;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    
    // Admin access code - in production, this should be in environment variables
    private static final String ADMIN_ACCESS_CODE = "ADMIN2025";

    public String register(RegisterRequestDto dto) {
        Optional<User> existingUser = userRepository.findByEmailOrPhone(dto.getEmail(), dto.getPhone());

        User user;
        if (existingUser.isPresent()) {
            user = existingUser.get();
            // Update password for existing user if provided
            if (dto.getPassword() != null && !dto.getPassword().isEmpty()) {
                user.setPassword(dto.getPassword());
            }
            // Ensure role is set correctly for existing user
            String userRole = (dto.getRole() != null && !dto.getRole().isEmpty()) 
                            ? dto.getRole() 
                            : "ROLE_VENDOR";
            user.setRole(userRole);
            user = userRepository.save(user);
            System.out.println("✅ Updated existing user: " + user.getName() + " with role: " + user.getRole());
        } else {
            // Use role from DTO or default to ROLE_VENDOR
            String userRole = (dto.getRole() != null && !dto.getRole().isEmpty()) 
                            ? dto.getRole() 
                            : "ROLE_VENDOR";
            
            user = User.builder()
                    .name(dto.getName())
                    .email(dto.getEmail())
                    .phone(dto.getPhone())
                    .password(dto.getPassword())
                    .role(userRole)  // Use role from DTO or default
                    .build();
            user = userRepository.save(user);
            System.out.println("✅ Created new user: " + user.getName() + " with role: " + user.getRole());
        }

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

        if (dto.getEmail() != null) {
            System.out.println("📧 SENDING EMAIL OTP to: " + dto.getEmail());
            emailService.sendOtp(dto.getEmail(), otp);
        }
        if (dto.getPhone() != null) {
            System.out.println("📱 SENDING SMS OTP to: " + dto.getPhone());
            smsService.sendOtp(dto.getPhone(), otp);
        }

        return "OTP sent to your email and phone";
    }
    
    public String sendLoginOtp(LoginRequestDto loginRequest) {
        System.out.println("🔑 Login OTP request for: " + loginRequest.getEmailOrPhone());
        
        // Check if user exists
        Optional<User> userOpt = userRepository.findByEmailOrPhone(loginRequest.getEmailOrPhone(), loginRequest.getEmailOrPhone());
        if (!userOpt.isPresent()) {
            System.out.println("❌ User not found: " + loginRequest.getEmailOrPhone());
            return "User not found. Please register first.";
        }
        
        User user = userOpt.get();
        System.out.println("👤 User found: " + user.getName() + " | Email: " + user.getEmail());
        
        // Check if this is an admin login attempt
        if ("ROLE_ADMIN".equals(user.getRole()) || "ADMIN".equals(user.getRole())) {
            System.out.println("🔐 Admin login attempt detected");
            
            // Verify admin access code
            if (loginRequest.getAdminCode() == null || !ADMIN_ACCESS_CODE.equals(loginRequest.getAdminCode())) {
                System.out.println("❌ Invalid or missing admin access code");
                return "Invalid admin access code. Please contact system administrator.";
            }
            
            System.out.println("✅ Admin access code verified");
        }
        
        // Check if user has a password set
        if (user.getPassword() == null || user.getPassword().isEmpty()) {
            System.out.println("⚠️ User " + user.getName() + " has no password set. Please contact support.");
            return "Account setup incomplete. Please contact support to set up your password.";
        }
        
        // Validate password - REQUIRED for OTP generation
        System.out.println("🔍 Validating password for user: " + user.getName());
        if (loginRequest.getPassword() == null || loginRequest.getPassword().trim().isEmpty()) {
            System.out.println("❌ Password is required for login");
            return "Password is required for login.";
        }
        
        boolean passwordMatches = loginRequest.getPassword().equals(user.getPassword());
        System.out.println("🔒 Password validation result: " + passwordMatches);
        System.out.println("🔍 Stored password: " + user.getPassword());
        System.out.println("🔍 Input password: " + loginRequest.getPassword());
        
        if (!passwordMatches) {
            System.out.println("❌ Invalid password for user: " + user.getName());
            return "Invalid password. Please check your credentials and try again.";
        }
        
        System.out.println("✅ Password validation successful for user: " + user.getName());
        
        String contact = loginRequest.getEmailOrPhone();
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
            System.out.println("📧 LOGIN: SENDING EMAIL OTP to: " + contact);
            emailService.sendOtp(contact, otp);
            return "Password verified. OTP sent to your email.";
        } else {
            System.out.println("📱 LOGIN: SENDING SMS OTP to: " + contact);
            smsService.sendOtp(contact, otp);
            return "Password verified. OTP sent to your phone.";
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
        
        // Ensure user has correct role before OTP verification
        ensureUserHasCorrectRole(user);

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
                System.out.println("🔍 User Role BEFORE JWT: " + user.getRole());

                user.setVerified(true);
                userRepository.save(user);
                System.out.println("✅ User marked as verified");

                otpRepo.delete(otp);
                System.out.println("🧹 OTP entry deleted after verification");

                try {
                    // Re-fetch user to ensure we have the latest role
                    User refreshedUser = userRepository.findByEmailOrPhone(contact, contact).orElse(user);
                    System.out.println("🔄 Refreshed User Role: " + refreshedUser.getRole());
                    
                    String token = jwtUtil.generateToken(refreshedUser.getEmail(), refreshedUser.getRole(), refreshedUser.getId());
                    System.out.println("✅ JWT Token Generated Successfully with role: " + refreshedUser.getRole() + " and user ID: " + refreshedUser.getId());
                    System.out.println("🔐 Token: " + token.substring(0, 20) + "...");

                    JwtResponse response = JwtResponse.builder()
                        .token(token)
                        .message("OTP Verified. Login Successful!")
                        .user(JwtResponse.UserInfo.builder()
                            .id(refreshedUser.getId())
                            .email(refreshedUser.getEmail())
                            .name(refreshedUser.getName())
                            .role(refreshedUser.getRole().replace("ROLE_", ""))
                            .isVerified(refreshedUser.isVerified())
                            .build())
                        .build();
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
    
    // Method to ensure user has correct role
    private void ensureUserHasCorrectRole(User user) {
        System.out.println("🔍 Current user role: " + user.getRole() + " for user: " + user.getName());
        
        // Don't change admin roles
        if ("ROLE_ADMIN".equals(user.getRole()) || "ADMIN".equals(user.getRole())) {
            System.out.println("🔐 User is admin, keeping role: " + user.getRole());
            return;
        }
        
        // For non-admin users, set role to ROLE_VENDOR if not already set
        if (user.getRole() == null || user.getRole().isEmpty() || user.getRole().equals("ROLE_USER")) {
            System.out.println("🔄 Fixing user role from " + user.getRole() + " to ROLE_VENDOR");
            user.setRole("ROLE_VENDOR");
            userRepository.save(user);
            System.out.println("✅ Fixed user role to ROLE_VENDOR for: " + user.getName());
        } else {
            System.out.println("✅ User already has correct role: " + user.getRole());
        }
    }
    
    public String setPassword(SetPasswordDto dto) {
        System.out.println("🔑 Setting password for: " + dto.getEmailOrPhone());
        
        Optional<User> userOpt = userRepository.findByEmailOrPhone(dto.getEmailOrPhone(), dto.getEmailOrPhone());
        if (!userOpt.isPresent()) {
            return "User not found.";
        }
        
        User user = userOpt.get();
user.setPassword(dto.getNewPassword());
        userRepository.save(user);
        
        System.out.println("✅ Password set successfully for user: " + user.getName());
        return "Password set successfully. You can now login.";
    }
    
    public String debugUser(String email) {
        Optional<User> userOpt = userRepository.findByEmailOrPhone(email, email);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            return "Debug User: " + user.getName() + 
                   " | Email: " + user.getEmail() + 
                   " | Role: " + user.getRole() + 
                   " | Verified: " + user.isVerified() + 
                   " | VendorType: " + user.getVendorType();
        }
        return "User not found: " + email;
    }
}
