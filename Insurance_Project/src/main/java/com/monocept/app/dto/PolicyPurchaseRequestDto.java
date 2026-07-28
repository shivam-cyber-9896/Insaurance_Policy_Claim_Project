package com.monocept.app.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PolicyPurchaseRequestDto {

    @NotNull(message = "Plan id is required")
    private Long planId;

    @NotNull(message = "Start date is required")
    @FutureOrPresent(message = "Start date must be today or future date")
    private LocalDate startDate;

    /** Agent assigned to this policy. Must match the policy's product type (or be SUPER specialization). */
    private Long agentId;

    private Boolean isSmoker;

    private com.monocept.app.enums.PremiumType premiumType;

    @NotNull(message = "Selected coverage amount is required")
    @DecimalMin(value = "50000.00", message = "Coverage must be at least ₹50,000")
    private BigDecimal selectedCoverageAmount;

    @NotBlank(message = "Holder name is required")
    private String holderName;

    @NotBlank(message = "Holder address is required")
    private String holderAddress;

    @NotBlank(message = "Holder phone is required")
    @Pattern(regexp = "^[6-9]\\d{9}$", message = "Phone must be a valid 10-digit Indian mobile number")
    private String holderPhone;

    // Required for LIFE, HEALTH, TRAVEL — validated in service layer
    private String holderAadhaar;

    // Required only for MOTOR insurance — validated in service layer
    private String vehicleNumber;
}