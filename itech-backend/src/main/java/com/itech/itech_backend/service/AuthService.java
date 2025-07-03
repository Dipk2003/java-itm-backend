package com.itech.itech_backend.service;

import com.itech.itech_backend.model.OtpVerification;
import com.itech.itech_backend.model.User;
import com.itech.itech_backend.repository.OtpVerificationRepository;
import com.itech.itech_backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OtpVerificationRepository otpRepo;

    public String registerWithEmail(String name, String email) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        User user = userOpt.orElseGet(() -> new User(name, email, null, "ROLE_VENDOR", false));
        userRepository.save(user);
        return sendOtp(email);
    }

    public String registerWithPhone(String name, String phone) {
        Optional<User> userOpt = userRepository.findByPhone(phone);
        User user = userOpt.orElseGet(() -> new User(name, null, phone, "ROLE_VENDOR", false));
        userRepository.save(user);
        return sendOtp(phone);
    }

    public boolean verifyOTP(String emailOrPhone, String otp) {
        Optional<OtpVerification> otpRecord = otpRepo.findByEmailOrPhone(emailOrPhone);
        if (otpRecord.isPresent() && otpRecord.get().getOtp().equals(otp)) {
            if (otpRecord.get().getExpiryTime().isAfter(LocalDateTime.now())) {
                User user = userRepository.findByEmail(emailOrPhone)
                        .orElseGet(() -> userRepository.findByPhone(emailOrPhone).orElseThrow());
                user.setVerified(true);
                userRepository.save(user);
                return true;
            }
        }
        return false;
    }

    private String sendOtp(String emailOrPhone) {
        String otp = String.valueOf(new Random().nextInt(900000) + 100000);
        OtpVerification record = new OtpVerification();
        record.setEmailOrPhone(emailOrPhone);
        record.setOtp(otp);
        record.setExpiryTime(LocalDateTime.now().plusMinutes(10));
        otpRepo.save(record);
        return "OTP sent to " + emailOrPhone + ": " + otp;
    }
}