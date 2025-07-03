package com.itech.itech_backend.controller;

import com.itech.itech_backend.service.VendorTaxService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/pan")
@CrossOrigin
public class PanVerificationController {

    @Autowired
    private VendorTaxService vendorTaxService;

    @PostMapping("/verify")
    public Map<String, Object> verifyPan(@RequestBody Map<String, String> payload) {
        String pan = payload.get("pan");
        String dob = payload.get("dob");
        return vendorTaxService.fetchPanTaxData(pan, dob);
    }
}
