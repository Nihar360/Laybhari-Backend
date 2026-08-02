package com.laybhari.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreatePaymentResponse {
    private String razorpayOrderId;
    private Long amount;
    private String currency;
    private String keyId;
}
