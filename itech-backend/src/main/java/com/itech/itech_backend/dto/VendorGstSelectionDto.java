package com.itech.itech_backend.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VendorGstSelectionDto {
    private Long vendorId;
    private String gstNumber;
    private List<SelectedGstRate> selectedGstRates;
    private List<SelectedTdsRate> selectedTdsRates;
    private String notes;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SelectedGstRate {
        private String category;
        private String description;
        private Double rate;
        private String hsn;
        private String taxType;
        private boolean isSelected;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SelectedTdsRate {
        private String section;
        private String description;
        private Double rate;
        private String paymentType;
        private String categoryCode;
        private String natureOfPayment;
        private boolean isSelected;
    }
}
