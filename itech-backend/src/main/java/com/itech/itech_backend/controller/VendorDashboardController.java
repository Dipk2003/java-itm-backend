package com.itech.itech_backend.controller;

import com.itech.itech_backend.dto.*;
import com.itech.itech_backend.model.*;
import com.itech.itech_backend.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/vendor")
@RequiredArgsConstructor
@CrossOrigin
@Slf4j
public class VendorDashboardController {

    private final VendorRankingService rankingService;
    private final UserService userService;
    private final VendorTaxService vendorTaxService;

    @GetMapping("/{vendorId}/ranking")
    public VendorRanking getVendorRank(@PathVariable Long vendorId) {
        User vendor = userService.getUserById(vendorId).orElseThrow(() -> new RuntimeException("Vendor not found"));
        return rankingService.getOrCreateRanking(vendor);
    }

    /**
     * Fetch GST details from Quicko API
     */
    @GetMapping("/{vendorId}/gst/{gstNumber}/details")
    public Mono<ResponseEntity<QuickoGstDetailsDto>> fetchGstDetails(
            @PathVariable Long vendorId,
            @PathVariable String gstNumber) {
        log.info("Fetching GST details for vendor: {} with GST: {}", vendorId, gstNumber);
        
        return vendorTaxService.fetchVendorGstDetails(vendorId, gstNumber)
                .map(ResponseEntity::ok)
                .onErrorReturn(ResponseEntity.internalServerError().build());
    }

    /**
     * Fetch TDS details from Quicko API
     */
    @GetMapping("/{vendorId}/tds/{panNumber}/details")
    public Mono<ResponseEntity<QuickoTdsDetailsDto>> fetchTdsDetails(
            @PathVariable Long vendorId,
            @PathVariable String panNumber) {
        log.info("Fetching TDS details for vendor: {} with PAN: {}", vendorId, panNumber);
        
        return vendorTaxService.fetchVendorTdsDetails(vendorId, panNumber)
                .map(ResponseEntity::ok)
                .onErrorReturn(ResponseEntity.internalServerError().build());
    }

    /**
     * Save vendor's GST and TDS selections
     */
    @PostMapping("/{vendorId}/tax-selections")
    public ResponseEntity<Map<String, String>> saveVendorTaxSelections(
            @PathVariable Long vendorId,
            @RequestBody VendorGstSelectionDto selectionDto) {
        log.info("Saving tax selections for vendor: {}", vendorId);
        
        try {
            selectionDto.setVendorId(vendorId);
            vendorTaxService.saveVendorTaxSelections(selectionDto);
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Tax selections saved successfully");
            response.put("status", "success");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error saving tax selections for vendor: {} - {}", vendorId, e.getMessage());
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Failed to save tax selections: " + e.getMessage());
            response.put("status", "error");
            
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Get vendor's saved GST selections
     */
    @GetMapping("/{vendorId}/gst/{gstNumber}/selections")
    public ResponseEntity<List<VendorGstSelection>> getVendorGstSelections(
            @PathVariable Long vendorId,
            @PathVariable String gstNumber) {
        log.info("Getting GST selections for vendor: {} with GST: {}", vendorId, gstNumber);
        
        try {
            List<VendorGstSelection> selections = vendorTaxService.getVendorGstSelections(vendorId, gstNumber);
            return ResponseEntity.ok(selections);
        } catch (Exception e) {
            log.error("Error getting GST selections for vendor: {} - {}", vendorId, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get vendor's saved TDS selections
     */
    @GetMapping("/{vendorId}/tds/{panNumber}/selections")
    public ResponseEntity<List<VendorTdsSelection>> getVendorTdsSelections(
            @PathVariable Long vendorId,
            @PathVariable String panNumber) {
        log.info("Getting TDS selections for vendor: {} with PAN: {}", vendorId, panNumber);
        
        try {
            List<VendorTdsSelection> selections = vendorTaxService.getVendorTdsSelections(vendorId, panNumber);
            return ResponseEntity.ok(selections);
        } catch (Exception e) {
            log.error("Error getting TDS selections for vendor: {} - {}", vendorId, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get vendor's selected GST rates for sale
     */
    @GetMapping("/{vendorId}/gst/{gstNumber}/selected-rates")
    public ResponseEntity<List<VendorGstSelection>> getSelectedGstRates(
            @PathVariable Long vendorId,
            @PathVariable String gstNumber) {
        log.info("Getting selected GST rates for vendor: {} with GST: {}", vendorId, gstNumber);
        
        try {
            List<VendorGstSelection> selectedRates = vendorTaxService.getSelectedGstRates(vendorId, gstNumber);
            return ResponseEntity.ok(selectedRates);
        } catch (Exception e) {
            log.error("Error getting selected GST rates for vendor: {} - {}", vendorId, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get vendor's selected TDS rates
     */
    @GetMapping("/{vendorId}/tds/{panNumber}/selected-rates")
    public ResponseEntity<List<VendorTdsSelection>> getSelectedTdsRates(
            @PathVariable Long vendorId,
            @PathVariable String panNumber) {
        log.info("Getting selected TDS rates for vendor: {} with PAN: {}", vendorId, panNumber);
        
        try {
            List<VendorTdsSelection> selectedRates = vendorTaxService.getSelectedTdsRates(vendorId, panNumber);
            return ResponseEntity.ok(selectedRates);
        } catch (Exception e) {
            log.error("Error getting selected TDS rates for vendor: {} - {}", vendorId, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get comprehensive vendor tax dashboard data
     */
    @GetMapping("/{vendorId}/tax-dashboard")
    public ResponseEntity<Map<String, Object>> getVendorTaxDashboard(
            @PathVariable Long vendorId,
            @RequestParam(required = false) String gstNumber,
            @RequestParam(required = false) String panNumber) {
        log.info("Getting tax dashboard data for vendor: {}", vendorId);
        
        try {
            Map<String, Object> dashboardData = new HashMap<>();
            
            // Get vendor info
            User vendor = userService.getUserById(vendorId).orElseThrow(() -> new RuntimeException("Vendor not found"));
            dashboardData.put("vendor", vendor);
            
            // Get ranking
            VendorRanking ranking = rankingService.getOrCreateRanking(vendor);
            dashboardData.put("ranking", ranking);
            
            // Get GST selections if GST number provided
            if (gstNumber != null && !gstNumber.isEmpty()) {
                List<VendorGstSelection> gstSelections = vendorTaxService.getVendorGstSelections(vendorId, gstNumber);
                List<VendorGstSelection> selectedGstRates = vendorTaxService.getSelectedGstRates(vendorId, gstNumber);
                dashboardData.put("gstSelections", gstSelections);
                dashboardData.put("selectedGstRates", selectedGstRates);
            }
            
            // Get TDS selections if PAN number provided
            if (panNumber != null && !panNumber.isEmpty()) {
                List<VendorTdsSelection> tdsSelections = vendorTaxService.getVendorTdsSelections(vendorId, panNumber);
                List<VendorTdsSelection> selectedTdsRates = vendorTaxService.getSelectedTdsRates(vendorId, panNumber);
                dashboardData.put("tdsSelections", tdsSelections);
                dashboardData.put("selectedTdsRates", selectedTdsRates);
            }
            
            return ResponseEntity.ok(dashboardData);
        } catch (Exception e) {
            log.error("Error getting tax dashboard data for vendor: {} - {}", vendorId, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
}
