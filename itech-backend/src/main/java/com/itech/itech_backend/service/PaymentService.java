package com.itech.itech_backend.service;

import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class PaymentService {

    @Value("${razorpay.key.id:}")
    private String razorpayKeyId;

    @Value("${razorpay.key.secret:}")
    private String razorpayKeySecret;

    @Value("${razorpay.currency:INR}")
    private String currency;

    private RazorpayClient razorpayClient;

    private RazorpayClient getRazorpayClient() throws RazorpayException {
        if (razorpayClient == null) {
            razorpayClient = new RazorpayClient(razorpayKeyId, razorpayKeySecret);
        }
        return razorpayClient;
    }

    public Map<String, Object> createOrder(String orderNumber, BigDecimal amount, String customerEmail, String customerPhone) {
        try {
            RazorpayClient client = getRazorpayClient();
            
            JSONObject orderRequest = new JSONObject();
            orderRequest.put("amount", amount.multiply(BigDecimal.valueOf(100)).intValue()); // Amount in paise
            orderRequest.put("currency", currency);
            orderRequest.put("receipt", orderNumber);
            
            // Customer details
            JSONObject notes = new JSONObject();
            notes.put("email", customerEmail);
            notes.put("phone", customerPhone);
            orderRequest.put("notes", notes);
            
            Order order = client.orders.create(orderRequest);
            
            Map<String, Object> response = new HashMap<>();
            response.put("razorpayOrderId", order.get("id"));
            response.put("amount", order.get("amount"));
            response.put("currency", order.get("currency"));
            response.put("key", razorpayKeyId);
            
            log.info("Razorpay order created successfully: {}", order.get("id").toString());
            return response;
            
        } catch (RazorpayException e) {
            log.error("Error creating Razorpay order", e);
            throw new RuntimeException("Failed to create payment order: " + e.getMessage());
        }
    }

    public boolean verifyPayment(String razorpayOrderId, String razorpayPaymentId, String signature) {
        try {
            // Create signature verification string
            String generatedSignature = hmacSha256(razorpayOrderId + "|" + razorpayPaymentId, razorpayKeySecret);
            
            boolean isValid = generatedSignature.equals(signature);
            log.info("Payment verification result: {} for order: {}", isValid, razorpayOrderId);
            
            return isValid;
        } catch (Exception e) {
            log.error("Error verifying payment", e);
            return false;
        }
    }

    public Map<String, Object> getPaymentDetails(String paymentId) {
        try {
            RazorpayClient client = getRazorpayClient();
            com.razorpay.Payment payment = client.payments.fetch(paymentId);
            
            Map<String, Object> details = new HashMap<>();
            details.put("id", payment.get("id"));
            details.put("amount", payment.get("amount"));
            details.put("currency", payment.get("currency"));
            details.put("status", payment.get("status"));
            details.put("method", payment.get("method"));
            details.put("created_at", payment.get("created_at"));
            
            return details;
        } catch (RazorpayException e) {
            log.error("Error fetching payment details", e);
            throw new RuntimeException("Failed to fetch payment details: " + e.getMessage());
        }
    }

    public boolean refundPayment(String paymentId, BigDecimal amount) {
        try {
            RazorpayClient client = getRazorpayClient();
            
            JSONObject refundRequest = new JSONObject();
            if (amount != null) {
                refundRequest.put("amount", amount.multiply(BigDecimal.valueOf(100)).intValue());
            }
            
            com.razorpay.Refund refund = client.payments.refund(paymentId, refundRequest);
            
            log.info("Refund created successfully: {}", refund.get("id").toString());
            return true;
        } catch (RazorpayException e) {
            log.error("Error creating refund", e);
            return false;
        }
    }

    private String hmacSha256(String data, String key) {
        try {
            javax.crypto.Mac mac = javax.crypto.Mac.getInstance("HmacSHA256");
            javax.crypto.spec.SecretKeySpec secretKeySpec = new javax.crypto.spec.SecretKeySpec(key.getBytes(), "HmacSHA256");
            mac.init(secretKeySpec);
            byte[] hash = mac.doFinal(data.getBytes());
            
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException("Error generating HMAC SHA256", e);
        }
    }
}
