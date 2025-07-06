package com.itech.itech_backend.controller;

import com.itech.itech_backend.enums.VendorType;
import com.itech.itech_backend.model.User;
import com.itech.itech_backend.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@CrossOrigin
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/vendors")
    public List<User> getAllVendors() {
        return adminService.getAllVendors();
    }

    @PutMapping("/vendor/{userId}/type")
    public User updateVendorType(@PathVariable Long userId, @RequestParam VendorType vendorType) {
        return adminService.updateVendorType(userId, vendorType.name());
    }
}
