package com.itech.itech_backend.controller;

import com.itech.itech_backend.dto.SubscriptionDto;
import com.itech.itech_backend.model.Subscription;
import com.itech.itech_backend.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/subscriptions")
@RequiredArgsConstructor
@CrossOrigin
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    // Get all subscription plans
    @GetMapping("/plans")
    public ResponseEntity<List<Subscription>> getAllPlans() {
        List<Subscription> plans = subscriptionService.getAllPlans();
        return ResponseEntity.ok(plans);
    }

    // Vendor subscribes to a plan
    @PostMapping("/vendor/subscribe")
    @PreAuthorize("hasRole('VENDOR')")
    public ResponseEntity<String> subscribeToPlan(@RequestBody SubscriptionDto subscriptionDto) {
        subscriptionService.subscribeToPlan(subscriptionDto);
        return ResponseEntity.ok("Subscription successful");
    }

    // Get vendor's current subscription
    @GetMapping("/vendor/current")
    @PreAuthorize("hasRole('VENDOR')")
    public ResponseEntity<Subscription> getCurrentSubscription() {
        Subscription subscription = subscriptionService.getCurrentVendorSubscription();
        return ResponseEntity.ok(subscription);
    }

    // Admin creates a new subscription plan
    @PostMapping("/admin/create-plan")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Subscription> createPlan(@RequestBody SubscriptionDto planDto) {
        Subscription plan = subscriptionService.createPlan(planDto);
        return ResponseEntity.ok(plan);
    }

    // Admin gets subscription analytics
    @GetMapping("/admin/analytics")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getSubscriptionAnalytics() {
        return ResponseEntity.ok(subscriptionService.getSubscriptionAnalytics());
    }

    // Get vendor subscription history
    @GetMapping("/vendor/history")
    @PreAuthorize("hasRole('VENDOR')")
    public ResponseEntity<List<Subscription>> getSubscriptionHistory() {
        List<Subscription> history = subscriptionService.getVendorSubscriptionHistory();
        return ResponseEntity.ok(history);
    }
}
