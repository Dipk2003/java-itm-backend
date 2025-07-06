package com.itech.itech_backend.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatbotResponseDto {
    
    private String response;
    private String sessionId;
    private List<VendorRecommendationDto> recommendations;
    private boolean hasRecommendations;
    
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class VendorRecommendationDto {
        private Long vendorId;
        private String vendorName;
        private String vendorEmail;
        private String vendorPhone;
        private String vendorType;
        private Double performanceScore;
        private List<String> products;
        private List<String> categories;
        private String reason; // Why this vendor is recommended
    }
}
