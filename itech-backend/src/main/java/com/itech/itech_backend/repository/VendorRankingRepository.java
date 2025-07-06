package com.itech.itech_backend.repository;

import com.itech.itech_backend.model.VendorRanking;
import com.itech.itech_backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface VendorRankingRepository extends JpaRepository<VendorRanking, Long> {
    Optional<VendorRanking> findByVendor(User vendor);
    
    @Query("SELECT vr FROM VendorRanking vr WHERE vr.vendor.id = :vendorId")
    Optional<VendorRanking> findByVendorId(@Param("vendorId") Long vendorId);
}
