package com.itech.itech_backend.controller;

import com.itech.itech_backend.dto.ExcelImportResponseDto;
import com.itech.itech_backend.service.ExcelImportService;

import com.itech.itech_backend.enums.VendorType;
import com.itech.itech_backend.model.User;
import com.itech.itech_backend.service.AdminService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@CrossOrigin
@Slf4j
public class AdminController {

    private final AdminService adminService;
    private final ExcelImportService excelImportService;

    @GetMapping("/vendors")
    public List<User> getAllVendors() {
        return adminService.getAllVendors();
    }

    @PutMapping("/vendor/{userId}/type")
    public User updateVendorType(@PathVariable Long userId, @RequestParam VendorType vendorType) {
        return adminService.updateVendorType(userId, vendorType.name());
    }
    /**
    * Bulk import products from Excel file
    */
   @PostMapping(value = "/products/bulk-import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
   @PreAuthorize("hasRole('ADMIN')")
   public ResponseEntity<?> adminBulkImportProducts(
           @RequestParam("file") MultipartFile excelFile,
           @RequestParam("vendorId") Long vendorId) {
       try {
           // Validate file
           if (excelFile.isEmpty()) {
               return ResponseEntity.badRequest().body("Please select an Excel file to upload");
           }

           String fileName = excelFile.getOriginalFilename();
           if (fileName == null || (!fileName.endsWith(".xlsx") && !fileName.endsWith(".xls"))) {
               return ResponseEntity.badRequest().body("Please upload a valid Excel file (.xlsx or .xls)");
           }

           log.info("Starting admin bulk import for vendor: {} with file: {}", vendorId, fileName);

           ExcelImportResponseDto response = excelImportService.importProductsFromExcel(excelFile, vendorId);

           if (response.getSuccess()) {
               return ResponseEntity.ok(response);
           } else {
               return ResponseEntity.badRequest().body(response);
           }

       } catch (Exception e) {
           log.error("Error during admin bulk import for vendor: {}", vendorId, e);
           return ResponseEntity.badRequest().body("Import failed: " + e.getMessage());
       }
   }


}
