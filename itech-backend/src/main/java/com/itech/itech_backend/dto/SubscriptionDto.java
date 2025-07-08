package com.itech.itech_backend.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubscriptionDto {
    private String planName;
    private String description;
    private Double price;
    private Integer durationDays;
    private String planType;
    private Integer maxProducts;
    private Integer maxLeads;
    private Boolean featuredListing;
    private Boolean prioritySupport;
    private Boolean analyticsAccess;
    private Boolean chatbotPriority;
    private Integer searchRanking;
}
