package com.itech.itech_backend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    
    @Value("${spring.mail.username:noreply@indiantradeMart.com}")
    private String fromEmail;
    
    public void sendOtp(String email, String otp) {
        try {
            // For now, we'll simulate email sending for development
            System.out.println("\n" + "=".repeat(60));
            System.out.println("📧 EMAIL SENT TO: " + email);
            System.out.println("From: " + fromEmail);
            System.out.println("Subject: Indian Trade Mart - OTP Verification");
            System.out.println("\nContent:");
            System.out.println(buildOtpEmailContent(otp));
            System.out.println("\n🔑 OTP: " + otp);
            System.out.println("=".repeat(60) + "\n");
            
            // In production, you can integrate with:
            // - SendGrid API
            // - AWS SES
            // - Mailgun
            // - Gmail SMTP (with proper authentication)
            
        } catch (Exception e) {
            System.out.println("❌ Failed to send email to: " + email + " - Error: " + e.getMessage());
            System.out.println("📧 Email OTP (Fallback) to " + email + ": " + otp);
        }
    }
    
    private String buildOtpEmailContent(String otp) {
        return String.format(
            "Dear User,\n\n" +
            "Welcome to Indian Trade Mart!\n\n" +
            "Your One-Time Password (OTP) for verification is: %s\n\n" +
            "This OTP is valid for 5 minutes only.\n\n" +
            "If you didn't request this OTP, please ignore this email.\n\n" +
            "Best regards,\n" +
            "Indian Trade Mart Team\n" +
            "\n" +
            "Note: Please do not reply to this email as it is auto-generated.",
            otp
        );
    }
}
