package com.itech.itech_backend.dto;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CheckoutDto {
    private Long addressId;
    private String paymentMethod; // RAZORPAY, COD, etc.
    private String couponCode;
    private String notes;
    
    // If address is not saved, provide address details
    private AddressDto shippingAddress;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AddressDto {
        private String fullName;
        private String addressLine1;
        private String addressLine2;
        private String city;
        private String state;
        private String pincode;
        private String phone;
    }
}
