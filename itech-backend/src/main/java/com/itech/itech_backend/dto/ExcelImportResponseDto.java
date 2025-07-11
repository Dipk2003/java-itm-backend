package com.itech.itech_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExcelImportResponseDto {
    private Integer totalRows;
    private Integer successfulImports;
    private Integer failedImports;
    private Integer skippedRows;
    private List<String> errors;
    private List<ExcelImportDto> failedRecords;
    private List<Long> createdProductIds;
    private String message;
    private Boolean success;
}
