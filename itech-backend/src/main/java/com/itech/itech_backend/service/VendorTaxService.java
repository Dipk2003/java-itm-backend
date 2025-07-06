package com.itech.itech_backend.service;

import com.itech.itech_backend.dto.*;
import com.itech.itech_backend.model.*;
import com.itech.itech_backend.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class VendorTaxService {

    private final VendorTaxProfileRepository taxRepo;
    private final VendorGstSelectionRepository gstSelectionRepo;
    private final VendorTdsSelectionRepository tdsSelectionRepo;
    private final QuickoApiService quickoApiService;
    private final UserService userService;

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
     * Fetch GST details from Quicko API for a vendor
     */
    public Mono<QuickoGstDetailsDto> fetchVendorGstDetails(Long vendorId, String gstNumber) {
        log.info("Fetching GST details for vendor: {} with GST: {}", vendorId, gstNumber);
        return quickoApiService.fetchGstDetails(gstNumber)
                .doOnNext(gstDetails -> {
                    log.info("Successfully fetched GST details for vendor: {}", vendorId);
                })
                .doOnError(error -> {
                    log.error("Error fetching GST details for vendor: {} - {}", vendorId, error.getMessage());
                });
    }

    /**
     * Fetch TDS details from Quicko API for a vendor
     */
    public Mono<QuickoTdsDetailsDto> fetchVendorTdsDetails(Long vendorId, String panNumber) {
        log.info("Fetching TDS details for vendor: {} with PAN: {}", vendorId, panNumber);
        return quickoApiService.fetchTdsDetails(panNumber)
                .doOnNext(tdsDetails -> {
                    log.info("Successfully fetched TDS details for vendor: {}", vendorId);
                })
                .doOnError(error -> {
                    log.error("Error fetching TDS details for vendor: {} - {}", vendorId, error.getMessage());
                });
    }

    /**
     * Save vendor's GST and TDS selections
     */
    @Transactional
    public void saveVendorTaxSelections(VendorGstSelectionDto selectionDto) {
        User vendor = userService.getById(selectionDto.getVendorId());
        
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
        User vendor = userService.getById(vendorId);
        return gstSelectionRepo.findByVendorAndGstNumber(vendor, gstNumber);
    }

    /**
     * Get vendor's saved TDS selections
     */
    public List<VendorTdsSelection> getVendorTdsSelections(Long vendorId, String panNumber) {
        User vendor = userService.getById(vendorId);
        return tdsSelectionRepo.findByVendorAndPanNumber(vendor, panNumber);
    }

    /**
     * Get only selected GST rates for a vendor
     */
    public List<VendorGstSelection> getSelectedGstRates(Long vendorId, String gstNumber) {
        User vendor = userService.getById(vendorId);
        return gstSelectionRepo.findSelectedGstRatesByVendorAndGstNumber(vendor, gstNumber);
    }

    /**
     * Get only selected TDS rates for a vendor
     */
    public List<VendorTdsSelection> getSelectedTdsRates(Long vendorId, String panNumber) {
        User vendor = userService.getById(vendorId);
        return tdsSelectionRepo.findSelectedTdsRatesByVendorAndPanNumber(vendor, panNumber);
    }
}
