package com.itech.itech_backend.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import java.util.Base64;

@Service
@Slf4j
public class SmsService {
    
    @Value("${twilio.account.sid:your_account_sid}")
    private String accountSid;
    
    @Value("${twilio.auth.token:your_auth_token}")
    private String authToken;
    
    @Value("${twilio.phone.number:+1234567890}")
    private String fromPhoneNumber;
    
    private final RestTemplate restTemplate = new RestTemplate();
    
    public void sendOtp(String phone, String otp) {
        try {
            // Format phone number (add +91 for Indian numbers if not present)
            String formattedPhone = formatPhoneNumber(phone);
            String message = buildOtpSmsContent(otp);
            
            // Try to send via Twilio first
            if (isTwilioConfigured()) {
                sendViaTwilio(formattedPhone, message);
            } else {
                // Fallback: Try alternative SMS service or console
                sendViaAlternative(formattedPhone, otp);
            }
            
        } catch (Exception e) {
            log.error("❌ Failed to send SMS OTP to: {} - Error: {}", phone, e.getMessage());
            // Final fallback to console for development
            System.out.println("📱 SMS OTP (Fallback) to " + phone + ": " + otp);
        }
    }
    
    private void sendViaTwilio(String phone, String message) {
        try {
            String url = String.format("https://api.twilio.com/2010-04-01/Accounts/%s/Messages.json", accountSid);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            headers.set("Authorization", "Basic " + 
                Base64.getEncoder().encodeToString((accountSid + ":" + authToken).getBytes()));
            
            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add("From", fromPhoneNumber);
            body.add("To", phone);
            body.add("Body", message);
            
            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);
            
            restTemplate.exchange(url, HttpMethod.POST, request, String.class);
            log.info("✅ SMS OTP sent successfully via Twilio to: {}", phone);
            
        } catch (Exception e) {
            log.error("❌ Twilio SMS failed: {}", e.getMessage());
            throw e;
        }
    }
    
    private void sendViaAlternative(String phone, String otp) {
        // Alternative SMS providers can be implemented here
        // For now, using console output for development
        log.info("📱 SMS Service (Development Mode) - OTP for {}: {}", phone, otp);
        System.out.println("📱 SMS OTP to " + phone + ": " + otp);
        System.out.println("ℹ️ Configure Twilio credentials in application.properties for real SMS sending");
    }
    
    private String formatPhoneNumber(String phone) {
        // Remove any non-digits
        String cleanPhone = phone.replaceAll("[^0-9]", "");
        
        // Add +91 for Indian numbers if not present
        if (cleanPhone.length() == 10 && cleanPhone.matches("[6-9][0-9]{9}")) {
            return "+91" + cleanPhone;
        }
        
        // If already has country code, return as is
        if (cleanPhone.startsWith("91") && cleanPhone.length() == 12) {
            return "+" + cleanPhone;
        }
        
        // Default: assume it's already formatted or add +91
        return phone.startsWith("+") ? phone : "+91" + cleanPhone;
    }
    
    private String buildOtpSmsContent(String otp) {
        return String.format(
            "Indian Trade Mart: Your OTP is %s. Valid for 5 minutes. Do not share with anyone.",
            otp
        );
    }
    
    private boolean isTwilioConfigured() {
        return !"your_account_sid".equals(accountSid) && 
               !"your_auth_token".equals(authToken) && 
               !"your_phone_number".equals(fromPhoneNumber);
    }
}
