package com.itech.itech_backend.service;

import com.itech.itech_backend.dto.SubscriptionDto;
import com.itech.itech_backend.model.Subscription;
import com.itech.itech_backend.model.User;
import com.itech.itech_backend.repository.SubscriptionRepository;
import com.itech.itech_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final UserRepository userRepository;

    /**
     * Get all subscription plans
     */
    public List<Subscription> getAllPlans() {
        return subscriptionRepository.findByVendorIsNull();
    }

    /**
     * Vendor subscribes to a plan
     */
    @Transactional
    public void subscribeToPlan(SubscriptionDto subscriptionDto) {
        // Get current user from security context
        String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        User vendor = userRepository.findByEmailOrPhone(userEmail, userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Cancel any existing active subscription
        subscriptionRepository.findByVendorAndStatus(vendor, Subscription.SubscriptionStatus.ACTIVE)
                .ifPresent(existingSubscription -> {
                    existingSubscription.setStatus(Subscription.SubscriptionStatus.CANCELLED);
                    subscriptionRepository.save(existingSubscription);
                });

        // Create new subscription
        Subscription subscription = Subscription.builder()
                .planName(subscriptionDto.getPlanName())
                .description(subscriptionDto.getDescription())
                .price(subscriptionDto.getPrice())
                .durationDays(subscriptionDto.getDurationDays())
                .planType(Subscription.PlanType.valueOf(subscriptionDto.getPlanType().toUpperCase()))
                .maxProducts(subscriptionDto.getMaxProducts())
                .maxLeads(subscriptionDto.getMaxLeads())
                .featuredListing(subscriptionDto.getFeaturedListing())
                .prioritySupport(subscriptionDto.getPrioritySupport())
                .analyticsAccess(subscriptionDto.getAnalyticsAccess())
                .chatbotPriority(subscriptionDto.getChatbotPriority())
                .searchRanking(subscriptionDto.getSearchRanking())
                .vendor(vendor)
                .startDate(LocalDateTime.now())
                .endDate(LocalDateTime.now().plusDays(subscriptionDto.getDurationDays()))
                .status(Subscription.SubscriptionStatus.ACTIVE)
                .build();

        subscriptionRepository.save(subscription);
    }

    /**
     * Get current vendor's active subscription
     */
    public Subscription getCurrentVendorSubscription() {
        String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        User vendor = userRepository.findByEmailOrPhone(userEmail, userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return subscriptionRepository.findByVendorAndStatus(vendor, Subscription.SubscriptionStatus.ACTIVE)
                .orElseThrow(() -> new RuntimeException("No active subscription found"));
    }

    /**
     * Admin creates a new subscription plan
     */
    @Transactional
    public Subscription createPlan(SubscriptionDto planDto) {
        Subscription plan = Subscription.builder()
                .planName(planDto.getPlanName())
                .description(planDto.getDescription())
                .price(planDto.getPrice())
                .durationDays(planDto.getDurationDays())
                .planType(Subscription.PlanType.valueOf(planDto.getPlanType().toUpperCase()))
                .maxProducts(planDto.getMaxProducts())
                .maxLeads(planDto.getMaxLeads())
                .featuredListing(planDto.getFeaturedListing())
                .prioritySupport(planDto.getPrioritySupport())
                .analyticsAccess(planDto.getAnalyticsAccess())
                .chatbotPriority(planDto.getChatbotPriority())
                .searchRanking(planDto.getSearchRanking())
                .vendor(null) // Plans have no vendor
                .status(Subscription.SubscriptionStatus.ACTIVE)
                .build();

        return subscriptionRepository.save(plan);
    }

    /**
     * Get subscription analytics for admin
     */
    public Map<String, Object> getSubscriptionAnalytics() {
        Map<String, Object> analytics = new HashMap<>();
        
        // Get subscription counts by status
        List<Object[]> statusCounts = subscriptionRepository.getSubscriptionStatusCounts();
        Map<String, Long> statusAnalytics = new HashMap<>();
        for (Object[] row : statusCounts) {
            statusAnalytics.put(row[0].toString(), (Long) row[1]);
        }
        analytics.put("statusCounts", statusAnalytics);
        
        // Get subscription counts by plan type
        List<Object[]> planTypeCounts = subscriptionRepository.getSubscriptionPlanTypeCounts();
        Map<String, Long> planTypeAnalytics = new HashMap<>();
        for (Object[] row : planTypeCounts) {
            planTypeAnalytics.put(row[0].toString(), (Long) row[1]);
        }
        analytics.put("planTypeCounts", planTypeAnalytics);
        
        // Get total revenue
        List<Subscription> allSubscriptions = subscriptionRepository.findAll();
        double totalRevenue = allSubscriptions.stream()
                .filter(s -> s.getVendor() != null)
                .mapToDouble(s -> s.getPrice() != null ? s.getPrice() : 0.0)
                .sum();
        analytics.put("totalRevenue", totalRevenue);
        
        return analytics;
    }

    /**
     * Get vendor's subscription history
     */
    public List<Subscription> getVendorSubscriptionHistory() {
        String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        User vendor = userRepository.findByEmailOrPhone(userEmail, userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return subscriptionRepository.findByVendorOrderByCreatedAtDesc(vendor);
    }
}
