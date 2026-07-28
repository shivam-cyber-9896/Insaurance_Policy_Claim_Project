package com.monocept.app.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.monocept.app.enums.PolicyStatus;
import com.monocept.app.enums.PremiumType;
import com.monocept.app.enums.ProductType;

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
    private BigDecimal coverageAmount;
    private BigDecimal premiumAmount;
    private PremiumType premiumType;
    private LocalDate startDate;
    private LocalDate endDate;
    private PolicyStatus policyStatus;
    private BigDecimal totalPremiumPaid;
    private BigDecimal remainingCoverage;
    private Long agentId;
    private String agentName;

    // New fields
    private BigDecimal selectedCoverageAmount;

    // Policyholder detail fields
    private String holderName;
    private String holderAddress;
    private String holderPhone;
    private String holderAadhaar;   // Masked: "XXXX-XXXX-1234"
    private String vehicleNumber;
}
