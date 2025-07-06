package com.itech.itech_backend.dto;

import lombok.Data;

@Data
public class VerifyOtpRequestDto {
    private String emailOrPhone;
    private String otp;
}
