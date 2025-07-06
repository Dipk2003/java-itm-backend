package com.itech.itech_backend.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuickoGstDetailsDto {
    private String gstin;
    private String legalName;
    private String tradeName;
    private String businessType;
    private String registrationDate;
    private String status;
    private String businessAddress;
    private String principalPlace;
    private List<GstRate> gstRates;
    private String lastUpdated;
    private boolean isActive;
    private String panNumber;
    private String state;
    private String stateCode;
    private String center;
    private String registrationType;
    private String constitution;
    private String grossTurnover;
    private String aggregateTurnover;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GstRate {
        private String category;
        private String description;
        private Double rate;
        private String effectiveDate;
        private String hsn;
        private String taxType;
        private boolean isApplicable;
    }
}
