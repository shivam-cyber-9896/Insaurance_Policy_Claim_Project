package com.monocept.app.dto;

import java.math.BigDecimal;
import com.monocept.app.enums.PaymentMode;
import com.monocept.app.enums.PremiumType;
import com.monocept.app.enums.ProductType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PremiumCalculatorRequestDto {

    @NotNull(message = "Coverage Amount is required")
    @DecimalMin(value = "1000.00", message = "Coverage Amount must be at least ₹1,000")
    private BigDecimal coverageAmount;

    @NotNull(message = "Duration in years is required")
    @Min(value = 1, message = "Duration must be at least 1 year")
    @Max(value = 40, message = "Duration cannot exceed 40 years")
    private Integer durationYears;

    @NotNull(message = "Premium Type is required")
    private PremiumType premiumType;

    private ProductType productType;

    @Min(value = 18, message = "Age must be at least 18")
    @Max(value = 99, message = "Age must be less than 100")
    private Integer age;

    private Boolean isSmoker;
}

