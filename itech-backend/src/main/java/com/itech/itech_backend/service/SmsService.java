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
    
    @Value("${msg91.api.key:your_msg91_api_key}")
    private String msg91ApiKey;
    
    @Value("${msg91.template.id:your_template_id}")
    private String msg91TemplateId;
    
    @Value("${textlocal.api.key:your_textlocal_api_key}")
    private String textlocalApiKey;
    
    @Value("${sms.simulation.enabled:true}")
    private boolean smsSimulationEnabled;
    
    private final RestTemplate restTemplate = new RestTemplate();
    
    public void sendOtp(String phone, String otp) {
        try {
            // Format phone number (add +91 for Indian numbers if not present)
            String formattedPhone = formatPhoneNumber(phone);
            String message = buildOtpSmsContent(otp);
            
            if (smsSimulationEnabled) {
                sendSimulatedSms(formattedPhone, otp);
                return;
            }
            
            // Try Indian SMS providers first for Indian numbers
            if (formattedPhone.startsWith("+91")) {
                if (isMsg91Configured()) {
                    sendViaMsg91(formattedPhone, otp);
                    return;
                } else if (isTextlocalConfigured()) {
                    sendViaTextlocal(formattedPhone, message);
                    return;
                }
            }
            
            // Fallback to Twilio for international numbers
            if (isTwilioConfigured()) {
                sendViaTwilio(formattedPhone, message);
            } else {
                // Final fallback to console
                sendSimulatedSms(formattedPhone, otp);
            }
            
        } catch (Exception e) {
            log.error("❌ Failed to send SMS OTP to: {} - Error: {}", phone, e.getMessage());
            // Final fallback to console for development
            sendSimulatedSms(phone, otp);
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
    
    private void sendViaMsg91(String phone, String otp) {
        try {
            String url = "https://api.msg91.com/api/v5/otp";
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("authkey", msg91ApiKey);
            
            String requestBody = String.format(
                "{\"template_id\":\"%s\",\"mobile\":\"%s\",\"authkey\":\"%s\",\"otp\":\"%s\"}",
                msg91TemplateId, phone.replace("+91", ""), msg91ApiKey, otp
            );
            
            HttpEntity<String> request = new HttpEntity<>(requestBody, headers);
            restTemplate.exchange(url, HttpMethod.POST, request, String.class);
            
            log.info("✅ SMS OTP sent successfully via MSG91 to: {}", phone);
            System.out.println("✅ Real SMS sent via MSG91 to: " + phone + " with OTP: " + otp);
            
        } catch (Exception e) {
            log.error("❌ MSG91 SMS failed: {}", e.getMessage());
            throw e;
        }
    }
    
    private void sendViaTextlocal(String phone, String message) {
        try {
            String url = "https://api.textlocal.in/send/";
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            
            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add("apikey", textlocalApiKey);
            body.add("numbers", phone.replace("+91", ""));
            body.add("message", message);
            body.add("sender", "TXTSMS");
            
            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);
            restTemplate.exchange(url, HttpMethod.POST, request, String.class);
            
            log.info("✅ SMS OTP sent successfully via Textlocal to: {}", phone);
            System.out.println("✅ Real SMS sent via Textlocal to: " + phone);
            
        } catch (Exception e) {
            log.error("❌ Textlocal SMS failed: {}", e.getMessage());
            throw e;
        }
    }
    
    private void sendSimulatedSms(String phone, String otp) {
        // Enhanced console display for development
        System.out.println("\n" + "=".repeat(80));
        System.out.println("📱 SIMULATED SMS SENT TO: " + phone);
        System.out.println("Provider: Development Mode");
        System.out.println("\n" + "-".repeat(80));
        System.out.println("SMS CONTENT:");
        System.out.println("-".repeat(80));
        System.out.println(buildOtpSmsContent(otp));
        System.out.println("-".repeat(80));
        System.out.println("\n🔑 YOUR OTP IS: " + otp);
        System.out.println("⏰ Valid for 5 minutes only!");
        System.out.println("\n💡 To enable real SMS sending:");
        System.out.println("1. Configure SMS provider in application.properties");
        System.out.println("2. Set sms.simulation.enabled=false");
        System.out.println("3. For Indian numbers: MSG91 or Textlocal");
        System.out.println("4. For International: Twilio");
        System.out.println("=".repeat(80) + "\n");
        
        log.info("📱 Simulated SMS sent to: {} with OTP: {}", phone, otp);
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
    
    private boolean isMsg91Configured() {
        return !"your_msg91_api_key".equals(msg91ApiKey) && 
               !"your_template_id".equals(msg91TemplateId);
    }
    
    private boolean isTextlocalConfigured() {
        return !"your_textlocal_api_key".equals(textlocalApiKey);
    }
}
