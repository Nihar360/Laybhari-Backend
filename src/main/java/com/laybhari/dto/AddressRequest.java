package com.laybhari.dto;

import lombok.Data;

@Data
public class AddressRequest {
    private String fullName;
    private String phone;
    private String line1;
    private String line2;
    private String city;
    private String state;
    private String pincode;
    private Boolean isDefault;
}
