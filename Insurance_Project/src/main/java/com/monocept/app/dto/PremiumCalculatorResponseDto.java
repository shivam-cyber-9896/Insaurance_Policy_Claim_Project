package com.monocept.app.dto;

import java.math.BigDecimal;
import com.monocept.app.enums.PremiumType;
import com.monocept.app.enums.ProductType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PremiumCalculatorResponseDto {

    private BigDecimal coverageAmount;
    private Integer durationYears;
    private PremiumType premiumType;
    private ProductType productType;
    private Integer age;

    // Formula Breakdown Components: Premium = (Risk Premium) + (Loading Charges) - (Discounts)
    private BigDecimal riskPremium;
    private BigDecimal loadingCharges;
    private BigDecimal discounts;

    private BigDecimal calculatedPremium; // Final Net Premium
    private BigDecimal totalPremiumPaidOverTerm;
    private BigDecimal baseRatePercentage;
    private BigDecimal ageRiskFactor;
    private Boolean isSmoker;
    private BigDecimal smokerRiskFactor;
    private String breakdownSummary;
}

