package com.example.app.dto;

import com.example.app.enums.PaymentMode;
import com.example.app.enums.PaymentStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRequestDto {

    @NotNull(message = "Policy ID is required")
    private Long policyId;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
    private BigDecimal amount;

    @NotNull(message = "Payment mode is required")
    private PaymentMode paymentMode;

    @NotBlank(message = "Transaction reference is required")
    private String transactionReference;

    @NotNull(message = "Payment status is required")
    private PaymentStatus paymentStatus;
}
