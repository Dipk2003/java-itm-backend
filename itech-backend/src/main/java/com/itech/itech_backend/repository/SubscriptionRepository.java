package com.itech.itech_backend.repository;

import com.itech.itech_backend.model.Subscription;
import com.itech.itech_backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
    
    // Find active subscription for a vendor
    Optional<Subscription> findByVendorAndStatus(User vendor, Subscription.SubscriptionStatus status);
    
    // Find all subscriptions for a vendor
    List<Subscription> findByVendorOrderByCreatedAtDesc(User vendor);
    
    // Find all subscription plans (where vendor is null)
    List<Subscription> findByVendorIsNull();
    
    // Get subscription analytics
    @Query("SELECT s.status, COUNT(s) FROM Subscription s WHERE s.vendor IS NOT NULL GROUP BY s.status")
    List<Object[]> getSubscriptionStatusCounts();
    
    @Query("SELECT s.planType, COUNT(s) FROM Subscription s WHERE s.vendor IS NOT NULL GROUP BY s.planType")
    List<Object[]> getSubscriptionPlanTypeCounts();
}
