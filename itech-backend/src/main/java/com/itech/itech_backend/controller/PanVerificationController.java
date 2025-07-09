package com.itech.itech_backend.controller;

import com.itech.itech_backend.model.User;
import com.itech.itech_backend.model.VendorTaxProfile;
import com.itech.itech_backend.service.UserService;
import com.itech.itech_backend.service.VendorTaxService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/tax")
@RequiredArgsConstructor
@CrossOrigin
@Slf4j
public class PanVerificationController {

    private final VendorTaxService vendorTaxService;
    private final UserService userService;

    // PAN format validation regex
    private static final Pattern PAN_PATTERN = Pattern.compile("^[A-Z]{5}[0-9]{4}[A-Z]{1}$");
    
    // GST format validation regex
    private static final Pattern GST_PATTERN = Pattern.compile("^[0-9]{2}[A-Z]{5}[0-9]{4}[A-Z]{1}[1-9A-Z]{1}Z[0-9A-Z]{1}$");

    /**
     * Verify PAN number format and validity
     */
    @PostMapping("/verify-pan")
    public ResponseEntity<Map<String, Object>> verifyPan(@RequestBody Map<String, String> request) {
        String panNumber = request.get("panNumber");
        log.info("PAN verification request for: {}", panNumber);
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            if (panNumber == null || panNumber.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "PAN number is required");
                return ResponseEntity.badRequest().body(response);
            }
            
            panNumber = panNumber.toUpperCase().trim();
            
            // Validate PAN format
            if (!PAN_PATTERN.matcher(panNumber).matches()) {
                response.put("success", false);
                response.put("message", "Invalid PAN format. PAN should be 10 characters (5 letters, 4 digits, 1 letter)");
                return ResponseEntity.badRequest().body(response);
            }
            
            // TODO: Add actual PAN verification with government API
            // For now, we'll just validate the format
            response.put("success", true);
            response.put("message", "PAN number format is valid");
            response.put("panNumber", panNumber);
            response.put("verified", true);
            
            log.info("PAN verification successful for: {}", panNumber);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error verifying PAN: {}", e.getMessage());
            response.put("success", false);
            response.put("message", "PAN verification failed: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * Verify GST number format and validity
     */
    @PostMapping("/verify-gst")
    public ResponseEntity<Map<String, Object>> verifyGst(@RequestBody Map<String, String> request) {
        String gstNumber = request.get("gstNumber");
        log.info("GST verification request for: {}", gstNumber);
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            if (gstNumber == null || gstNumber.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "GST number is required");
                return ResponseEntity.badRequest().body(response);
            }
            
            gstNumber = gstNumber.toUpperCase().trim();
            
            // Validate GST format
            if (!GST_PATTERN.matcher(gstNumber).matches()) {
                response.put("success", false);
                response.put("message", "Invalid GST format. GST should be 15 characters");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Extract PAN from GST
            String extractedPan = gstNumber.substring(2, 12);
            
            // TODO: Add actual GST verification with government API
            // For now, we'll just validate the format
            response.put("success", true);
            response.put("message", "GST number format is valid");
            response.put("gstNumber", gstNumber);
            response.put("extractedPan", extractedPan);
            response.put("verified", true);
            
            log.info("GST verification successful for: {}", gstNumber);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error verifying GST: {}", e.getMessage());
            response.put("success", false);
            response.put("message", "GST verification failed: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * Verify and save vendor tax data
     */
    @PostMapping("/verify-vendor-tax")
    public ResponseEntity<Map<String, Object>> verifyVendorTaxData(@RequestBody Map<String, Object> request) {
        log.info("Vendor tax verification request: {}", request);
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            Long vendorId = Long.valueOf(request.get("vendorId").toString());
            String panNumber = (String) request.get("panNumber");
            String gstNumber = (String) request.get("gstNumber");
            String legalName = (String) request.get("legalName");
            
            if (panNumber != null) panNumber = panNumber.toUpperCase().trim();
            if (gstNumber != null) gstNumber = gstNumber.toUpperCase().trim();
            
            // Validate vendor exists
            User vendor = userService.getUserById(vendorId).orElse(null);
            if (vendor == null) {
                response.put("success", false);
                response.put("message", "Vendor not found");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Validate PAN format
            if (panNumber == null || !PAN_PATTERN.matcher(panNumber).matches()) {
                response.put("success", false);
                response.put("message", "Invalid PAN format");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Validate GST format
            if (gstNumber == null || !GST_PATTERN.matcher(gstNumber).matches()) {
                response.put("success", false);
                response.put("message", "Invalid GST format");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Check if PAN and GST match
            String extractedPan = gstNumber.substring(2, 12);
            if (!panNumber.equals(extractedPan)) {
                response.put("success", false);
                response.put("message", "PAN number does not match with GST number");
                response.put("extractedPan", extractedPan);
                return ResponseEntity.badRequest().body(response);
            }
            
            // Save tax data
            VendorTaxProfile taxProfile = vendorTaxService.saveTaxData(vendor, panNumber, gstNumber, legalName);
            
            response.put("success", true);
            response.put("message", "Tax information verified and saved successfully");
            response.put("taxProfile", taxProfile);
            
            log.info("Tax data saved successfully for vendor: {}", vendorId);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error saving vendor tax data: {}", e.getMessage());
            response.put("success", false);
            response.put("message", "Failed to save tax information: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * Get vendor tax profile
     */
    @GetMapping("/vendor/{vendorId}/profile")
    public ResponseEntity<VendorTaxProfile> getVendorTaxProfile(@PathVariable Long vendorId) {
        try {
            User vendor = userService.getUserById(vendorId).orElse(null);
            if (vendor == null) {
                return ResponseEntity.notFound().build();
            }
            
            // TODO: Implement method to get tax profile by vendor
            // VendorTaxProfile profile = vendorTaxService.getTaxProfileByVendor(vendor);
            // return ResponseEntity.ok(profile);
            
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error fetching vendor tax profile: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Update vendor tax profile
     */
    @PutMapping("/vendor/{vendorId}/profile")
    public ResponseEntity<Map<String, Object>> updateVendorTaxProfile(
            @PathVariable Long vendorId,
            @RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            User vendor = userService.getUserById(vendorId).orElse(null);
            if (vendor == null) {
                response.put("success", false);
                response.put("message", "Vendor not found");
                return ResponseEntity.status(404).body(response);
            }
            
            String panNumber = request.get("panNumber");
            String gstNumber = request.get("gstNumber");
            String legalName = request.get("legalName");
            
            VendorTaxProfile updatedProfile = vendorTaxService.saveTaxData(vendor, panNumber, gstNumber, legalName);
            
            response.put("success", true);
            response.put("message", "Tax profile updated successfully");
            response.put("taxProfile", updatedProfile);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error updating vendor tax profile: {}", e.getMessage());
            response.put("success", false);
            response.put("message", "Failed to update tax profile: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
}
