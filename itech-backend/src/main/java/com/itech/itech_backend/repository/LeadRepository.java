package com.itech.itech_backend.repository;

import com.itech.itech_backend.model.Lead;
import com.itech.itech_backend.model.User;
import com.itech.itech_backend.enums.LeadStatus;
import com.itech.itech_backend.enums.LeadPriority;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface LeadRepository extends JpaRepository<Lead, Long> {
    
    // Find leads by vendor
    List<Lead> findByVendor(User vendor);
    
    // Find leads by vendor and status
    List<Lead> findByVendorAndStatus(User vendor, LeadStatus status);
    
    // Find leads by vendor and priority
    List<Lead> findByVendorAndPriority(User vendor, LeadPriority priority);
    
    // Find leads by vendor with follow-up date before given date
    List<Lead> findByVendorAndNextFollowUpDateBefore(User vendor, LocalDateTime date);
    
    // Count leads by vendor and status
    long countByVendorAndStatus(User vendor, LeadStatus status);
    
    // Find recent leads for vendor (last 30 days)
    @Query("SELECT l FROM Lead l WHERE l.vendor = :vendor AND l.inquiryDate >= :thirtyDaysAgo ORDER BY l.inquiryDate DESC")
    List<Lead> findRecentLeadsByVendor(@Param("vendor") User vendor, @Param("thirtyDaysAgo") LocalDateTime thirtyDaysAgo);
    
    // Get lead statistics for vendor
    @Query("SELECT l.status, COUNT(l) FROM Lead l WHERE l.vendor = :vendor GROUP BY l.status")
    List<Object[]> getLeadStatsByVendor(@Param("vendor") User vendor);
    
    // Find leads by customer email or phone
    List<Lead> findByCustomerEmailOrCustomerPhone(String email, String phone);
    
    // Search leads by customer name containing
    List<Lead> findByVendorAndCustomerNameContainingIgnoreCase(User vendor, String customerName);
}
