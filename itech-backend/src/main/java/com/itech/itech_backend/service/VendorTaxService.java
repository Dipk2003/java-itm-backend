package com.itech.itech_backend.service;

import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class VendorTaxService {

    public Map<String, Object> fetchPanTaxData(String pan, String dob) {
        String url = "https://quicko.com/api/v1/pan/verify";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("x-api-key", "YOUR_API_KEY");
        headers.set("x-api-secret", "YOUR_API_SECRET");

        Map<String, String> body = new HashMap<>();
        body.put("pan", pan);
        body.put("dob", dob);

        HttpEntity<Map<String, String>> request = new HttpEntity<>(body, headers);
        RestTemplate restTemplate = new RestTemplate();

        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, request, Map.class);
        return response.getBody();
    }
}
