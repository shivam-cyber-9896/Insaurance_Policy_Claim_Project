package com.monocept.app.dto;

import java.math.BigDecimal;

import com.monocept.app.enums.ClaimStatus;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClaimReviewRequestDto {

    @NotNull(message = "Recommended status is required")
    private ClaimStatus recommendedStatus;

    @NotBlank(message = "Remarks are required")
    private String remarks;

    /**
     * The amount the agent recommends to pass/approve for this claim.
     * Optional — if not provided, the original claim amount is retained.
     * Must not exceed the original claim amount.
     */
    @DecimalMin(value = "0.01", inclusive = true, message = "Suggested amount must be at least 0.01")
    @DecimalMax(value = "99999999.99", message = "Suggested amount exceeds maximum allowed limit")
    @Digits(integer = 8, fraction = 2, message = "Suggested amount must have at most 8 integer digits and 2 decimal places")
    private BigDecimal suggestedAmount;
}
