package com.monocept.app.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.monocept.app.enums.PaymentMode;
import com.monocept.app.enums.PaymentStatus;

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
