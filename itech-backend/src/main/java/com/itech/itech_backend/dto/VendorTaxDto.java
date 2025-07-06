package com.itech.itech_backend.dto;

import lombok.Data;

@Data
public class VendorTaxDto {
    private Long vendorId;
    private String pan;
    private String gst;
    private String legalName;
}
