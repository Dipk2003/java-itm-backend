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
public class GstVerificationService {

    @Value("${gst.verification.enabled:false}")
    private boolean gstVerificationEnabled;

    @Value("${gst.api.url:}")
    private String gstApiUrl;

    @Value("${gst.api.key:}")
    private String gstApiKey;

    private final WebClient webClient;

    // GST number pattern: 15 characters
    // Format: 2 digits (state code) + 10 characters (PAN) + 1 digit (entity number) + 1 character (check digit) + 1 character (optional)
    private static final Pattern GST_PATTERN = Pattern.compile("^[0-9]{2}[A-Z]{5}[0-9]{4}[A-Z]{1}[1-9A-Z]{1}Z[0-9A-Z]{1}$");

    public GstVerificationService() {
        this.webClient = WebClient.builder()
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(1024 * 1024))
                .build();
    }

    /**
     * Validate GST number format
     */
    public boolean validateGstFormat(String gstNumber) {
        if (gstNumber == null || gstNumber.trim().isEmpty()) {
            return false;
        }

        String cleanGst = gstNumber.trim().toUpperCase();
        return GST_PATTERN.matcher(cleanGst).matches();
    }

    /**
     * Verify GST number with government API (if enabled and configured)
     */
    public CompletableFuture<Map<String, Object>> verifyGstNumber(String gstNumber) {
        return CompletableFuture.supplyAsync(() -> {
            Map<String, Object> result = new HashMap<>();
            
            // First validate format
            if (!validateGstFormat(gstNumber)) {
                result.put("valid", false);
                result.put("error", "Invalid GST number format");
                result.put("gstNumber", gstNumber);
                return result;
            }

            // If API verification is not enabled, return format validation only
            if (!gstVerificationEnabled || gstApiUrl == null || gstApiUrl.trim().isEmpty()) {
                result.put("valid", true);
                result.put("verified", false);
                result.put("message", "GST format is valid (API verification disabled)");
                result.put("gstNumber", gstNumber);
                return result;
            }

            try {
                // Attempt API verification
                Map<String, Object> apiResponse = callGstVerificationApi(gstNumber);
                result.putAll(apiResponse);
                result.put("verified", true);
                result.put("gstNumber", gstNumber);
                
            } catch (Exception e) {
                log.warn("GST API verification failed for {}: {}", gstNumber, e.getMessage());
                // Fallback to format validation
                result.put("valid", true);
                result.put("verified", false);
                result.put("message", "GST format is valid (API verification failed)");
                result.put("gstNumber", gstNumber);
                result.put("apiError", e.getMessage());
            }

            return result;
        });
    }

    /**
     * Get GST details (business name, address, etc.) if API is configured
     */
    public CompletableFuture<Map<String, Object>> getGstDetails(String gstNumber) {
        return CompletableFuture.supplyAsync(() -> {
            Map<String, Object> result = new HashMap<>();

            if (!validateGstFormat(gstNumber)) {
                result.put("valid", false);
                result.put("error", "Invalid GST number format");
                return result;
            }

            if (!gstVerificationEnabled || gstApiUrl == null || gstApiUrl.trim().isEmpty()) {
                result.put("valid", true);
                result.put("gstNumber", gstNumber);
                result.put("message", "GST details API not configured");
                return result;
            }

            try {
                Map<String, Object> apiResponse = callGstDetailsApi(gstNumber);
                result.putAll(apiResponse);
                result.put("gstNumber", gstNumber);
                
            } catch (Exception e) {
                log.warn("GST details API failed for {}: {}", gstNumber, e.getMessage());
                result.put("valid", true);
                result.put("gstNumber", gstNumber);
                result.put("message", "GST number is valid but details unavailable");
                result.put("apiError", e.getMessage());
            }

            return result;
        });
    }

    /**
     * Extract state code from GST number
     */
    public String extractStateCode(String gstNumber) {
        if (!validateGstFormat(gstNumber)) {
            return null;
        }
        return gstNumber.substring(0, 2);
    }

    /**
     * Extract PAN from GST number
     */
    public String extractPanFromGst(String gstNumber) {
        if (!validateGstFormat(gstNumber)) {
            return null;
        }
        return gstNumber.substring(2, 12);
    }

    /**
     * Get state name from state code
     */
    public String getStateName(String stateCode) {
        Map<String, String> stateCodes = getStateCodes();
        return stateCodes.get(stateCode);
    }

    /**
     * Call external GST verification API
     */
    private Map<String, Object> callGstVerificationApi(String gstNumber) {
        try {
            String url = gstApiUrl + "/verify";
            
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("gstNumber", gstNumber);
            
            Map<String, Object> response = webClient.post()
                    .uri(url)
                    .header("Authorization", "Bearer " + gstApiKey)
                    .header("Content-Type", "application/json")
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                    .block();

            if (response != null) {
                return response;
            } else {
                throw new RuntimeException("Empty response from GST API");
            }
            
        } catch (WebClientResponseException e) {
            log.error("GST API error: Status {}, Body: {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("GST verification API error: " + e.getMessage());
        }
    }

    /**
     * Call external GST details API
     */
    private Map<String, Object> callGstDetailsApi(String gstNumber) {
        try {
            String url = gstApiUrl + "/details";
            
            Map<String, Object> response = webClient.get()
                    .uri(url + "?gstNumber=" + gstNumber)
                    .header("Authorization", "Bearer " + gstApiKey)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                    .block();

            if (response != null) {
                return response;
            } else {
                throw new RuntimeException("Empty response from GST details API");
            }
            
        } catch (WebClientResponseException e) {
            log.error("GST details API error: Status {}, Body: {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("GST details API error: " + e.getMessage());
        }
    }

    /**
     * Get mapping of state codes to state names
     */
    private Map<String, String> getStateCodes() {
        Map<String, String> stateCodes = new HashMap<>();
        stateCodes.put("01", "Jammu and Kashmir");
        stateCodes.put("02", "Himachal Pradesh");
        stateCodes.put("03", "Punjab");
        stateCodes.put("04", "Chandigarh");
        stateCodes.put("05", "Uttarakhand");
        stateCodes.put("06", "Haryana");
        stateCodes.put("07", "Delhi");
        stateCodes.put("08", "Rajasthan");
        stateCodes.put("09", "Uttar Pradesh");
        stateCodes.put("10", "Bihar");
        stateCodes.put("11", "Sikkim");
        stateCodes.put("12", "Arunachal Pradesh");
        stateCodes.put("13", "Nagaland");
        stateCodes.put("14", "Manipur");
        stateCodes.put("15", "Mizoram");
        stateCodes.put("16", "Tripura");
        stateCodes.put("17", "Meghalaya");
        stateCodes.put("18", "Assam");
        stateCodes.put("19", "West Bengal");
        stateCodes.put("20", "Jharkhand");
        stateCodes.put("21", "Odisha");
        stateCodes.put("22", "Chhattisgarh");
        stateCodes.put("23", "Madhya Pradesh");
        stateCodes.put("24", "Gujarat");
        stateCodes.put("25", "Daman and Diu");
        stateCodes.put("26", "Dadra and Nagar Haveli");
        stateCodes.put("27", "Maharashtra");
        stateCodes.put("28", "Andhra Pradesh");
        stateCodes.put("29", "Karnataka");
        stateCodes.put("30", "Goa");
        stateCodes.put("31", "Lakshadweep");
        stateCodes.put("32", "Kerala");
        stateCodes.put("33", "Tamil Nadu");
        stateCodes.put("34", "Puducherry");
        stateCodes.put("35", "Andaman and Nicobar Islands");
        stateCodes.put("36", "Telangana");
        stateCodes.put("37", "Andhra Pradesh (New)");
        return stateCodes;
    }
}
