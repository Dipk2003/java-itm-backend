package com.itech.itech_backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class VerificationConfig {

    @Value("${gst.verification.enabled:false}")
    private boolean gstVerificationEnabled;

    @Value("${pan.verification.enabled:false}")
    private boolean panVerificationEnabled;

    @Bean
    public WebClient verificationWebClient() {
        return WebClient.builder()
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(2 * 1024 * 1024)) // 2MB
                .build();
    }

    /**
     * Check if any verification service is enabled
     */
    public boolean isAnyVerificationEnabled() {
        return gstVerificationEnabled || panVerificationEnabled;
    }

    /**
     * Check if GST verification is enabled
     */
    public boolean isGstVerificationEnabled() {
        return gstVerificationEnabled;
    }

    /**
     * Check if PAN verification is enabled
     */
    public boolean isPanVerificationEnabled() {
        return panVerificationEnabled;
    }
}
