package com.itech.itech_backend.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "vendor_tds_selection")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VendorTdsSelection {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vendor_id", nullable = false)
    private User vendor;
    
    @Column(name = "pan_number", nullable = false)
    private String panNumber;
    
    @Column(name = "section")
    private String section;
    
    @Column(name = "description")
    private String description;
    
    @Column(name = "rate")
    private Double rate;
    
    @Column(name = "payment_type")
    private String paymentType;
    
    @Column(name = "category_code")
    private String categoryCode;
    
    @Column(name = "nature_of_payment")
    private String natureOfPayment;
    
    @Column(name = "is_selected")
    private boolean isSelected;
    
    @Column(name = "created_at")
    private java.time.LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private java.time.LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = java.time.LocalDateTime.now();
        updatedAt = java.time.LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = java.time.LocalDateTime.now();
    }
}
