package com.itech.itech_backend.controller;

import com.itech.itech_backend.model.User;
import com.itech.itech_backend.model.VendorTaxProfile;
import com.itech.itech_backend.service.UserService;
import com.itech.itech_backend.service.VendorTaxService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/tax")
@RequiredArgsConstructor
@CrossOrigin
public class PanVerificationController {

    private final VendorTaxService vendorTaxService;
    private final UserService userService;

    @PostMapping("/verify-pan")
    public VendorTaxProfile verifyPanAndGst(@RequestParam Long vendorId,
                                            @RequestParam String pan,
                                            @RequestParam String gst,
                                            @RequestParam String legalName) {
        User vendor = userService.getById(vendorId);
        return vendorTaxService.saveTaxData(vendor, pan, gst, legalName);
    }
}
