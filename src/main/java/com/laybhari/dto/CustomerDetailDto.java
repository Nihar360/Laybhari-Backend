package com.laybhari.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerDetailDto {
    private CustomerDto customer;
    @Builder.Default
    private List<OrderDto> orderHistory = new ArrayList<>();
}
