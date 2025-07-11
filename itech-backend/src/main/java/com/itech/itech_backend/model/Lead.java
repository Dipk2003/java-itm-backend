package com.itech.itech_backend.model;

import com.itech.itech_backend.enums.LeadStatus;
import com.itech.itech_backend.enums.LeadPriority;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "leads")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Lead {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String customerName;
    
    @Column(unique = false)
    private String customerEmail;
    
    private String customerPhone;
    
    @ManyToOne
    private Product product; // Product the customer is interested in
    
    @ManyToOne
    private User vendor; // Vendor who owns this lead
    
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private LeadStatus status = LeadStatus.NEW;
    
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private LeadPriority priority = LeadPriority.MEDIUM;
    
    private Double estimatedValue;
    
    @Column(length = 1000)
    private String notes;
    
    @Column(length = 500)
    private String inquiryMessage;
    
    @Builder.Default
    private LocalDateTime inquiryDate = LocalDateTime.now();
    
    private LocalDateTime lastContactDate;
    
    private LocalDateTime nextFollowUpDate;
    
    @Builder.Default
    private LocalDateTime lastUpdated = LocalDateTime.now();
    
    private String customerCompany;
    
    private String productInterest;
}
