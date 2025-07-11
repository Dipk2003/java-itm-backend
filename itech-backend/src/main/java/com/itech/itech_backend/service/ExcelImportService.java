package com.itech.itech_backend.service;

import com.itech.itech_backend.dto.ExcelImportDto;
import com.itech.itech_backend.dto.ExcelImportResponseDto;
import com.itech.itech_backend.dto.ProductDto;
import com.itech.itech_backend.model.Category;
import com.itech.itech_backend.model.Product;
import com.itech.itech_backend.model.User;
import com.itech.itech_backend.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExcelImportService {

    private final ProductService productService;
    private final CategoryRepository categoryRepository;
    private final UserService userService;

    @Transactional
    public ExcelImportResponseDto importProductsFromExcel(MultipartFile file, Long vendorId) {
        try {
            // Validate vendor exists
            User vendor = userService.getUserById(vendorId)
                    .orElseThrow(() -> new RuntimeException("Vendor not found"));

            List<ExcelImportDto> excelData = parseExcelFile(file);
            return processImportData(excelData, vendor);

        } catch (Exception e) {
            log.error("Error importing Excel file", e);
            return ExcelImportResponseDto.builder()
                    .success(false)
                    .message("Failed to import Excel file: " + e.getMessage())
                    .totalRows(0)
                    .successfulImports(0)
                    .failedImports(0)
                    .errors(List.of(e.getMessage()))
                    .build();
        }
    }

    private List<ExcelImportDto> parseExcelFile(MultipartFile file) throws IOException {
        List<ExcelImportDto> excelData = new ArrayList<>();

        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            
            // Skip header row (row 0)
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                ExcelImportDto dto = parseRow(row, i + 1);
                excelData.add(dto);
            }
        }

        return excelData;
    }

    private ExcelImportDto parseRow(Row row, int rowNumber) {
        ExcelImportDto dto = ExcelImportDto.builder()
                .rowNumber(rowNumber)
                .isValid(true)
                .build();

        try {
            // Expected columns (Your 5-Column Format):
            // A: Category (Required)
            // B: Subcategory (Optional) 
            // C: Product Name (Required)
            // D: Description (Optional)
            // E: Price (Required)

            dto.setCategory(getCellValueAsString(row.getCell(0)));
            dto.setSubcategory(getCellValueAsString(row.getCell(1)));
            dto.setProductName(getCellValueAsString(row.getCell(2)));
            dto.setDescription(getCellValueAsString(row.getCell(3)));
            
            // Price (required) - Column E (index 4)
            String priceStr = getCellValueAsString(row.getCell(4));
            if (priceStr != null && !priceStr.trim().isEmpty()) {
                dto.setPrice(new BigDecimal(priceStr.trim()));
            }
            
            // Set default values for optional fields
            if (dto.getDescription() == null || dto.getDescription().trim().isEmpty()) {
                dto.setDescription(dto.getProductName()); // Use product name as description if empty
            }
            dto.setMinOrderQuantity(1); // Default minimum order quantity
            dto.setUnit("piece"); // Default unit
            dto.setGstRate(18.0); // Default GST rate for India
            dto.setFreeShipping(false); // Default no free shipping


            // Validate required fields
            validateRequiredFields(dto);

        } catch (Exception e) {
            dto.setIsValid(false);
            dto.setErrorMessage("Error parsing row " + rowNumber + ": " + e.getMessage());
            log.error("Error parsing row {}: {}", rowNumber, e.getMessage());
        }

        return dto;
    }

    private String getCellValueAsString(Cell cell) {
        if (cell == null) return null;
        
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                } else {
                    return String.valueOf((long) cell.getNumericCellValue());
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            default:
                return "";
        }
    }

    private void validateRequiredFields(ExcelImportDto dto) {
        List<String> errors = new ArrayList<>();

        if (dto.getCategory() == null || dto.getCategory().trim().isEmpty()) {
            errors.add("Category is required");
        }
        if (dto.getProductName() == null || dto.getProductName().trim().isEmpty()) {
            errors.add("Product name is required");
        }
        if (dto.getPrice() == null || dto.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            errors.add("Valid price is required");
        }

        if (!errors.isEmpty()) {
            dto.setIsValid(false);
            dto.setErrorMessage(String.join(", ", errors));
        }
    }

    private ExcelImportResponseDto processImportData(List<ExcelImportDto> excelData, User vendor) {
        List<String> errors = new ArrayList<>();
        List<ExcelImportDto> failedRecords = new ArrayList<>();
        List<Long> createdProductIds = new ArrayList<>();
        
        int successfulImports = 0;
        int failedImports = 0;
        int skippedRows = 0;

        for (ExcelImportDto dto : excelData) {
            if (!dto.getIsValid()) {
                failedRecords.add(dto);
                errors.add("Row " + dto.getRowNumber() + ": " + dto.getErrorMessage());
                failedImports++;
                continue;
            }

            try {
                // Find or create category
                Category category = findOrCreateCategory(dto.getCategory(), dto.getSubcategory());

                // Create ProductDto
                ProductDto productDto = convertToProductDto(dto, category.getId());

                // Create product
                productDto.setVendorId(vendor.getId());
                Product product = productService.addProduct(productDto);
                createdProductIds.add(product.getId());
                successfulImports++;

                log.info("Successfully imported product: {} (Row {})", dto.getProductName(), dto.getRowNumber());

            } catch (Exception e) {
                dto.setErrorMessage("Failed to create product: " + e.getMessage());
                failedRecords.add(dto);
                errors.add("Row " + dto.getRowNumber() + ": " + e.getMessage());
                failedImports++;
                log.error("Failed to import product at row {}: {}", dto.getRowNumber(), e.getMessage());
            }
        }

        return ExcelImportResponseDto.builder()
                .success(failedImports == 0)
                .totalRows(excelData.size())
                .successfulImports(successfulImports)
                .failedImports(failedImports)
                .skippedRows(skippedRows)
                .errors(errors)
                .failedRecords(failedRecords)
                .createdProductIds(createdProductIds)
                .message(String.format("Import completed: %d successful, %d failed out of %d total rows", 
                        successfulImports, failedImports, excelData.size()))
                .build();
    }

    private Category findOrCreateCategory(String categoryName, String subcategoryName) {
        // Try to find existing category
        Optional<Category> existingCategory = categoryRepository.findByName(categoryName);
        
        if (existingCategory.isPresent()) {
            return existingCategory.get();
        }

        // Create new category
        Category category = new Category();
        category.setName(categoryName);
        // You can add subcategory logic here if needed
        
        return categoryRepository.save(category);
    }

    private ProductDto convertToProductDto(ExcelImportDto dto, Long categoryId) {
        return ProductDto.builder()
                .name(dto.getProductName())
                .description(dto.getDescription())
                .price(dto.getPrice() != null ? dto.getPrice().doubleValue() : null)
                .originalPrice(dto.getOriginalPrice() != null ? dto.getOriginalPrice().doubleValue() : null)
                .categoryId(categoryId)
                .brand(dto.getBrand())
                .model(dto.getModel())
                .sku(dto.getSku())
                .minOrderQuantity(dto.getMinOrderQuantity())
                .unit(dto.getUnit())
                .specifications(dto.getSpecifications())
                .tags(dto.getTags())
                .gstRate(dto.getGstRate())
                .weight(dto.getWeight())
                .length(dto.getLength())
                .width(dto.getWidth())
                .height(dto.getHeight())
                .freeShipping(dto.getFreeShipping())
                .shippingCharge(dto.getShippingCharge())
                .isActive(true)
                .build();
    }
}
