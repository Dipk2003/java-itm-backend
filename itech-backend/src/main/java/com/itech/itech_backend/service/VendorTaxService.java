package com.itech.itech_backend.service;

import com.itech.itech_backend.model.User;
import com.itech.itech_backend.model.VendorTaxProfile;
import com.itech.itech_backend.repository.VendorTaxProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class VendorTaxService {

    private final VendorTaxProfileRepository taxRepo;

    public VendorTaxProfile saveTaxData(User vendor, String pan, String gst, String name) {
        VendorTaxProfile profile = VendorTaxProfile.builder()
                .vendor(vendor)
                .panNumber(pan)
                .gstNumber(gst)
                .legalName(name)
                .build();
        return taxRepo.save(profile);
    }

    // Quicko API integration can be added here
}
