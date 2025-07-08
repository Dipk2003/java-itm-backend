package com.itech.itech_backend.dto;

import lombok.Data;

@Data
public class SetPasswordDto {
    private String emailOrPhone;
    private String newPassword;
}
