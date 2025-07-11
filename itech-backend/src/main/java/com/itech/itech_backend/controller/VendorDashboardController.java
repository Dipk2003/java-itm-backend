package com.itech.itech_backend.controller;

import com.itech.itech_backend.dto.*;
import com.itech.itech_backend.model.*;
import com.itech.itech_backend.repository.ProductImageRepository;
import com.itech.itech_backend.service.*;
import com.itech.itech_backend.util.JwtTokenUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
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
    private final ExcelImportService excelImportService;
    private final FileUploadService fileUploadService;
    private final ProductImageRepository productImageRepository;
    private final ProductService productService;
    private final JwtTokenUtil jwtTokenUtil;

    @GetMapping("/{vendorId}/ranking")
    public VendorRanking getVendorRank(@PathVariable Long vendorId) {
        User vendor = userService.getUserById(vendorId).orElseThrow(() -> new RuntimeException("Vendor not found"));
        return rankingService.getOrCreateRanking(vendor);
    }

    /**
     * Validate GST number format
     */
    @GetMapping("/{vendorId}/gst/{gstNumber}/validate")
    public ResponseEntity<Map<String, Object>> validateGstNumber(
            @PathVariable Long vendorId,
            @PathVariable String gstNumber) {
        log.info("Validating GST number for vendor: {} with GST: {}", vendorId, gstNumber);
        
        Map<String, Object> response = new HashMap<>();
        boolean isValid = vendorTaxService.validateGstNumber(gstNumber);
        
        response.put("valid", isValid);
        response.put("gstNumber", gstNumber);
        response.put("message", isValid ? "Valid GST number format" : "Invalid GST number format");
        
        return ResponseEntity.ok(response);
    }

    /**
     * Verify GST number with external API
     */
    @GetMapping("/{vendorId}/gst/{gstNumber}/verify")
    public ResponseEntity<Map<String, Object>> verifyGstNumber(
            @PathVariable Long vendorId,
            @PathVariable String gstNumber) {
        log.info("Verifying GST number for vendor: {} with GST: {}", vendorId, gstNumber);
        
        try {
            Map<String, Object> response = vendorTaxService.verifyGstNumber(gstNumber).get();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error verifying GST number: {}", gstNumber, e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("valid", false);
            errorResponse.put("error", "Verification failed: " + e.getMessage());
            errorResponse.put("gstNumber", gstNumber);
            return ResponseEntity.ok(errorResponse);
        }
    }

    /**
     * Get GST details from external API
     */
    @GetMapping("/{vendorId}/gst/{gstNumber}/details")
    public ResponseEntity<Map<String, Object>> getGstDetails(
            @PathVariable Long vendorId,
            @PathVariable String gstNumber) {
        log.info("Getting GST details for vendor: {} with GST: {}", vendorId, gstNumber);
        
        try {
            Map<String, Object> response = vendorTaxService.getGstDetails(gstNumber).get();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error getting GST details: {}", gstNumber, e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("valid", vendorTaxService.validateGstNumber(gstNumber));
            errorResponse.put("error", "Details unavailable: " + e.getMessage());
            errorResponse.put("gstNumber", gstNumber);
            return ResponseEntity.ok(errorResponse);
        }
    }

    /**
     * Validate PAN number format
     */
    @GetMapping("/{vendorId}/pan/{panNumber}/validate")
    public ResponseEntity<Map<String, Object>> validatePanNumber(
            @PathVariable Long vendorId,
            @PathVariable String panNumber) {
        log.info("Validating PAN number for vendor: {} with PAN: {}", vendorId, panNumber);
        
        Map<String, Object> response = new HashMap<>();
        boolean isValid = vendorTaxService.validatePanNumber(panNumber);
        
        response.put("valid", isValid);
        response.put("panNumber", panNumber);
        response.put("message", isValid ? "Valid PAN number format" : "Invalid PAN number format");
        
        return ResponseEntity.ok(response);
    }

    /**
     * Verify PAN number with external API
     */
    @GetMapping("/{vendorId}/pan/{panNumber}/verify")
    public ResponseEntity<Map<String, Object>> verifyPanNumber(
            @PathVariable Long vendorId,
            @PathVariable String panNumber) {
        log.info("Verifying PAN number for vendor: {} with PAN: {}", vendorId, panNumber);
        
        try {
            Map<String, Object> response = vendorTaxService.verifyPanNumber(panNumber).get();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error verifying PAN number: {}", panNumber, e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("valid", false);
            errorResponse.put("error", "Verification failed: " + e.getMessage());
            errorResponse.put("panNumber", panNumber);
            return ResponseEntity.ok(errorResponse);
        }
    }

    /**
     * Get PAN details from external API
     */
    @GetMapping("/{vendorId}/pan/{panNumber}/details")
    public ResponseEntity<Map<String, Object>> getPanDetails(
            @PathVariable Long vendorId,
            @PathVariable String panNumber) {
        log.info("Getting PAN details for vendor: {} with PAN: {}", vendorId, panNumber);
        
        try {
            Map<String, Object> response = vendorTaxService.getPanDetails(panNumber).get();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error getting PAN details: {}", panNumber, e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("valid", vendorTaxService.validatePanNumber(panNumber));
            errorResponse.put("error", "Details unavailable: " + e.getMessage());
            errorResponse.put("panNumber", panNumber);
            return ResponseEntity.ok(errorResponse);
        }
    }
    
    /**
     * Get available GST rates
     */
    @GetMapping("/gst-rates")
    public ResponseEntity<Map<String, Object>> getAvailableGstRates() {
        log.info("Getting available GST rates");
        
        Map<String, Object> response = new HashMap<>();
        List<Double> rates = vendorTaxService.getAvailableGstRates();
        
        response.put("gstRates", rates);
        response.put("message", "Available GST rates retrieved successfully");
        
        return ResponseEntity.ok(response);
    }

    /**
     * Bulk import products from Excel file
     */
    @PostMapping(value = "/{vendorId}/products/bulk-import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('VENDOR')")
    public ResponseEntity<?> bulkImportProducts(
            @PathVariable Long vendorId,
            @RequestParam("file") MultipartFile excelFile,
            HttpServletRequest request) {
        try {
            // Verify vendor ID from JWT matches path variable
            Long currentVendorId = jwtTokenUtil.extractUserIdFromRequest(request);
            if (currentVendorId == null || !currentVendorId.equals(vendorId)) {
                return ResponseEntity.badRequest().body("Invalid vendor session or mismatched vendor ID");
            }

            // Validate file
            if (excelFile.isEmpty()) {
                return ResponseEntity.badRequest().body("Please select an Excel file to upload");
            }

            String fileName = excelFile.getOriginalFilename();
            if (fileName == null || (!fileName.endsWith(".xlsx") && !fileName.endsWith(".xls"))) {
                return ResponseEntity.badRequest().body("Please upload a valid Excel file (.xlsx or .xls)");
            }

            log.info("Starting bulk import for vendor: {} with file: {}", vendorId, fileName);
            
            ExcelImportResponseDto response = excelImportService.importProductsFromExcel(excelFile, vendorId);
            
            if (response.getSuccess()) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.badRequest().body(response);
            }
            
        } catch (Exception e) {
            log.error("Error during bulk import for vendor: {}", vendorId, e);
            return ResponseEntity.badRequest().body("Import failed: " + e.getMessage());
        }
    }

    /**
     * Upload images for a specific product
     */
    @PostMapping(value = "/{vendorId}/products/{productId}/upload-images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('VENDOR')")
    public ResponseEntity<?> uploadProductImages(
            @PathVariable Long vendorId,
            @PathVariable Long productId,
            @RequestParam("images") MultipartFile[] images,
            HttpServletRequest request) {
        try {
            // Verify vendor ID from JWT matches path variable
            Long currentVendorId = jwtTokenUtil.extractUserIdFromRequest(request);
            if (currentVendorId == null || !currentVendorId.equals(vendorId)) {
                return ResponseEntity.badRequest().body("Invalid vendor session or mismatched vendor ID");
            }

            if (images == null || images.length == 0) {
                return ResponseEntity.badRequest().body("Please select at least one image to upload");
            }

            // Delete existing images linked to the product
            productImageRepository.deleteByProductId(productId);

            // Perform the upload process
            List<String> imageUrls = fileUploadService.uploadProductImages(images, "products/" + productId);

            // Save image URLs in the database
            for (String imageUrl : imageUrls) {
                ProductImage productImage = ProductImage.builder()
                        .product(productService.getProductById(productId))
                        .imageUrl(imageUrl)
                        .build();
                productImageRepository.save(productImage);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Images uploaded successfully");
            response.put("productId", productId);
            response.put("imageCount", images.length);
            response.put("uploadedImages", imageUrls);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error uploading images for product: {} by vendor: {}", productId, vendorId, e);
            return ResponseEntity.badRequest().body("Image upload failed: " + e.getMessage());
        }
    }

    /**
     * Get Excel import template
     */
    @GetMapping("/vendors/{vendorId}/dashboard/import-template")
    @PreAuthorize("hasRole('VENDOR')")
    public ResponseEntity<?> getImportTemplate(@PathVariable Long vendorId, HttpServletRequest request) {
        try {
            // Verify vendor ID from JWT matches path variable
            Long currentVendorId = jwtTokenUtil.extractUserIdFromRequest(request);
            if (currentVendorId == null || !currentVendorId.equals(vendorId)) {
                return ResponseEntity.badRequest().body("Invalid vendor session or mismatched vendor ID");
            }

            Map<String, Object> template = new HashMap<>();
            template.put("columns", List.of(
                "Category", "Subcategory", "Product", "Description", "Price"
            ));
            template.put("sampleData", List.of(
                Map.of(
                    "Category", "Electronics",
                    "Subcategory", "Mobile Phones",
                    "Product", "iPhone 15",
                    "Description", "Latest smartphone with advanced features",
                    "Price", "75000"
                ),
                Map.of(
                    "Category", "Textiles",
                    "Subcategory", "Cotton Fabric",
                    "Product", "Cotton Cloth",
                    "Description", "Pure cotton fabric",
                    "Price", "500"
                ),
                Map.of(
                    "Category", "Fashion",
                    "Subcategory", "Shoes",
                    "Product", "Running Shoes",
                    "Description", "Comfortable running shoes",
                    "Price", "1500"
                )
            ));
            template.put("instructions", List.of(
                "Column A: Category (Required) - Product category name",
                "Column B: Subcategory (Optional) - Product subcategory",
                "Column C: Product (Required) - Name of the product",
                "Column D: Description (Optional) - Product description",
                "Column E: Price (Required) - Selling price of the product",
                "Save as .xlsx or .xls format",
                "First row should contain headers",
                "Data should start from row 2",
                "Only 5 columns are required for this simplified template"
            ));
            
            return ResponseEntity.ok(template);
            
        } catch (Exception e) {
            log.error("Error getting import template for vendor: {}", vendorId, e);
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/vendors/{vendorId}/dashboard/download-template")
    @PreAuthorize("hasRole('VENDOR')")
    public ResponseEntity<Resource> downloadTemplate(@PathVariable Long vendorId, HttpServletRequest request) {
        try {
            // Verify vendor ID from JWT matches path variable
            Long currentVendorId = jwtTokenUtil.extractUserIdFromRequest(request);
            if (currentVendorId == null || !currentVendorId.equals(vendorId)) {
                return ResponseEntity.badRequest().build();
            }

            // Create CSV content
            StringBuilder csvContent = new StringBuilder();
            csvContent.append("Category,Subcategory,Product,Description,Price\n");
            csvContent.append("Electronics,Mobile Phones,iPhone 15,Latest smartphone with advanced features,75000\n");
            csvContent.append("Textiles,Cotton Fabric,Cotton Cloth,Pure cotton fabric,500\n");
            csvContent.append("Fashion,Shoes,Running Shoes,Comfortable running shoes,1500\n");
            csvContent.append("Food Products,Snacks,Chips,Delicious potato chips,100\n");
            csvContent.append("Healthcare,Supplements,Vitamins,Essential vitamins for health,300\n");

            // Convert to bytes
            byte[] csvBytes = csvContent.toString().getBytes(StandardCharsets.UTF_8);
            
            // Create resource
            ByteArrayResource resource = new ByteArrayResource(csvBytes);
            
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=product_template.csv")
                    .contentType(MediaType.parseMediaType("text/csv"))
                    .contentLength(csvBytes.length)
                    .body(resource);
                    
        } catch (Exception e) {
            log.error("Error downloading template for vendor: {}", vendorId, e);
            return ResponseEntity.badRequest().build();
        }
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
