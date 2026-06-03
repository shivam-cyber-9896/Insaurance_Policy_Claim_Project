package com.example.app.dto;

import com.example.app.enums.PaymentMode;
import com.example.app.enums.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponseDto {
    private Long id;
    private Long policyId;
    private String policyNumber;
    private BigDecimal amount;
    private LocalDateTime paymentDate;
    private PaymentMode paymentMode;
    private String transactionReference;
    private PaymentStatus paymentStatus;
}
