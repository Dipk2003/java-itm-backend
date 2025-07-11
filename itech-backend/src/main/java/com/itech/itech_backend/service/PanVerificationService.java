package com.itech.itech_backend.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;
import org.springframework.core.ParameterizedTypeReference;

@Service
@Slf4j
public class PanVerificationService {

    @Value("${pan.verification.enabled:false}")
    private boolean panVerificationEnabled;

    @Value("${pan.api.url:}")
    private String panApiUrl;

    @Value("${pan.api.key:}")
    private String panApiKey;

    private final WebClient webClient;

    // PAN number pattern: 10 characters
    // Format: 5 letters + 4 digits + 1 letter
    // Example: ABCDE1234F
    private static final Pattern PAN_PATTERN = Pattern.compile("^[A-Z]{5}[0-9]{4}[A-Z]{1}$");

    public PanVerificationService() {
        this.webClient = WebClient.builder()
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(1024 * 1024))
                .build();
    }

    /**
     * Validate PAN number format
     */
    public boolean validatePanFormat(String panNumber) {
        if (panNumber == null || panNumber.trim().isEmpty()) {
            return false;
        }

        String cleanPan = panNumber.trim().toUpperCase();
        return PAN_PATTERN.matcher(cleanPan).matches();
    }

    /**
     * Verify PAN number with government API (if enabled and configured)
     */
    public CompletableFuture<Map<String, Object>> verifyPanNumber(String panNumber) {
        return CompletableFuture.supplyAsync(() -> {
            Map<String, Object> result = new HashMap<>();
            
            // First validate format
            if (!validatePanFormat(panNumber)) {
                result.put("valid", false);
                result.put("error", "Invalid PAN number format");
                result.put("panNumber", panNumber);
                return result;
            }

            // If API verification is not enabled, return format validation only
            if (!panVerificationEnabled || panApiUrl == null || panApiUrl.trim().isEmpty()) {
                result.put("valid", true);
                result.put("verified", false);
                result.put("message", "PAN format is valid (API verification disabled)");
                result.put("panNumber", panNumber);
                result.put("category", getPanCategory(panNumber));
                return result;
            }

            try {
                // Attempt API verification
                Map<String, Object> apiResponse = callPanVerificationApi(panNumber);
                result.putAll(apiResponse);
                result.put("verified", true);
                result.put("panNumber", panNumber);
                result.put("category", getPanCategory(panNumber));
                
            } catch (Exception e) {
                log.warn("PAN API verification failed for {}: {}", panNumber, e.getMessage());
                // Fallback to format validation
                result.put("valid", true);
                result.put("verified", false);
                result.put("message", "PAN format is valid (API verification failed)");
                result.put("panNumber", panNumber);
                result.put("category", getPanCategory(panNumber));
                result.put("apiError", e.getMessage());
            }

            return result;
        });
    }

    /**
     * Get PAN details (name, status, etc.) if API is configured
     */
    public CompletableFuture<Map<String, Object>> getPanDetails(String panNumber) {
        return CompletableFuture.supplyAsync(() -> {
            Map<String, Object> result = new HashMap<>();

            if (!validatePanFormat(panNumber)) {
                result.put("valid", false);
                result.put("error", "Invalid PAN number format");
                return result;
            }

            if (!panVerificationEnabled || panApiUrl == null || panApiUrl.trim().isEmpty()) {
                result.put("valid", true);
                result.put("panNumber", panNumber);
                result.put("category", getPanCategory(panNumber));
                result.put("message", "PAN details API not configured");
                return result;
            }

            try {
                Map<String, Object> apiResponse = callPanDetailsApi(panNumber);
                result.putAll(apiResponse);
                result.put("panNumber", panNumber);
                result.put("category", getPanCategory(panNumber));
                
            } catch (Exception e) {
                log.warn("PAN details API failed for {}: {}", panNumber, e.getMessage());
                result.put("valid", true);
                result.put("panNumber", panNumber);
                result.put("category", getPanCategory(panNumber));
                result.put("message", "PAN number is valid but details unavailable");
                result.put("apiError", e.getMessage());
            }

            return result;
        });
    }

    /**
     * Get PAN category based on the 4th character
     */
    public String getPanCategory(String panNumber) {
        if (!validatePanFormat(panNumber)) {
            return "Unknown";
        }

        char categoryCode = panNumber.charAt(3);
        Map<Character, String> categories = getPanCategories();
        return categories.getOrDefault(categoryCode, "Unknown");
    }

    /**
     * Check if PAN belongs to an individual
     */
    public boolean isIndividualPan(String panNumber) {
        if (!validatePanFormat(panNumber)) {
            return false;
        }
        char categoryCode = panNumber.charAt(3);
        return categoryCode == 'P' || categoryCode == 'A' || categoryCode == 'B' || 
               categoryCode == 'G' || categoryCode == 'J' || categoryCode == 'L' || 
               categoryCode == 'N' || categoryCode == 'M';
    }

    /**
     * Check if PAN belongs to a company/business
     */
    public boolean isBusinessPan(String panNumber) {
        if (!validatePanFormat(panNumber)) {
            return false;
        }
        char categoryCode = panNumber.charAt(3);
        return categoryCode == 'C' || categoryCode == 'F' || categoryCode == 'H' || 
               categoryCode == 'T' || categoryCode == 'E' || categoryCode == 'K';
    }

    /**
     * Extract name initials from PAN (first 5 characters)
     */
    public String extractNameInitials(String panNumber) {
        if (!validatePanFormat(panNumber)) {
            return null;
        }
        return panNumber.substring(0, 5);
    }

    /**
     * Call external PAN verification API
     */
    private Map<String, Object> callPanVerificationApi(String panNumber) {
        try {
            String url = panApiUrl + "/verify";
            
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("panNumber", panNumber);
            
            Map<String, Object> response = webClient.post()
                    .uri(url)
                    .header("Authorization", "Bearer " + panApiKey)
                    .header("Content-Type", "application/json")
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                    .block();

            if (response != null) {
                return response;
            } else {
                throw new RuntimeException("Empty response from PAN API");
            }
            
        } catch (WebClientResponseException e) {
            log.error("PAN API error: Status {}, Body: {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("PAN verification API error: " + e.getMessage());
        }
    }

    /**
     * Call external PAN details API
     */
    private Map<String, Object> callPanDetailsApi(String panNumber) {
        try {
            String url = panApiUrl + "/details";
            
            Map<String, Object> response = webClient.get()
                    .uri(url + "?panNumber=" + panNumber)
                    .header("Authorization", "Bearer " + panApiKey)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                    .block();

            if (response != null) {
                return response;
            } else {
                throw new RuntimeException("Empty response from PAN details API");
            }
            
        } catch (WebClientResponseException e) {
            log.error("PAN details API error: Status {}, Body: {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("PAN details API error: " + e.getMessage());
        }
    }

    /**
     * Get mapping of PAN category codes to descriptions
     */
    private Map<Character, String> getPanCategories() {
        Map<Character, String> categories = new HashMap<>();
        categories.put('A', "Association of Persons (AOP)");
        categories.put('B', "Body of Individuals (BOI)");
        categories.put('C', "Company");
        categories.put('F', "Firm");
        categories.put('G', "Government");
        categories.put('H', "Hindu Undivided Family (HUF)");
        categories.put('L', "Local Authority");
        categories.put('J', "Artificial Juridical Person");
        categories.put('P', "Individual");
        categories.put('T', "Trust");
        categories.put('E', "Estate");
        categories.put('K', "Krish (Agricultural Income)");
        categories.put('N', "Non-resident Indian");
        categories.put('M', "Minor");
        return categories;
    }
}
