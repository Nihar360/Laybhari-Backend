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

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

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

            String cleanedPhone = phone.replaceAll("[^0-9]", "");
            if (cleanedPhone.length() > 10) {
                cleanedPhone = cleanedPhone.substring(cleanedPhone.length() - 10);
            }

            HttpHeaders headers = new HttpHeaders();
            headers.set("authorization", apiKey.trim());
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            // Try Quick SMS Route (route=q) first as it works without website verification
            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add("route", "q");
            body.add("message", "Your OTP verification code for Laybhari Vlogs is " + otpCode + ". Valid for 5 minutes.");
            body.add("flash", "0");
            body.add("numbers", cleanedPhone);

            HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(body, headers);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);

            String resBody = response.getBody() != null ? response.getBody() : "";
            log.info("📲 Fast2SMS OTP response for [{}]: status={} body={}", cleanedPhone, response.getStatusCode(), resBody);

            boolean isSuccess = response.getStatusCode().is2xxSuccessful() && (resBody.contains("\"return\":true") || resBody.contains("\"return\": true"));
            if (!isSuccess) {
                // Fallback to OTP route if route=q returns error
                MultiValueMap<String, String> otpBody = new LinkedMultiValueMap<>();
                otpBody.add("route", "otp");
                otpBody.add("variables_values", otpCode);
                otpBody.add("numbers", cleanedPhone);

                HttpEntity<MultiValueMap<String, String>> otpEntity = new HttpEntity<>(otpBody, headers);
                ResponseEntity<String> otpResponse = restTemplate.exchange(url, HttpMethod.POST, otpEntity, String.class);
                String otpResBody = otpResponse.getBody() != null ? otpResponse.getBody() : "";
                log.info("📲 Fast2SMS OTP fallback response for [{}]: status={} body={}", cleanedPhone, otpResponse.getStatusCode(), otpResBody);
                isSuccess = otpResponse.getStatusCode().is2xxSuccessful() && (otpResBody.contains("\"return\":true") || otpResBody.contains("\"return\": true"));
            }

            if (!isSuccess) {
                log.warn("⚠️ Fast2SMS request failed. Body: {}", resBody);
            }
            return isSuccess;
        } catch (Exception e) {
            log.error("❌ Fast2SMS error sending SMS to [{}]: {}", phone, e.getMessage());
            return false;
        }
    }
}
