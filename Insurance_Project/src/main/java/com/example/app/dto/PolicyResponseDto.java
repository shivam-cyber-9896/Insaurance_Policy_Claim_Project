package com.example.app.dto;

import com.example.app.enums.PolicyStatus;
import com.example.app.enums.PremiumType;
import com.example.app.enums.ProductType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PolicyResponseDto {
    private Long id;
    private String policyNumber;
    private Long customerId;
    private String customerName;
    private Long planId;
    private String planName;
    private ProductType productType;
    private Double coverageAmount;
    private Double premiumAmount;
    private PremiumType premiumType;
    private LocalDate startDate;
    private LocalDate endDate;
    private PolicyStatus policyStatus;
    private BigDecimal totalPremiumPaid;
}
