package com.laybhari.service;

import com.laybhari.entity.Order;
import com.laybhari.entity.OrderItem;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Slf4j
@Service
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:}")
    private String fromEmail;

    public EmailService(@Autowired(required = false) JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Async
    public void sendOrderConfirmationEmail(Order order) {
        if (order == null || order.getUser() == null) {
            log.warn("Cannot send order confirmation email: Order or User is null.");
            return;
        }

        String recipientEmail = order.getUser().getEmail();
        if (recipientEmail == null || recipientEmail.isBlank() || recipientEmail.endsWith("@phone.laybhari.com")) {
            log.info("User has no valid email address (Phone account). Skipping order confirmation email for order #{}", order.getId());
            return;
        }

        if (mailSender == null || fromEmail == null || fromEmail.isBlank()) {
            log.warn("⚠️ Email service is not configured (spring.mail.username is empty). Skipping email for order #{}", order.getId());
            return;
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(recipientEmail);
            helper.setSubject("Laybhari Vlogs - Order Confirmation #" + order.getId());

            StringBuilder htmlContent = new StringBuilder();
            htmlContent.append("<!DOCTYPE html><html><head><style>")
                       .append("body { font-family: Arial, sans-serif; color: #333; line-height: 1.6; }")
                       .append(".container { max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #e0e0e0; border-radius: 8px; }")
                       .append(".header { background-color: #e53e3e; color: white; padding: 15px; text-align: center; border-radius: 6px 6px 0 0; }")
                       .append(".content { padding: 20px; }")
                       .append("table { width: 100%; border-collapse: collapse; margin-top: 15px; }")
                       .append("th, td { border: 1px solid #ddd; padding: 10px; text-align: left; }")
                       .append("th { background-color: #f7f7f7; }")
                       .append(".total { text-align: right; margin-top: 15px; font-weight: bold; font-size: 16px; }")
                       .append(".footer { margin-top: 20px; font-size: 12px; color: #777; text-align: center; }")
                       .append("</style></head><body>")
                       .append("<div class='container'>")
                       .append("<div class='header'><h2>Laybhari Vlogs - Order Confirmed!</h2></div>")
                       .append("<div class='content'>")
                       .append("<p>Hi <strong>").append(order.getUser().getName() != null ? order.getUser().getName() : "Customer").append("</strong>,</p>")
                       .append("<p>Thank you for shopping with Laybhari Vlogs! Your order <strong>#").append(order.getId()).append("</strong> has been successfully confirmed.</p>");

            if (order.getAddress() != null) {
                String street = order.getAddress().getLine1() + (order.getAddress().getLine2() != null && !order.getAddress().getLine2().isBlank() ? ", " + order.getAddress().getLine2() : "");
                htmlContent.append("<h3>Delivery Address:</h3><p>")
                           .append(order.getAddress().getFullName() != null ? order.getAddress().getFullName() + "<br/>" : "")
                           .append(street).append("<br/>")
                           .append(order.getAddress().getCity()).append(", ").append(order.getAddress().getState()).append(" - ").append(order.getAddress().getPincode()).append("<br/>")
                           .append("Phone: ").append(order.getAddress().getPhone())
                           .append("</p>");
            }

            htmlContent.append("<h3>Order Summary:</h3>")
                       .append("<table><thead><tr><th>Item</th><th>Weight</th><th>Qty</th><th>Price</th><th>Total</th></tr></thead><tbody>");

            if (order.getItems() != null) {
                for (OrderItem item : order.getItems()) {
                    htmlContent.append("<tr>")
                               .append("<td>").append(item.getProductName()).append("</td>")
                               .append("<td>").append(item.getWeightLabel() != null ? item.getWeightLabel() : "-").append("</td>")
                               .append("<td>").append(item.getQuantity()).append("</td>")
                               .append("<td>₹").append(item.getPrice()).append("</td>")
                               .append("<td>₹").append(item.getLineTotal()).append("</td>")
                               .append("</tr>");
                }
            }

            BigDecimal shipping = order.getShippingFee() != null ? order.getShippingFee() : BigDecimal.ZERO;
            htmlContent.append("</tbody></table>")
                       .append("<div class='total'>")
                       .append("<p>Subtotal: ₹").append(order.getSubtotal()).append("<br/>")
                       .append("Shipping: ").append(shipping.compareTo(BigDecimal.ZERO) == 0 ? "FREE" : "₹" + shipping).append("<br/>")
                       .append("<span style='color:#e53e3e; font-size:18px;'>Grand Total: ₹").append(order.getTotalAmount()).append("</span></p>")
                       .append("</div>")
                       .append("</div>")
                       .append("<div class='footer'><p>If you have any questions, reply to this email or contact support.</p></div>")
                       .append("</div></body></html>");

            helper.setText(htmlContent.toString(), true);

            mailSender.send(message);
            log.info("📧 Order confirmation email sent successfully to [{}] for order #{}", recipientEmail, order.getId());
        } catch (Exception e) {
            log.error("❌ Failed to send order confirmation email for order #{}: {}", order.getId(), e.getMessage(), e);
            // Exception caught so order confirmation is never interrupted
        }
    }
}
