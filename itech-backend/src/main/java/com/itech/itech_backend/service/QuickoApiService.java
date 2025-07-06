package com.itech.itech_backend.service;

import com.itech.itech_backend.dto.QuickoGstDetailsDto;
import com.itech.itech_backend.dto.QuickoTdsDetailsDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class QuickoApiService {
    
    private final WebClient.Builder webClientBuilder;
    
    @Value("${quicko.api.base-url:https://api.quicko.com}")
    private String quickoApiBaseUrl;
    
    @Value("${quicko.api.key:}")
    private String quickoApiKey;
    
    @Value("${quicko.api.timeout:30}")
    private int timeoutSeconds;
    
    @Value("${quicko.api.enabled:false}")
    private boolean quickoApiEnabled;
    
    @Value("${quicko.api.retry.max-attempts:3}")
    private int maxRetryAttempts;
    
    @Value("${quicko.api.retry.delay:1000}")
    private long retryDelayMs;
    
    /**
     * Fetch GST details from Quicko API
     */
    public Mono<QuickoGstDetailsDto> fetchGstDetails(String gstNumber) {
        log.info("Fetching GST details for: {} (API Enabled: {})", gstNumber, quickoApiEnabled);
        
        // Use mock data if API is disabled or key is not configured
        if (!quickoApiEnabled || quickoApiKey.isEmpty()) {
            log.info("Using mock GST data - API Enabled: {}, Key Configured: {}", 
                    quickoApiEnabled, !quickoApiKey.isEmpty());
            return Mono.just(createMockGstDetails(gstNumber));
        }
        
        WebClient webClient = webClientBuilder
            .baseUrl(quickoApiBaseUrl)
            .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + quickoApiKey)
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .build();
        
        return webClient.get()
            .uri("/gst/details/{gstNumber}", gstNumber)
            .retrieve()
            .onStatus(status -> status.is4xxClientError(), clientResponse -> {
                log.error("Client error while fetching GST details for: {} - Status: {}", 
                         gstNumber, clientResponse.statusCode());
                return Mono.error(new RuntimeException("Invalid GST number or API request: " + clientResponse.statusCode()));
            })
            .onStatus(status -> status.is5xxServerError(), clientResponse -> {
                log.error("Server error while fetching GST details for: {} - Status: {}", 
                         gstNumber, clientResponse.statusCode());
                return Mono.error(new RuntimeException("Quicko API server error: " + clientResponse.statusCode()));
            })
            .bodyToMono(QuickoGstDetailsDto.class)
            .timeout(Duration.ofSeconds(timeoutSeconds))
            .retryWhen(reactor.util.retry.Retry.backoff(maxRetryAttempts, Duration.ofMillis(retryDelayMs))
                .filter(throwable -> !(throwable instanceof WebClientResponseException.BadRequest))
                .doBeforeRetry(retrySignal -> 
                    log.warn("Retrying GST API call for {} - Attempt: {}", gstNumber, retrySignal.totalRetries() + 1)))
            .doOnSuccess(response -> log.info("Successfully fetched GST details for: {}", gstNumber))
            .doOnError(WebClientResponseException.class, ex -> {
                log.error("Error fetching GST details for {}: {} - {}", gstNumber, ex.getStatusCode(), ex.getMessage());
            })
            .onErrorResume(throwable -> {
                log.warn("Falling back to mock data for GST: {} due to error: {}", gstNumber, throwable.getMessage());
                return Mono.just(createMockGstDetails(gstNumber));
            });
    }
    
    /**
     * Fetch TDS details from Quicko API
     */
    public Mono<QuickoTdsDetailsDto> fetchTdsDetails(String panNumber) {
        log.info("Fetching TDS details for: {} (API Enabled: {})", panNumber, quickoApiEnabled);
        
        // Use mock data if API is disabled or key is not configured
        if (!quickoApiEnabled || quickoApiKey.isEmpty()) {
            log.info("Using mock TDS data - API Enabled: {}, Key Configured: {}", 
                    quickoApiEnabled, !quickoApiKey.isEmpty());
            return Mono.just(createMockTdsDetails(panNumber));
        }
        
        WebClient webClient = webClientBuilder
            .baseUrl(quickoApiBaseUrl)
            .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + quickoApiKey)
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .build();
        
        return webClient.get()
            .uri("/tds/details/{panNumber}", panNumber)
            .retrieve()
            .onStatus(status -> status.is4xxClientError(), clientResponse -> {
                log.error("Client error while fetching TDS details for: {} - Status: {}", 
                         panNumber, clientResponse.statusCode());
                return Mono.error(new RuntimeException("Invalid PAN number or API request: " + clientResponse.statusCode()));
            })
            .onStatus(status -> status.is5xxServerError(), clientResponse -> {
                log.error("Server error while fetching TDS details for: {} - Status: {}", 
                         panNumber, clientResponse.statusCode());
                return Mono.error(new RuntimeException("Quicko API server error: " + clientResponse.statusCode()));
            })
            .bodyToMono(QuickoTdsDetailsDto.class)
            .timeout(Duration.ofSeconds(timeoutSeconds))
            .retryWhen(reactor.util.retry.Retry.backoff(maxRetryAttempts, Duration.ofMillis(retryDelayMs))
                .filter(throwable -> !(throwable instanceof WebClientResponseException.BadRequest))
                .doBeforeRetry(retrySignal -> 
                    log.warn("Retrying TDS API call for {} - Attempt: {}", panNumber, retrySignal.totalRetries() + 1)))
            .doOnSuccess(response -> log.info("Successfully fetched TDS details for: {}", panNumber))
            .doOnError(WebClientResponseException.class, ex -> {
                log.error("Error fetching TDS details for {}: {} - {}", panNumber, ex.getStatusCode(), ex.getMessage());
            })
            .onErrorResume(throwable -> {
                log.warn("Falling back to mock data for TDS: {} due to error: {}", panNumber, throwable.getMessage());
                return Mono.just(createMockTdsDetails(panNumber));
            });
    }
    
    /**
     * Create mock GST details for testing/demo purposes
     */
    private QuickoGstDetailsDto createMockGstDetails(String gstNumber) {
        QuickoGstDetailsDto gstDetails = new QuickoGstDetailsDto();
        gstDetails.setGstin(gstNumber);
        gstDetails.setLegalName("Demo Company Pvt Ltd");
        gstDetails.setTradeName("Demo Company");
        gstDetails.setBusinessType("Private Limited Company");
        gstDetails.setRegistrationDate("2020-01-01");
        gstDetails.setStatus("Active");
        gstDetails.setBusinessAddress("123 Business Street, City, State - 123456");
        gstDetails.setPrincipalPlace("Main Office");
        gstDetails.setLastUpdated("2024-01-01");
        gstDetails.setActive(true);
        gstDetails.setPanNumber(gstNumber.substring(2, 12));
        gstDetails.setState("Maharashtra");
        gstDetails.setStateCode("27");
        gstDetails.setCenter("Mumbai");
        gstDetails.setRegistrationType("Regular");
        gstDetails.setConstitution("Private Limited Company");
        gstDetails.setGrossTurnover("50000000");
        gstDetails.setAggregateTurnover("50000000");
        
        // Mock GST rates
        List<QuickoGstDetailsDto.GstRate> gstRates = new ArrayList<>();
        gstRates.add(new QuickoGstDetailsDto.GstRate("Goods", "Standard Rate", 18.0, "2020-01-01", "8544", "SGST+CGST", true));
        gstRates.add(new QuickoGstDetailsDto.GstRate("Goods", "Reduced Rate", 12.0, "2020-01-01", "8544", "SGST+CGST", true));
        gstRates.add(new QuickoGstDetailsDto.GstRate("Services", "Standard Rate", 18.0, "2020-01-01", "998311", "SGST+CGST", true));
        gstRates.add(new QuickoGstDetailsDto.GstRate("Goods", "Low Rate", 5.0, "2020-01-01", "2106", "SGST+CGST", true));
        gstRates.add(new QuickoGstDetailsDto.GstRate("Goods", "High Rate", 28.0, "2020-01-01", "8703", "SGST+CGST", true));
        
        gstDetails.setGstRates(gstRates);
        
        return gstDetails;
    }
    
    /**
     * Create mock TDS details for testing/demo purposes
     */
    private QuickoTdsDetailsDto createMockTdsDetails(String panNumber) {
        QuickoTdsDetailsDto tdsDetails = new QuickoTdsDetailsDto();
        tdsDetails.setPanNumber(panNumber);
        tdsDetails.setAssesseeName("Demo Company Pvt Ltd");
        tdsDetails.setAssesseeType("Company");
        tdsDetails.setFinancialYear("2024-25");
        tdsDetails.setAssessmentYear("2025-26");
        tdsDetails.setLastUpdated("2024-01-01");
        tdsDetails.setActive(true);
        tdsDetails.setStatus("Active");
        tdsDetails.setJurisdictionCode("MUMBAI");
        tdsDetails.setAoCode("AIPB00000F");
        tdsDetails.setAoType("Regular");
        tdsDetails.setRangeCode("123");
        tdsDetails.setWardCode("1");
        
        // Mock TDS rates
        List<QuickoTdsDetailsDto.TdsRate> tdsRates = new ArrayList<>();
        tdsRates.add(new QuickoTdsDetailsDto.TdsRate("194C", "Payment to contractors", 1.0, "Contractor", "2024-01-01", "CON", "Contractor", 30000.0, true, "Individual", "0", "0"));
        tdsRates.add(new QuickoTdsDetailsDto.TdsRate("194I", "Rent", 10.0, "Rent", "2024-01-01", "RNT", "Rent", 240000.0, true, "Individual", "0", "0"));
        tdsRates.add(new QuickoTdsDetailsDto.TdsRate("194J", "Professional fees", 10.0, "Professional", "2024-01-01", "PRO", "Professional fees", 30000.0, true, "Individual", "0", "0"));
        tdsRates.add(new QuickoTdsDetailsDto.TdsRate("194H", "Commission", 5.0, "Commission", "2024-01-01", "COM", "Commission", 15000.0, true, "Individual", "0", "0"));
        tdsRates.add(new QuickoTdsDetailsDto.TdsRate("194A", "Interest", 10.0, "Interest", "2024-01-01", "INT", "Interest", 5000.0, true, "Individual", "0", "0"));
        
        tdsDetails.setTdsRates(tdsRates);
        
        return tdsDetails;
    }
}
