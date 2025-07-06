package com.itech.itech_backend.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VendorTaxProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "vendor_id", referencedColumnName = "id", nullable = false, unique = true)
    private User vendor;

    private String panNumber;
    private String gstNumber;
    private String legalName;
    private String businessType;
    private String status;
}
