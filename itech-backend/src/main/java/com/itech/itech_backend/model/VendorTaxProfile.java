package com.itech.itech_backend.model;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
public class VendorTaxProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @OneToOne
    private User vendor;

    private String pan;
    private String selectedGstin;
    private boolean tdsStatus;
    private String itrStatus;

    @Lob
    private String allGstinsJson;
}
