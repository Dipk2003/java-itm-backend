package com.itech.itech_backend.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "vendor_gst_selection")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VendorGstSelection {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vendor_id", nullable = false)
    private User vendor;
    
    @Column(name = "gst_number", nullable = false)
    private String gstNumber;
    
    @Column(name = "category")
    private String category;
    
    @Column(name = "description")
    private String description;
    
    @Column(name = "rate")
    private Double rate;
    
    @Column(name = "hsn")
    private String hsn;
    
    @Column(name = "tax_type")
    private String taxType;
    
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
