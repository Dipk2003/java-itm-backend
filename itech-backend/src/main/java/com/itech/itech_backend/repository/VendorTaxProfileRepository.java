package com.itech.itech_backend.repository;

import com.itech.itech_backend.model.VendorTaxProfile;
import com.itech.itech_backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface VendorTaxProfileRepository extends JpaRepository<VendorTaxProfile, UUID> {
    Optional<VendorTaxProfile> findByVendor(User vendor);
}
