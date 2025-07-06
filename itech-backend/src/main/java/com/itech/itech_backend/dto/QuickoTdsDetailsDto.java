package com.itech.itech_backend.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuickoTdsDetailsDto {
    private String panNumber;
    private String assesseeName;
    private String assesseeType;
    private String financialYear;
    private String assessmentYear;
    private List<TdsRate> tdsRates;
    private String lastUpdated;
    private boolean isActive;
    private String status;
    private String jurisdictionCode;
    private String aoCode;
    private String aoType;
    private String rangeCode;
    private String wardCode;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TdsRate {
        private String section;
        private String description;
        private Double rate;
        private String paymentType;
        private String effectiveDate;
        private String categoryCode;
        private String natureOfPayment;
        private Double thresholdLimit;
        private boolean isApplicable;
        private String deducteeType;
        private String surchargeRate;
        private String cessRate;
    }
}
