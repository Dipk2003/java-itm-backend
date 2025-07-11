package com.itech.itech_backend.service;

import com.itech.itech_backend.dto.*;
import com.itech.itech_backend.model.*;
import com.itech.itech_backend.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class VendorTaxService {

    private final VendorTaxProfileRepository taxRepo;
    private final VendorGstSelectionRepository gstSelectionRepo;
    private final VendorTdsSelectionRepository tdsSelectionRepo;
    private final UserService userService;
    
    @Autowired
    private GstVerificationService gstVerificationService;
    
    @Autowired
    private PanVerificationService panVerificationService;
    
    @Value("${gst.default.rates:0,5,12,18,28}")
    private String defaultGstRates;
    
    @Value("${gst.validation.enabled:true}")
    private boolean gstValidationEnabled;

    public VendorTaxProfile saveTaxData(User vendor, String pan, String gst, String name) {
        VendorTaxProfile profile = VendorTaxProfile.builder()
                .vendor(vendor)
                .panNumber(pan)
                .gstNumber(gst)
                .legalName(name)
                .build();
        return taxRepo.save(profile);
    }

    /**
     * Validate GST number format
     */
    public boolean validateGstNumber(String gstNumber) {
        if (!gstValidationEnabled) {
            return true;
        }
        
        return gstVerificationService.validateGstFormat(gstNumber);
    }
    
    /**
     * Validate PAN number format
     */
    public boolean validatePanNumber(String panNumber) {
        return panVerificationService.validatePanFormat(panNumber);
    }
    
    /**
     * Verify GST number with external API
     */
    public CompletableFuture<Map<String, Object>> verifyGstNumber(String gstNumber) {
        return gstVerificationService.verifyGstNumber(gstNumber);
    }
    
    /**
     * Verify PAN number with external API
     */
    public CompletableFuture<Map<String, Object>> verifyPanNumber(String panNumber) {
        return panVerificationService.verifyPanNumber(panNumber);
    }
    
    /**
     * Get GST details from external API
     */
    public CompletableFuture<Map<String, Object>> getGstDetails(String gstNumber) {
        return gstVerificationService.getGstDetails(gstNumber);
    }
    
    /**
     * Get PAN details from external API
     */
    public CompletableFuture<Map<String, Object>> getPanDetails(String panNumber) {
        return panVerificationService.getPanDetails(panNumber);
    }
    
    /**
     * Get available GST rates
     */
    public List<Double> getAvailableGstRates() {
        return Arrays.stream(defaultGstRates.split(","))
                .map(String::trim)
                .map(Double::parseDouble)
                .collect(Collectors.toList());
    }

    /**
     * Save vendor's GST and TDS selections
     */
    @Transactional
    public void saveVendorTaxSelections(VendorGstSelectionDto selectionDto) {
        User vendor = userService.getUserById(selectionDto.getVendorId()).orElseThrow(() -> new RuntimeException("Vendor not found"));
        
        // Clear existing selections for this GST number
        gstSelectionRepo.deleteByVendorAndGstNumber(vendor, selectionDto.getGstNumber());
        
        // Save new GST selections
        if (selectionDto.getSelectedGstRates() != null) {
            List<VendorGstSelection> gstSelections = selectionDto.getSelectedGstRates().stream()
                    .map(rate -> VendorGstSelection.builder()
                            .vendor(vendor)
                            .gstNumber(selectionDto.getGstNumber())
                            .category(rate.getCategory())
                            .description(rate.getDescription())
                            .rate(rate.getRate())
                            .hsn(rate.getHsn())
                            .taxType(rate.getTaxType())
                            .isSelected(rate.isSelected())
                            .build())
                    .collect(Collectors.toList());
            
            gstSelectionRepo.saveAll(gstSelections);
            log.info("Saved {} GST selections for vendor: {}", gstSelections.size(), selectionDto.getVendorId());
        }
        
        // Save TDS selections if provided
        if (selectionDto.getSelectedTdsRates() != null) {
            // Extract PAN from GST or use vendor's PAN
            String panNumber = selectionDto.getGstNumber().substring(2, 12);
            
            // Clear existing TDS selections for this PAN
            tdsSelectionRepo.deleteByVendorAndPanNumber(vendor, panNumber);
            
            List<VendorTdsSelection> tdsSelections = selectionDto.getSelectedTdsRates().stream()
                    .map(rate -> VendorTdsSelection.builder()
                            .vendor(vendor)
                            .panNumber(panNumber)
                            .section(rate.getSection())
                            .description(rate.getDescription())
                            .rate(rate.getRate())
                            .paymentType(rate.getPaymentType())
                            .categoryCode(rate.getCategoryCode())
                            .natureOfPayment(rate.getNatureOfPayment())
                            .isSelected(rate.isSelected())
                            .build())
                    .collect(Collectors.toList());
            
            tdsSelectionRepo.saveAll(tdsSelections);
            log.info("Saved {} TDS selections for vendor: {}", tdsSelections.size(), selectionDto.getVendorId());
        }
    }

    /**
     * Get vendor's saved GST selections
     */
    public List<VendorGstSelection> getVendorGstSelections(Long vendorId, String gstNumber) {
        User vendor = userService.getUserById(vendorId).orElseThrow(() -> new RuntimeException("Vendor not found"));
        return gstSelectionRepo.findByVendorAndGstNumber(vendor, gstNumber);
    }

    /**
     * Get vendor's saved TDS selections
     */
    public List<VendorTdsSelection> getVendorTdsSelections(Long vendorId, String panNumber) {
        User vendor = userService.getUserById(vendorId).orElseThrow(() -> new RuntimeException("Vendor not found"));
        return tdsSelectionRepo.findByVendorAndPanNumber(vendor, panNumber);
    }

    /**
     * Get only selected GST rates for a vendor
     */
    public List<VendorGstSelection> getSelectedGstRates(Long vendorId, String gstNumber) {
        User vendor = userService.getUserById(vendorId).orElseThrow(() -> new RuntimeException("Vendor not found"));
        return gstSelectionRepo.findSelectedGstRatesByVendorAndGstNumber(vendor, gstNumber);
    }

    /**
     * Get only selected TDS rates for a vendor
     */
    public List<VendorTdsSelection> getSelectedTdsRates(Long vendorId, String panNumber) {
        User vendor = userService.getUserById(vendorId).orElseThrow(() -> new RuntimeException("Vendor not found"));
        return tdsSelectionRepo.findSelectedTdsRatesByVendorAndPanNumber(vendor, panNumber);
    }
}
