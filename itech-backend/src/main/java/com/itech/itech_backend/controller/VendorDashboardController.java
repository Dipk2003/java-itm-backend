package com.itech.itech_backend.controller;

import com.itech.itech_backend.model.User;
import com.itech.itech_backend.model.VendorRanking;
import com.itech.itech_backend.service.UserService;
import com.itech.itech_backend.service.VendorRankingService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/vendor")
@RequiredArgsConstructor
@CrossOrigin
public class VendorDashboardController {

    private final VendorRankingService rankingService;
    private final UserService userService;

    @GetMapping("/{vendorId}/ranking")
    public VendorRanking getVendorRank(@PathVariable Long vendorId) {
        User vendor = userService.getById(vendorId);
        return rankingService.getOrCreateRanking(vendor);
    }
}
