package com.monocept.app.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RazorpayOrderResponseDto {
    private String razorpayOrderId;
    private Long amountInPaise;
    private BigDecimal amountInRupees;
    private String currency;
    private String keyId;
    private Long policyId;
    private String policyNumber;
}
