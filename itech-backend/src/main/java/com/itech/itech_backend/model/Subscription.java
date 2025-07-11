package com.itech.itech_backend.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Subscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String planName;
    
    private String description;
    
    private Double price;
    
    private Integer durationDays;
    
    @Enumerated(EnumType.STRING)
    private PlanType planType;
    
    // Features
    private Integer maxProducts;
    
    private Integer maxLeads;
    
    private Boolean featuredListing;
    
    private Boolean prioritySupport;
    
    private Boolean analyticsAccess;
    
    private Boolean chatbotPriority;
    
    private Integer searchRanking; // 1 = highest priority
    
    // Vendor subscription details
    @ManyToOne
    @JoinColumn(name = "vendor_id")
    private User vendor;
    
    private LocalDateTime startDate;
    
    private LocalDateTime endDate;
    
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private SubscriptionStatus status = SubscriptionStatus.ACTIVE;
    
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
    
    public enum PlanType {
        BASIC, SILVER, GOLD, PLATINUM, DIAMOND
    }
    
    public enum SubscriptionStatus {
        ACTIVE, EXPIRED, CANCELLED, PENDING
    }
}
