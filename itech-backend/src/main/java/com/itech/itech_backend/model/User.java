package com.itech.itech_backend.model;

import com.itech.itech_backend.enums.VendorType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(unique = true)
    private String email;

    @Column(unique = true)
    private String phone;

    private boolean isVerified = false;

    @Enumerated(EnumType.STRING)
    private VendorType vendorType = VendorType.BASIC;

    private String role = "ROLE_VENDOR";

    private LocalDateTime createdAt = LocalDateTime.now();
}
