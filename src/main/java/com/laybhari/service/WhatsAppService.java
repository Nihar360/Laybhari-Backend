package com.laybhari.service;

import com.laybhari.entity.Order;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

@Slf4j
@Service
public class WhatsAppService {

    @Value("${whatsapp.phone-number-id:}")
    private String phoneNumberId;

    @Value("${whatsapp.access-token:}")
    private String accessToken;

    @Value("${whatsapp.template-name:hello_world}")
    private String templateName;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    @Async
    public void sendOrderConfirmationWhatsApp(Order order) {
        if (order == null || order.getUser() == null) {
            log.warn("Cannot send WhatsApp message: Order or User is null.");
            return;
        }

        String rawPhone = order.getUser().getPhone();
        if (rawPhone == null || rawPhone.isBlank()) {
            if (order.getAddress() != null && order.getAddress().getPhone() != null) {
                rawPhone = order.getAddress().getPhone();
            }
        }

        if (rawPhone == null || rawPhone.isBlank()) {
            log.info("User has no phone number. Skipping WhatsApp notification for order #{}", order.getId());
            return;
        }

        if (phoneNumberId == null || phoneNumberId.isBlank() || accessToken == null || accessToken.isBlank()) {
            log.warn("⚠️ WhatsApp Cloud API is not configured (phone-number-id or access-token is empty). Skipping WhatsApp for order #{}", order.getId());
            return;
        }

        String recipientPhone = formatPhoneForWhatsApp(rawPhone);

        try {
            String url = "https://graph.facebook.com/v20.0/" + phoneNumberId + "/messages";

            String jsonPayload;
            String customerName = order.getUser().getName() != null ? order.getUser().getName() : "Customer";
            String orderIdStr = String.valueOf(order.getId());
            String totalAmountStr = order.getTotalAmount() != null ? order.getTotalAmount().toString() : "0";
            String estDelivery = "3-5 Business Days";

            if ("hello_world".equalsIgnoreCase(templateName.trim())) {
                jsonPayload = String.format("""
                    {
                      "messaging_product": "whatsapp",
                      "to": "%s",
                      "type": "template",
                      "template": {
                        "name": "hello_world",
                        "language": { "code": "en_US" }
                      }
                    }
                    """, recipientPhone);
            } else {
                jsonPayload = String.format("""
                    {
                      "messaging_product": "whatsapp",
                      "to": "%s",
                      "type": "template",
                      "template": {
                        "name": "%s",
                        "language": { "code": "en_US" },
                        "components": [
                          {
                            "type": "body",
                            "parameters": [
                              { "type": "text", "text": "%s" },
                              { "type": "text", "text": "%s" },
                              { "type": "text", "text": "%s" },
                              { "type": "text", "text": "%s" }
                            ]
                          }
                        ]
                      }
                    }
                    """, recipientPhone, templateName.trim(), sanitizeJson(customerName), orderIdStr, totalAmountStr, estDelivery);
            }

            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Authorization", "Bearer " + accessToken)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                    .timeout(Duration.ofSeconds(15))
                    .build();

            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                log.info("📱 WhatsApp order confirmation message sent successfully to [{}] for order #{}", recipientPhone, order.getId());
            } else {
                log.error("❌ WhatsApp Cloud API returned HTTP status {}: {}", response.statusCode(), response.body());
            }
        } catch (Exception e) {
            log.error("❌ Failed to send WhatsApp notification for order #{}: {}", order.getId(), e.getMessage(), e);
        }
    }

    private String formatPhoneForWhatsApp(String phone) {
        String cleaned = phone.replaceAll("[^0-9]", "");
        if (cleaned.length() == 10) {
            return "91" + cleaned;
        }
        return cleaned;
    }

    private String sanitizeJson(String input) {
        if (input == null) return "";
        return input.replace("\"", "\\\"").replace("\n", " ");
    }
}
