package com.laybhari.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class Fast2SmsService {

    private static final Logger log = LoggerFactory.getLogger(Fast2SmsService.class);

    @Value("${fast2sms.api.key:mock_key}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    public boolean sendOtpSms(String phone, String otpCode) {
        if (apiKey == null || apiKey.isBlank() || "mock_key".equalsIgnoreCase(apiKey) || "YOUR_FAST2SMS_API_KEY".equalsIgnoreCase(apiKey)) {
            log.info("ℹ️ Fast2SMS API key not set in environment (FAST2SMS_API_KEY). SMS skipped. (Console Test OTP for {}: {})", phone, otpCode);
            return false;
        }

        try {
            String url = "https://www.fast2sms.com/dev/bulkV2";

            HttpHeaders headers = new HttpHeaders();
            headers.set("authorization", apiKey.trim());
            headers.setContentType(MediaType.APPLICATION_JSON);

            String cleanedPhone = phone.replaceAll("[^0-9]", "");
            if (cleanedPhone.length() > 10) {
                cleanedPhone = cleanedPhone.substring(cleanedPhone.length() - 10);
            }

            Map<String, Object> body = new HashMap<>();
            body.put("route", "otp");
            body.put("variables_values", otpCode);
            body.put("numbers", cleanedPhone);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);

            log.info("📲 Fast2SMS OTP response for [{}]: status={} body={}", cleanedPhone, response.getStatusCode(), response.getBody());
            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            log.error("❌ Fast2SMS error sending SMS to [{}]: {}", phone, e.getMessage());
            return false;
        }
    }
}
