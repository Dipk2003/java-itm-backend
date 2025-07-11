package com.itech.itech_backend.controller;

import com.itech.itech_backend.model.UserAddress;
import com.itech.itech_backend.service.UserAddressService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/user/addresses")
@CrossOrigin
@RequiredArgsConstructor
@Slf4j
public class UserAddressController {

    private final UserAddressService userAddressService;

    @GetMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<UserAddress>> getUserAddresses() {
        try {
            Long userId = getCurrentUserId();
            List<UserAddress> addresses = userAddressService.getUserAddresses(userId);
            return ResponseEntity.ok(addresses);
        } catch (Exception e) {
            log.error("Error getting user addresses", e);
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> createAddress(@RequestBody UserAddress address) {
        try {
            Long userId = getCurrentUserId();
            UserAddress createdAddress = userAddressService.createAddress(userId, address);
            return ResponseEntity.ok(createdAddress);
        } catch (Exception e) {
            log.error("Error creating address", e);
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    @PutMapping("/{addressId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> updateAddress(@PathVariable Long addressId, @RequestBody UserAddress address) {
        try {
            Long userId = getCurrentUserId();
            UserAddress updatedAddress = userAddressService.updateAddress(userId, addressId, address);
            return ResponseEntity.ok(updatedAddress);
        } catch (Exception e) {
            log.error("Error updating address", e);
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    @DeleteMapping("/{addressId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> deleteAddress(@PathVariable Long addressId) {
        try {
            Long userId = getCurrentUserId();
            userAddressService.deleteAddress(userId, addressId);
            return ResponseEntity.ok("Address deleted successfully");
        } catch (Exception e) {
            log.error("Error deleting address", e);
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/{addressId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<UserAddress> getAddressById(@PathVariable Long addressId) {
        try {
            Long userId = getCurrentUserId();
            UserAddress address = userAddressService.getAddressById(userId, addressId);
            return ResponseEntity.ok(address);
        } catch (Exception e) {
            log.error("Error getting address", e);
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{addressId}/set-default")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> setDefaultAddress(@PathVariable Long addressId) {
        try {
            Long userId = getCurrentUserId();
            UserAddress address = userAddressService.setDefaultAddress(userId, addressId);
            return ResponseEntity.ok(address);
        } catch (Exception e) {
            log.error("Error setting default address", e);
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/default")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<UserAddress> getDefaultAddress() {
        try {
            Long userId = getCurrentUserId();
            Optional<UserAddress> defaultAddress = userAddressService.getDefaultAddress(userId);
            return defaultAddress.map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            log.error("Error getting default address", e);
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/type/{addressType}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<UserAddress>> getAddressesByType(@PathVariable String addressType) {
        try {
            Long userId = getCurrentUserId();
            List<UserAddress> addresses = userAddressService.getAddressesByType(userId, addressType);
            return ResponseEntity.ok(addresses);
        } catch (Exception e) {
            log.error("Error getting addresses by type", e);
            return ResponseEntity.badRequest().build();
        }
    }

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        
        // TODO: Implement proper user ID extraction from email
        // This is a placeholder - you should implement this properly
        return 1L;
    }
}
