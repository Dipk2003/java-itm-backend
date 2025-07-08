package com.itech.itech_backend.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeadDto {
    private String customerName;
    private String email;
    private String phone;
    private String productWanted;
    private Integer quantity;
    private String description;
    private String location;
    private Double budget;
    private String urgency;
}
